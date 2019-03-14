package org.deidentifier.arx.benchmark;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkQualityModel;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkTransformationModel;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.io.CSVDataOutput;
import org.deidentifier.arx.io.CSVSyntax;
import org.deidentifier.arx.r.OS;
import org.deidentifier.arx.r.RIntegration;
import org.deidentifier.arx.utility.UtilityMeasure;
import org.deidentifier.arx.utility.UtilityMeasureGranularity;
import org.deidentifier.arx.utility.UtilityMeasureNonUniformEntropy;
import org.deidentifier.arx.utility.util.DataConverter;

/**
 * Benchmark executor
 * @author Fabian Prasser
 */
public class BenchmarkExecutorSDC extends BenchmarkExecutor {
    
    /** File*/
    private File input;
    /** File*/
    private File output;
	/** Integration*/
	private RIntegration r;

    /**
     * Creates a new instance
     * @param dataset
     * @param transformation
     * @param quality
     * @param qis
     * @param k
     * @param timeLimit
     */
    public BenchmarkExecutorSDC(BenchmarkDataset dataset, BenchmarkTransformationModel transformation, int qis, int k, int timeLimit) {
        super(dataset, transformation, qis, k, timeLimit);
    }

	@Override
	public void prepare() throws RollbackRequiredException, IOException {

		// Prepare files
		input = new File("input.csv");
		input.deleteOnExit();
		output = new File("output.csv");
		output.deleteOnExit();

		// Copy data
		Data data = BenchmarkSetup.getData(dataset, transformation, qis);
		CSVSyntax syntax = new CSVSyntax();
		new CSVDataOutput(input, syntax).write(data.getHandle().iterator());

		// R integration
		// Start integration
		r = new RIntegration(OS.getR());
		r.execute("require(sdcMicro)");
		r.execute("alphaParam <- 0");
		r.execute("kParam     <- " + k);
		r.execute("sepParam   <- \";\"");
		r.execute("fileInput  <- \"" + input.getAbsolutePath().replace("\\", "\\\\") + "\"");
		r.execute("fileOutput <- \"" + output.getAbsolutePath().replace("\\", "\\\\") + "\"");
	}

	@Override
	public void anonymize() throws RollbackRequiredException, IOException, BenchmarkTimeoutException {

		r.execute("input      <- readMicrodata(path=fileInput, type=\"csv\", convertCharToFac=TRUE, drop_all_missings=TRUE, header=TRUE, sep=sepParam)");
		r.execute("qis        <- colnames(input)");
		r.execute("sdcObj     <- createSdcObj(dat=input, keyVars=qis,  numVars=NULL,  weightVar=NULL, hhId=NULL, strataVar=NULL, pramVars=NULL, excludeVars=NULL, seed=0, randomizeRecords=FALSE, alpha=alphaParam)");
		r.execute("sdcObj     =  localSuppression(sdcObj, k=kParam, importance = NULL, combs = NULL)");
		r.execute("output     <- extractManipData(sdcObj)");
		r.execute("write.table(output, file = fileOutput, row.names=FALSE, na=\"*\",col.names=TRUE, sep=sepParam)");
		r.shutdown(BenchmarkSetup.TIME_LIMIT);
	}

	@Override
	public Map<BenchmarkQualityModel, Double> analyze() throws RollbackRequiredException, IOException {

	    // Load
	    DataHandle inHandle = BenchmarkSetup.getData(dataset, transformation, qis).getHandle();

	    try {
	    	
	        // Convert
	        DataConverter converter = new DataConverter();
	        String[][] _input = converter.toArray(inHandle);
	        String[] header = converter.getHeader(inHandle);
	        String[][] _output = converter.toArray(output, true);
	        String[] _header = header;
	        Map<String, String[][]> hierarchies = converter.toMap(inHandle.getDefinition());

	        // Result
	        Map<BenchmarkQualityModel, Double> result = new HashMap<>();

	        // Loss
	        UtilityMeasure<Double> model = new UtilityMeasureGranularity(_input);
	        double min = model.evaluate(_input).getUtility().doubleValue();
	        double max = model.evaluate(suppress(_input)).getUtility().doubleValue();
	        double val = model.evaluate(_output).getUtility().doubleValue();
	        result.put(BenchmarkQualityModel.LOSS, (1d - ((val - min) / (max - min))));

	        // Entropy
	        model = new UtilityMeasureNonUniformEntropy<Double>(_header, _input, hierarchies);
	        min = model.evaluate(_input).getUtility().doubleValue();
	        max = model.evaluate(suppress(_input)).getUtility().doubleValue();
	        val = model.evaluate(_output).getUtility().doubleValue();
	        result.put(BenchmarkQualityModel.NUENTROPY, (1d - ((val - min) / (max - min))));
	        
	        // Done
	        return result;
	        
		} catch (Exception e) {
			return null;
			
		} finally {

	        // Cleanup
	        input.delete();
	        output.delete();
		}
	}

	@Override
	public Map<BenchmarkQualityModel, Double> getTimeoutResult() {

        // Result
        Map<BenchmarkQualityModel, Double> result = new HashMap<>();
        result.put(BenchmarkQualityModel.LOSS, 0d);
	    result.put(BenchmarkQualityModel.NUENTROPY, 0d);
        return result;
	}
}
