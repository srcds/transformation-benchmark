package org.deidentifier.arx.benchmark;

import java.io.File;
import java.io.IOException;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;

import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkTransformationModel;

import de.linearbits.subframe.Benchmark;
import de.linearbits.subframe.analyzer.ValueBuffer;

/**
 * Main benchmark class.
 * 
 * @author Fabian Prasser
 */
public class BenchmarkExperimentThierry {

	/** The benchmark instance */
	private static final Benchmark BENCHMARK = new Benchmark(new String[] { "Dataset", "k", "QIs", "Iterations", "Run_Number" });

	/** TIME */
	private static final int TIME = BENCHMARK.addMeasure("Time");

	/** UTILITY */
	private static final int UTILITY = BENCHMARK.addMeasure("Utility");

	/** FILE */
	private static final File FILE = new File("results/results-thierry.csv");

	/**
	 * Main entry point
	 * 
	 * @param args
	 * @throws IOException
	 * @throws RollbackRequiredException
	 */
	public static void main(String[] args) throws IOException, RollbackRequiredException {

		int[] ks = new int[] { 2, 3, 5, 10 };
		int[] vals = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		int[] iterations = new int[] {100,500,1000,5000};
		BenchmarkTransformationModel[] transformations = new BenchmarkTransformationModel[] {
                BenchmarkTransformationModel.MULTI_DIMENSIONAL_GENERALIZATION,
                BenchmarkTransformationModel.LOCAL_GENERALIZATION,
                };

		// Init
		BENCHMARK.addAnalyzer(TIME, new ValueBuffer());
		BENCHMARK.addAnalyzer(UTILITY, new ValueBuffer());

		// Standard all config
		BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.ADULT, BenchmarkDataset.CUP,
				BenchmarkDataset.FARS, BenchmarkDataset.ATUS, BenchmarkDataset.IHIS, BenchmarkDataset.SS13ACS };

		// For each data set
		for (BenchmarkDataset dataset : datasets) {

			for (int k : ks) {

				for (int val : vals) {
					
					for (int iter : iterations) {

					benchmark(dataset, k, val, iter);
					}
				}
			}
		}
	}

	/**
	 * Performs the benchmark
	 * 
	 * @param dataset
	 * @throws IOException
	 * @throws RollbackRequiredException
	 */
	private static void benchmark(BenchmarkDataset dataset, int k, int qis, int gaIterations) throws IOException, RollbackRequiredException {

		// TODO: WARMUP, DANN N WIEDERHOLUNGEN
		int testIterations = 3;

		// Quality
		ARXConfiguration config = ARXConfiguration.create();
		config.setQualityModel(Metric.createLossMetric(0d));

		// Privacy
		config.addPrivacyModel(new KAnonymity(k));
		config.setHeuristicSearchStepLimit(gaIterations);

		// Dataset
		Data input = BenchmarkSetup.getData(dataset, qis);

		// Anonymize (warmup)
		ARXAnonymizer anonymizer = new ARXAnonymizer();
		ARXResult result = anonymizer.anonymize(input, config);
		DataHandle output = result.getOutput();
		double utility = output.getStatistics().getQualityStatistics().getGranularity().getArithmeticMean();

		long time;
		
		System.out.println(String.valueOf(dataset) + " | " + String.valueOf(k) +" | "+ String.valueOf(qis) + " | "+ String.valueOf(gaIterations));
		for(int i = 0; i < testIterations; i++) {
			
			time = System.currentTimeMillis();
			anonymizer = new ARXAnonymizer();
			input = BenchmarkSetup.getData(dataset, qis);
			result = anonymizer.anonymize(input, config);
			output = result.getOutput();
			utility = output.getStatistics().getQualityStatistics().getGranularity().getArithmeticMean();
			output.release();
			BENCHMARK.addRun(String.valueOf(dataset), String.valueOf(k), String.valueOf(qis), String.valueOf(gaIterations), String.valueOf(i));
			
			BENCHMARK.addValue(TIME, System.currentTimeMillis() - time);
			BENCHMARK.addValue(UTILITY, utility);
			// Save
			BENCHMARK.getResults().write(FILE);
			
			System.out.print(".");
		
		}
		
		// Store
		//BENCHMARK.addRun(String.valueOf(dataset), String.valueOf(k), String.valueOf(qis));
		// BENCHMARK.addRun(String.valueOf(dataset));

	}
	

}
