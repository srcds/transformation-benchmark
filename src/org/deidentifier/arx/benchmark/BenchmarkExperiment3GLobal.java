package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

/**
 * @author Thierry
 *
 *         Used for execute Benchmarks on high dim datasets with a continuously
 *         tracked utility improvement over time.
 *
 */
public class BenchmarkExperiment3GLobal extends AbstractBenchmark {

    BenchmarkExperiment3GLobal(String fileName) {
        super(fileName, true, false);
    }

    public static void main(String args[]) throws IOException {
        new BenchmarkExperiment3GLobal("results/results_high_dim_1_global_popUnique.csv").start();

    }

    @Override
    public void generateTestConfigurations(List<TestConfiguration> testConfigs) {

        
        // Definition of properties that will be varied for the Benchmark
        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] { AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_GENETIC,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN };
        
        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.CREDITCARD,
                                                               BenchmarkDataset.MACH2019,
                                                               BenchmarkDataset.SS13ACS };
        
        // AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] {AnonymizationAlgorithm.BEST_EFFORT_GENETIC };
        int[] timeLimits = new int[] {5000, 10000, 30000, 60000, 120000};

        // Number of testruns
        int testRuns = 6;

        // iterate through all possible configuration permutations
        for (int testRun = 0; testRun < testRuns; testRun++) {

            for (BenchmarkDataset dataset : datasets) {
                for (int timeLimit : timeLimits) {
                    for (AnonymizationAlgorithm algorithm : algorithms) {

                        TestConfiguration testConfig = new TestConfiguration();

                        testConfig.algorithm = algorithm;
                        testConfig.timeLimit = timeLimit;
                        testConfig.dataset = dataset;
                        testConfig.testRunNumber = testRun;

                        testConfig.crossoverFraction = 0.4;
                        testConfig.mutationProbability = 0.05;
                        
                        testConfig.privacyModel = PrivacyModel.POPULATION_UNIQUENESS;
                        
                        testConfig.useLocalTransformation = false;

                        if (testRun == 0) {
                            testConfig.writeToFile = false;
                            testConfig.timeLimit = 5000;
                        }
                        testConfigs.add(testConfig);
                    }
                }
            }
        }
    }

}
