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
//import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.metric.Metric.AggregateFunction;
import org.deidentifier.arx.aggregates.StatisticsQuality;
import org.deidentifier.arx.algorithm.AbstractAlgorithm;
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
    private static final Benchmark BENCHMARK = new Benchmark(new String[] { "Algorithm",
                                                                            "Dataset",
                                                                            "k",
                                                                            "QIs",
                                                                            "Iterations",
                                                                            "TimeLimit",
                                                                            "StopByOptimum",
                                                                            "TestRound",
                                                                            "QualityModel" });

    /** TIME */
    private static final int       TIME      = BENCHMARK.addMeasure("Time");

    /** UTILITY */
    private static final int       UTILITY   = BENCHMARK.addMeasure("Utility");

    /**
     * Main entry point
     * 
     * @param args
     * @throws IOException
     * @throws RollbackRequiredException
     */
    public static void main(String[] args) throws IOException, RollbackRequiredException {

        BENCHMARK.addAnalyzer(TIME, new ValueBuffer());
        BENCHMARK.addAnalyzer(UTILITY, new ValueBuffer());
        boolean writeToFile = true;
        
        /**
         * General ARX configuration
         */
        ARXConfiguration config = ARXConfiguration.create();
        config.setQualityModel(Metric.createLossMetric(0.5, AggregateFunction.ARITHMETIC_MEAN));
        config.setSuppressionLimit(1d);
        
        /**
         * Algorithm selection and specific configuration
         * 
         * 0 - Genetic
         * 1 - Flash
         * 2 - Lightning
         * 
         */
        int algorithmIndex = 1;
        int timeLimit = new int[] {Integer.MAX_VALUE, 1000, 5000, 30000, 60000, 300000}[3];
        boolean stopByOptimum = false;
        
        String fileName = "";
        
        switch (algorithmIndex) {
        case 0:
            config.setAlgorithm(AnonymizationAlgorithm.BEST_EFFORT_GENETIC);
            config.setGeneticAlgorithmSubpopulationSize(100);
            config.setGeneticAlgorithmEliteFraction(0.2);
            config.setGeneticAlgorithmCrossoverFraction(0.2);
            config.setGeneticAlgorithmIterations(50);
            config.setGeneticAlgorithmMutationProbability(0.2);          
            config.setHeuristicSearchTimeLimit(timeLimit);
            
            fileName = "results_GA_" + String.valueOf(timeLimit) + ".csv";
            break;
        case 1:
            config.setAlgorithm(AnonymizationAlgorithm.OPTIMAL);
            
            fileName = "results_OPTIMAL.csv";
            break;
        case 2:
            config.setAlgorithm(AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP);
            config.setHeuristicSearchTimeLimit(timeLimit);
            
            fileName = "results_LIGHTNING_" + String.valueOf(timeLimit) + ".csv";
            break;
        }
        
        File file = new File("results/" + fileName);

        /**
         * Iterable parameters
         */
        // int[] ks = new int[] { 2, 3, 5, 10 };
        int[] ks = new int[] { 5 };

        // int[] vals = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        int[] vals = new int[] { 0 }; // 0 --> max qids
        
        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.ADULT,BenchmarkDataset.CUP,BenchmarkDataset.FARS,BenchmarkDataset.ATUS,BenchmarkDataset.IHIS };
        //BenchmarkDataset[] datasets = new BenchmarkDataset[] {BenchmarkDataset.SS13ACS};

        
        /**
         * Number of test round for each configuration.
         */
        int testRounds = 1;

        // For each combination of iterable parameters
        for (BenchmarkDataset dataset : datasets) {

            for (int k : ks) {

                for (int val : vals) {

                    for (int testRound = 0; testRound < testRounds; testRound++) {
                        // check if is the fist run ("coldRun")
                        if (testRound == 0) benchmark(config.clone(), dataset, k, val, stopByOptimum, testRound, file, false);
                        benchmark(config.clone(), dataset, k, val, stopByOptimum, testRound, file, writeToFile);
                    }

                }
            }
            System.out.println();
        }
    }

    /**
     * Performs the benchmark
     * 
     * @param dataset
     * @throws IOException
     * @throws RollbackRequiredException
     */
    private static void benchmark(ARXConfiguration config,
                                  BenchmarkDataset dataset,
                                  int k,
                                  int qis,
                                  boolean stopByOptimum,
                                  int testRound,
                                  File file,
                                  boolean writeToFile) throws IOException, RollbackRequiredException {

        // Add k to config
        config.addPrivacyModel(new KAnonymity(k));

        // Load Dataset
        if (qis == 0) {
            qis = BenchmarkSetup.getQuasiIdentifyingAttributes(dataset).length;
        }
        Data input = BenchmarkSetup.getData(dataset, qis);
        
        if(stopByOptimum) {
            findAndSetOptimum(config.clone(), dataset, qis);
        }

        // Print current task
        System.out.println(String.valueOf(config.getAlgorithm()) + " | " + String.valueOf(dataset) + " | k=" + String.valueOf(k) + " | QIs=" + String.valueOf(qis) + " | TestRound="  + testRound);

        // Initalize and run anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        long time = System.currentTimeMillis();
        ARXResult result = anonymizer.anonymize(input, config);
        time = System.currentTimeMillis() - time;
        
        
        // Dont log if it was a cold run
        if (!writeToFile) {return;}
        
        // Analyse and store output
        DataHandle output = result.getOutput();
        Map<BenchmarkQualityModel, Double> utility = analyze(output.getStatistics().getQualityStatistics());

        Double.valueOf(result.getGlobalOptimum().getHighestScore().toString());
        
        
        for (Entry<BenchmarkQualityModel, Double> entry : utility.entrySet()) {
            BENCHMARK.addRun(String.valueOf(config.getAlgorithm()),
                             String.valueOf(dataset),
                             String.valueOf(k),
                             String.valueOf(qis),
                             String.valueOf(config.getGeneticAlgorithmIterations()),
                             String.valueOf(config.getHeuristicSearchTimeLimit()),
                             String.valueOf(stopByOptimum),
                             String.valueOf(testRound),
                             String.valueOf(entry.getKey().toString()));
            BENCHMARK.addValue(TIME, time);
            BENCHMARK.addValue(UTILITY, entry.getValue());
        }

        BENCHMARK.getResults().write(file);



    }

    private static void findAndSetOptimum(ARXConfiguration config, BenchmarkDataset dataset, int qis) throws IOException {
                
        if (qis == 0) {
            qis = BenchmarkSetup.getQuasiIdentifyingAttributes(dataset).length;
        }
        Data input = BenchmarkSetup.getData(dataset, qis);
                
        config.setAlgorithm(AnonymizationAlgorithm.OPTIMAL);
        AbstractAlgorithm.lossLimit = -1;
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(input, config);

        AbstractAlgorithm.lossLimit = Double.valueOf(result.getGlobalOptimum().getHighestScore().toString());
    }
    
    private static Map<BenchmarkQualityModel, Double> analyze(StatisticsQuality stats) {
        Map<BenchmarkQualityModel, Double> result = new HashMap<>();

        // result.put(BenchmarkQualityModel.SSE, stats.getSSESST().getValue());
        result.put(BenchmarkQualityModel.LOSS, stats.getGranularity().getArithmeticMean());

        return result;
    }

}
