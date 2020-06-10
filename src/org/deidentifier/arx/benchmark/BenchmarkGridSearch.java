package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.AbstractBenchmark.TestConfiguration;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;


public class BenchmarkGridSearch extends AbstractBenchmark{

    BenchmarkGridSearch(String fileName) {
        super(fileName, true, false);
    }
    
    public static void main(String args[]) throws IOException {
        new BenchmarkGridSearch("NEW_results_tune_MP_IHIS.csv").start();
    }
    


    @Override
    public void generateTestConfigurations(List<TestConfiguration> testConfigs) {

        // Definition of properties that will be varied for the Benchmark

        int testRuns = 4;

        double[] mutationProbabilities = new double[] { 0.05, 0.1, 0.2, 0.3, 0.4, 0.6 };
        int[] timeLimits = new int[] { 1000, 5000, 10000, 30000};

        // iterate through all possible configuration permutations
        for (int testRun = 0; testRun < testRuns; testRun++) {
            for (int timeLimit : timeLimits) {
                for (double mutationProbability : mutationProbabilities) {

                    TestConfiguration testConfig = new TestConfiguration();

                    testConfig.algorithm = AnonymizationAlgorithm.BEST_EFFORT_GENETIC;
                    testConfig.dataset = BenchmarkDataset.IHIS;
                    testConfig.testRunNumber = testRun;

                    testConfig.crossoverFraction = 0.4;
                    testConfig.mutationProbability = mutationProbability;

                    testConfig.timeLimit = timeLimit;

                    if (testRun == 0) testConfig.writeToFile = false;

                    testConfigs.add(testConfig);

                }
            }

        }
    }
}
