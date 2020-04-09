package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.AbstractBenchmark.TestConfiguration;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

/**
 * @author Thierry
 *
 *         Used for execute Benchmarks on high dim datasets with a continuously
 *         tracked utility improvement over time.
 *
 */
public class BenchmarkHighDim extends AbstractBenchmark {

    BenchmarkHighDim(String fileName) {
        super(fileName, true, true);
    }

    public static void main(String args[]) throws IOException {
        new BenchmarkHighDim("results/results_high_dim_1_creditcard_3h.csv").start();

    }

    @Override
    public void generateTestConfigurations(List<TestConfiguration> testConfigs) {

        
        // Definition of properties that will be varied for the Benchmark
        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] { AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_GENETIC,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN };
        
        // AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] {AnonymizationAlgorithm.BEST_EFFORT_GENETIC };
        // int[] timeLimits = new int[] {1000,5000,30000, 60000, 120000};

        // Number of testruns
        int testRuns = 2;

        // iterate through all possible configuration permutations
        for (int testRun = 0; testRun < testRuns; testRun++) {
            for (AnonymizationAlgorithm algorithm : algorithms) {

                TestConfiguration testConfig = new TestConfiguration();

                testConfig.algorithm = algorithm;
                testConfig.timeLimit = 10800000;
                testConfig.dataset = BenchmarkDataset.CREDITCARD;
                testConfig.testRunNumber = testRun;

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
