package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.AbstractBenchmark.TestConfiguration;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;


public class BenchmarkLowDim2 extends AbstractBenchmark{

    BenchmarkLowDim2(String fileName) {
        super(fileName, true, false);
    }
    
    public static void main(String args[]) throws IOException {
        new BenchmarkLowDim2("NEW_results_low_dim_WAN_MP_02.csv").start();
    }
    


    @Override
    public void generateTestConfigurations(List<TestConfiguration> testConfigs) {
                
        // Definition of properties that will be varied for the Benchmark
        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.ADULT,
                                                               BenchmarkDataset.CUP,
                                                               BenchmarkDataset.FARS,
                                                               BenchmarkDataset.ATUS,
                                                               BenchmarkDataset.IHIS };
        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] { AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_GENETIC,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN,
                                                                             AnonymizationAlgorithm.OPTIMAL};
        int testRuns = 3;
        
        // iterate through all possible configuration permutations
        for (BenchmarkDataset dataset : datasets) {
            for (int testRun = 0; testRun < testRuns; testRun++) {
                for (AnonymizationAlgorithm algorithm : algorithms) {

                    TestConfiguration testConfig = new TestConfiguration();

                    testConfig.algorithm = algorithm;
                    testConfig.dataset = dataset;
                    testConfig.testRunNumber = testRun;

                    testConfig.crossoverFraction = 0.4;
                    //testConfig.mutationProbability = 0.05;
                    
                    
                    testConfig.limitByOptimalLoss = true;
                    testConfig.timeLimit = 600000;
                    

                    if (testRun == 0) testConfig.writeToFile = false;

                    testConfigs.add(testConfig);
                }
            }
        }

    }

}
