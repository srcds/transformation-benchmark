package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.AbstractBenchmark.TestConfiguration;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;

public class BenchmarkHighDim2 extends AbstractBenchmark {

    BenchmarkHighDim2(String fileName) {
        super(fileName);
    }

    public static void main(String args[]) throws IOException {
        new BenchmarkHighDim2("results/results_high_dim_2_local_5runs.csv").start();

    }

    @Override
    public List<TestConfiguration> generateTestConfigurations() {

        List<TestConfiguration> testConfigs = new ArrayList<TestConfiguration>();

        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] { AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_GENETIC,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN };
        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.CREDITCARD,
                                                               BenchmarkDataset.MACH2019,
                                                               BenchmarkDataset.SS13ACS };
        int[] timeLimits = new int[] { 5000, 10000, 30000, 60000, 120000 };

        int testRuns = 6;
        int localTransformationIterations = 10;
        boolean useLocalTransformation = true;
        boolean splitTimeLimitBetweenRuns = true;

        for(BenchmarkDataset dataset : datasets) {
        for (int timeLimit : timeLimits) {
            for (int testRun = 0; testRun < testRuns; testRun++) {
                for (AnonymizationAlgorithm algorithm : algorithms) {

                    TestConfiguration testConfig = new TestConfiguration();
                    testConfig.supression = 1d - (1d / (double) localTransformationIterations);
                    testConfig.algorithm = algorithm;
                    testConfig.dataset = dataset;
                    testConfig.testRunNumber = testRun;

                    if(useLocalTransformation) {
                        testConfig.model = Metric.createLossMetric(0d, AggregateFunction.ARITHMETIC_MEAN);
                        testConfig.useLocalTransformation = useLocalTransformation;
                        testConfig.localTransformationIterations = localTransformationIterations;
                    } else {
                        testConfig.model = Metric.createLossMetric(0.5d, AggregateFunction.ARITHMETIC_MEAN);
                    }

                    if(useLocalTransformation && splitTimeLimitBetweenRuns) {
                        testConfig.timeLimit = (int)(timeLimit / (double) localTransformationIterations) ;
                    } else {
                        testConfig.timeLimit = timeLimit;
                    }


                    // Just for warm-up
                    if (testRun == 0) {
                        testConfig.writeToFile = false;
                        if(useLocalTransformation) {
                            testConfig.timeLimit = (int)(5000 / (double) localTransformationIterations);
                        } else {
                            testConfig.timeLimit = 5000;
                        }   
                    }
                    testConfigs.add(testConfig);
                }
            }
        }
        }
        return testConfigs;
    }

}
