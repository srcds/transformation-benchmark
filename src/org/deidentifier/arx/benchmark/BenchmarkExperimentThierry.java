package org.deidentifier.arx.benchmark;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsQuality;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkQualityModel;
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
 * @author Thierry Meurers
 */
public class BenchmarkExperimentThierry {

    /** The benchmark instance */
    private static final Benchmark BENCHMARK = new Benchmark(new String[] { "Dataset",
                                                                            "k",
                                                                            "QIs",
                                                                            "Iterations",
                                                                            "Round",
                                                                            "QualityModel" });

    /** TIME */
    private static final int       TIME      = BENCHMARK.addMeasure("Time");

    /** UTILITY */
    private static final int       UTILITY   = BENCHMARK.addMeasure("Utility");

    /** FILE */
    private static final File      FILE      = new File("results/results-thierry.csv");

	
	/**
	 * Main entry point
	 * 
	 * @param args
	 * @throws IOException
	 * @throws RollbackRequiredException
	 */
	public static void main(String[] args) throws IOException, RollbackRequiredException {

		//int[] ks = new int[] { 2, 3, 5, 10 };
		int[] ks = new int[] {5};
		
		//int[] vals = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		int[] vals = new int[] {5};
		
		//int[] iterations = new int[] {50,100,500};
		int[] iterations = new int[] {200};
		
		BenchmarkTransformationModel[] transformations = new BenchmarkTransformationModel[] {
                BenchmarkTransformationModel.MULTI_DIMENSIONAL_GENERALIZATION,
                BenchmarkTransformationModel.LOCAL_GENERALIZATION,
                };
		int testRounds = 1;
		

		
		// Init
		BENCHMARK.addAnalyzer(TIME, new ValueBuffer());
		BENCHMARK.addAnalyzer(UTILITY, new ValueBuffer());

		// Standard all config
		BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.ADULT, BenchmarkDataset.CUP, BenchmarkDataset.FARS, BenchmarkDataset.ATUS, BenchmarkDataset.IHIS, BenchmarkDataset.SS13ACS };
		
		//BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.IHIS};

		// For each data set
		for (BenchmarkDataset dataset : datasets) {

			for (int k : ks) {

				for (int val : vals) {
					
					for (int iter : iterations) {
						
						for (int testRound = 0; testRound < testRounds; testRound++) {
							benchmark(dataset, k, val, iter, testRound, false);
						}
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
	private static void benchmark(BenchmarkDataset dataset, int k, int qis, int gaIterations, int testRound, boolean coldRun) throws IOException, RollbackRequiredException {

	    System.out.println(String.valueOf(dataset) + " | " + String.valueOf(k) +" | "+ String.valueOf(qis) + " | "+ String.valueOf(gaIterations) + " | " + testRound);
	    
		// Quality
		ARXConfiguration config = ARXConfiguration.create();
		config.setQualityModel(Metric.createLossMetric(0d));

		// config
		config.addPrivacyModel(new KAnonymity(k));
		config.setSuppressionLimit(1d);
		
		config.setGeneticAlgorithmIterations(gaIterations);
		config.setHeuristicSearchStepLimit(Integer.MAX_VALUE);
		config.setHeuristicSearchTimeLimit(Integer.MAX_VALUE);
		config.setGeneticAlgorithmSubpopulationSize(100);
		config.setGeneticAlgorithmEliteFraction(0.2);
		config.setGeneticAlgorithmCrossoverFraction(0.2);
		config.setAlgorithm(AnonymizationAlgorithm.BEST_EFFORT_GENETIC);
		//config.setGeneticAlgorithmMutationProbability(0.2);

		
		
		// Dataset
		//Data input = BenchmarkSetup.getData(dataset, qis);
		Data input = BenchmarkSetup.getData(dataset, BenchmarkSetup.getQuasiIdentifyingAttributes(dataset).length);

		
		ARXAnonymizer anonymizer = new ARXAnonymizer();
		
		long time = System.currentTimeMillis();
		ARXResult result = anonymizer.anonymize(input, config);
		time = System.currentTimeMillis() - time;
		DataHandle output = result.getOutput();
		
        Map<BenchmarkQualityModel, Double> utility = analyze(output.getStatistics().getQualityStatistics());

        if (coldRun) {
        	benchmark(dataset, k, qis, gaIterations, testRound, false);
        	return;
        }
        	
        
        // Store
        for (Entry<BenchmarkQualityModel, Double> entry : utility.entrySet()) {
			BENCHMARK.addRun(String.valueOf(dataset), String.valueOf(k), String.valueOf(qis), String.valueOf(gaIterations), String.valueOf(testRound), String.valueOf(entry.getKey().toString()) );
	        BENCHMARK.addValue(TIME, time);
	        BENCHMARK.addValue(UTILITY, entry.getValue());
        }
		
		BENCHMARK.getResults().write(FILE);
		
		System.out.println();

	}
	
	private static Map<BenchmarkQualityModel, Double> analyze(StatisticsQuality stats){
		Map<BenchmarkQualityModel, Double> result = new HashMap<>();
		
		result.put(BenchmarkQualityModel.SSE, stats.getSSESST().getValue());
		result.put(BenchmarkQualityModel.LOSS, stats.getGranularity().getArithmeticMean());
		
		return result;
	}

}
