package org.deidentifier.arx.benchmark;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkQualityModel;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkTransformationModel;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.io.CSVDataOutput;
import org.deidentifier.arx.io.CSVSyntax;
import org.deidentifier.arx.utility.UtilityMeasure;
import org.deidentifier.arx.utility.UtilityMeasureGranularity;
import org.deidentifier.arx.utility.UtilityMeasureSSE;
import org.deidentifier.arx.utility.util.DataConverter;

import urv.crises.anonym.Functions;
import urv.crises.anonym.Record;

/**
 * Benchmark executor
 * @author Fabian Prasser
 */
public class BenchmarkExecutorSanchez extends BenchmarkExecutor {

    /** Header */
    private String[]          header;
    /** Output */
    private ArrayList<Record> output;
    /** Input */
    private Data              input;
    /** Filename */
    private String            filename;

    /**
     * Creates a new instance
     * @param dataset
     * @param transformation
     * @param quality
     * @param qis
     * @param k
     * @param timeLimit
     */
    public BenchmarkExecutorSanchez(BenchmarkDataset dataset, BenchmarkTransformationModel transformation, int qis, int k, int timeLimit) {
        super(dataset, transformation, qis, k, timeLimit);
    }

	@Override
	public void prepare() throws RollbackRequiredException, IOException {

	    // Create temp file
		File file = new File("input.csv");
		file.deleteOnExit();
	    this.filename = file.getAbsolutePath();
	    
        // Load
        this.input = BenchmarkSetup.getData(dataset, transformation, qis);
        
        // Store
        CSVSyntax syntax = new CSVSyntax();
        syntax.setDelimiter(',');
        new CSVDataOutput(filename, syntax).write(this.input.getHandle().iterator());
	}

	@Override
	public void anonymize() throws RollbackRequiredException, IOException, BenchmarkTimeoutException {

        // Anonymize
	    // We can ignore the timeout, because this algorithm is so fast
        ArrayList<String[]> input = new ArrayList<>();
        this.header = Functions.loadFile(this.filename, input);
        this.output = Functions.kAnonymize(input, k);
	}

	@Override
	public Map<BenchmarkQualityModel, Double> analyze() throws RollbackRequiredException, IOException {
	    
        // Convert
        DataConverter converter = new DataConverter();
        String[][] _input = converter.toArray(input);
        String[][] _output = converter.toArray(output, header);

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
        
        // Delete
        new File(filename).delete();

        // Done
        return result;
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
