package org.deidentifier.arx.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkQualityModel;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkTransformationModel;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.io.CSVDataOutput;
import org.deidentifier.arx.utility.UtilityMeasure;
import org.deidentifier.arx.utility.UtilityMeasureGranularity;
import org.deidentifier.arx.utility.UtilityMeasureSSE;
import org.deidentifier.arx.utility.util.DataConverter;

import anonymizer.Anonymizer;

/**
 * Benchmark executor
 * @author Fabian Prasser
 */
public class BenchmarkExecutorMondrian extends BenchmarkExecutor {
    
    /** File*/
    private File input;
    /** File*/
    private File output;
    /** File*/
    private File config;

    /**
     * Creates a new instance
     * @param dataset
     * @param transformation
     * @param quality
     * @param qis
     * @param k
     * @param timeLimit
     */
    public BenchmarkExecutorMondrian(BenchmarkDataset dataset, BenchmarkTransformationModel transformation, int qis, int k, int timeLimit) {
        super(dataset, transformation, qis, k, timeLimit);
    }

	@Override
	public void prepare() throws RollbackRequiredException, IOException {
		

        // Write file
        Data data = BenchmarkSetup.getData(dataset, transformation, qis);
        input = new File("input.csv");
        input.deleteOnExit();
        output = new File("output.csv");
        output.deleteOnExit();
        Iterator<String[]> iter = data.getHandle().iterator();
        iter.next(); // Skip header
        new CSVDataOutput(input.getAbsolutePath()).write(iter);
        
        // Write config
        config = new File("config.xml");
        config.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(config));
        
        writer.write("<config k=\"" + k + "\" method=\"mondrian\">\n");
        writer.write("   <input filename=\"" + input.getAbsolutePath() + "\" separator=\";\"/>\n");
        writer.write("   <output filename=\"" + output.getAbsolutePath()+ "\" format=\"genVals\"/>\n");
        writer.write("   <qid>\n");

        for (int column = 0; column < data.getHandle().getNumColumns(); column++) {
            String attribute = data.getHandle().getAttributeName(column);
            writer.write("      <att index=\""+column+"\" name=\""+attribute+"\">\n");
            String[] values = data.getHandle().getDistinctValues(column);
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (String value : values) {
                min = Math.min(Integer.parseInt(value), min);
                max = Math.max(Integer.parseInt(value), max);
            }
            writer.write("         <vgh value=\"["+min+":"+max+"]\"/>\n");
            writer.write("      </att>\n");
        }
        writer.write("   </qid>\n");
        writer.write("</config>\n");
        writer.close();
	}

	@Override
	public void anonymize() throws RollbackRequiredException, IOException, BenchmarkTimeoutException {
		try {
			Anonymizer.anonymizeDataset(new String[] {"-config", config.getAbsolutePath()});
		} catch (Exception e) {
			if (e instanceof BenchmarkTimeoutException) {
				throw((BenchmarkTimeoutException)e);
			} else {
			    throw new BenchmarkErrorException(e);
			}
			// Catch everything else silently
		}
	}

	@Override
	public Map<BenchmarkQualityModel, Double> analyze() throws RollbackRequiredException, IOException {

	    // Load
	    DataHandle inHandle = BenchmarkSetup.getData(dataset, transformation, qis).getHandle();

	    try {
	        // Convert
	        DataConverter converter = new DataConverter();
	        String[][] _input = converter.toArray(inHandle);
	        String[][] _output = converter.toArray(output, false);

	        // Result
	        Map<BenchmarkQualityModel, Double> result = new HashMap<>();

	        // Loss
	        UtilityMeasure<Double> model = new UtilityMeasureGranularity(_input);
	        double min = model.evaluate(_input).getUtility().doubleValue();
	        double max = model.evaluate(suppress(_input)).getUtility().doubleValue();
	        double val = model.evaluate(_output).getUtility().doubleValue();
	        result.put(BenchmarkQualityModel.LOSS, (1d - ((val - min) / (max - min))));

	        // SSE
	        model = new UtilityMeasureSSE(_input);
	        min = model.evaluate(_input).getUtility().doubleValue();
	        max = model.evaluate(suppress(_input)).getUtility().doubleValue();
	        val = model.evaluate(_output).getUtility().doubleValue();
	        result.put(BenchmarkQualityModel.SSE, (1d - ((val - min) / (max - min))));
	        
	        // Done
	        return result;
	        
	    } catch (IOException e) {
	    	return null;
	    	
	    } finally {

	        // Delete
	        input.delete();
	        output.delete();
	        config.delete();
	    }
	}

	@Override
	public Map<BenchmarkQualityModel, Double> getTimeoutResult() {

        // Result
        Map<BenchmarkQualityModel, Double> result = new HashMap<>();
        result.put(BenchmarkQualityModel.LOSS, 0d);
	    result.put(BenchmarkQualityModel.SSE, 0d);
        return result;
	}
}
