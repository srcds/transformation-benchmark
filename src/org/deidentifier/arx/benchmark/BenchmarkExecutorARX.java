package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXListener;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkQualityModel;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkTransformationModel;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.io.CSVDataOutput;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.utility.UtilityMeasure;
import org.deidentifier.arx.utility.UtilityMeasureGranularity;
import org.deidentifier.arx.utility.UtilityMeasureSSE;
import org.deidentifier.arx.utility.util.DataConverter;

/**
 * Benchmark executor
 * @author Fabian Prasser
 */
public class BenchmarkExecutorARX extends BenchmarkExecutor {

	/** Temporary result*/
	private DataHandle output;
	/** Input*/
	private Data input;

	/**
	 * Creates a new instance
	 * @param dataset
	 * @param transformation
	 * @param qis
	 * @param k
	 * @param timeLimit
	 */
	public BenchmarkExecutorARX(BenchmarkDataset dataset, BenchmarkTransformationModel transformation,
			int qis, int k, int timeLimit) {
		super(dataset, transformation, qis, k, timeLimit);
	}

	@Override
	public void prepare() throws RollbackRequiredException, IOException {
		// Empty by design
	}

	@Override
	public void anonymize() throws RollbackRequiredException, IOException, BenchmarkTimeoutException {

		// Default settings
		int iterations = 100;

        // Quality
		ARXConfiguration config = ARXConfiguration.create();
		config.setQualityModel(Metric.createLossMetric(0d));
		
		// Privacy
		config.addPrivacyModel(new KAnonymity(k));
		
		// Dataset
		this.input = BenchmarkSetup.getData(dataset, transformation, qis);

		// Transformation
		if (transformation == BenchmarkTransformationModel.LOCAL_GENERALIZATION) {
			for (String qi : input.getDefinition().getQuasiIdentifyingAttributes()) {
			    input.getDefinition().setDataType(qi, DataType.INTEGER);
				input.getDefinition().setMicroAggregationFunction(qi, MicroAggregationFunction.createInterval(), true);
			}
		}
		
		// Suppression
		config.setSuppressionLimit(1d - (1d / (double)iterations));
		
		// Heuristic search
		if (dataset == BenchmarkDataset.SS13ACS) {
			config.setHeuristicSearchEnabled(true);
			config.setHeuristicSearchTimeLimit((int)((double)timeLimit / (double)iterations));
		}
		
		// Anonymize
		final long time = System.currentTimeMillis();
		ARXAnonymizer anonymizer = new ARXAnonymizer();
		ARXResult result = anonymizer.anonymize(input, config);
		this.output = result.getOutput();
		result.optimizeIterativeFast(this.output, 1d / (double) iterations, new ARXListener() {
            @Override
            public void progress(double arg0) {
            	if (dataset != BenchmarkDataset.SS13ACS) {
	                if (System.currentTimeMillis() - time > timeLimit) {
	                    throw new BenchmarkTimeoutException();
	                }
            	}
            }
        });
		
	}

	@Override
	public Map<BenchmarkQualityModel, Double> analyze() throws RollbackRequiredException, IOException {
	    
        // Convert
        DataConverter converter = new DataConverter();
        String[][] _input = converter.toArray(input);
        String[][] _output = converter.toArray(output);

        new CSVDataOutput("output-benchmark.csv").write(output.iterator());
        
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
