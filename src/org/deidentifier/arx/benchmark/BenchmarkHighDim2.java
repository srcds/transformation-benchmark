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
 *         Used to perform Benchmarks on high dim datasets. Utility measurement
 *         is only performed once after reaching the final solution. Also
 *         capable of using local transformation.
 * 
 */
public class BenchmarkHighDim2 extends AbstractBenchmark {

    BenchmarkHighDim2(String fileName) {
        super(fileName, true, false);
    }

    public static void main(String args[]) throws IOException {
        new BenchmarkHighDim2("results/results_high_dim_2_local_5runs_100iters_5_10_20_cf04.csv").start();

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
        int[] timeLimits = new int[] {500000, 1000000, 2000000};
        
        // Number of testruns
        int testRuns = 6;
        
        
        // Configuration regarding the local transformation
        int localTransformationIterations = 100;
        boolean useLocalTransformation = true;
        boolean splitTimeLimitBetweenRuns = true;

        // iterate through all possible configuration permutations
        for (int testRun = 0; testRun < testRuns; testRun++) {
            for (BenchmarkDataset dataset : datasets) {
                for (int timeLimit : timeLimits) {

                    for (AnonymizationAlgorithm algorithm : algorithms) {

                        TestConfiguration testConfig = new TestConfiguration();

                        testConfig.algorithm = algorithm;
                        testConfig.dataset = dataset;
                        testConfig.testRunNumber = testRun;
                        testConfig.mutationProbability = 0.05;
                        testConfig.crossoverFraction = 0.4;

                        if (useLocalTransformation) {
                            testConfig.gsFactor = 0d;
                            testConfig.useLocalTransformation = useLocalTransformation;
                            testConfig.localTransformationIterations = localTransformationIterations;
                            testConfig.supression = 1d -
                                                    (1d / (double) localTransformationIterations);
                        } else {
                            testConfig.gsFactor = 0.5d;
                            testConfig.supression = 1d;
                        }

                        if (useLocalTransformation && splitTimeLimitBetweenRuns) {
                            testConfig.timeLimit = (int) (timeLimit /
                                                          (double) localTransformationIterations);
                        } else {
                            testConfig.timeLimit = timeLimit;
                        }

                        // Just for warm-up (dont write to log and limit time to
                        // 5s)
                        if (testRun == 0) {
                            testConfig.writeToFile = false;
                            if (useLocalTransformation) {
                                testConfig.timeLimit = (int) (5000 /
                                                              (double) localTransformationIterations);
                            } else {
                                testConfig.timeLimit = 5000;
                            }
                        }
                        testConfigs.add(testConfig);
                    }
                }
            }
        }
    }

}
