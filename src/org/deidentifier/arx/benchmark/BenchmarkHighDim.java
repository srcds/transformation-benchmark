package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.AbstractBenchmark.TestConfiguration;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

public class BenchmarkHighDim extends AbstractBenchmark{

    BenchmarkHighDim(String fileName) {
        super(fileName);
    }

    public static void main(String args[]) throws IOException {
        new BenchmarkHighDim("results_high_dim.csv").start();
        
    }
    @Override
    public List<TestConfiguration> generateTestConfigurations() {
        
        List<TestConfiguration> testConfigs = new ArrayList<TestConfiguration>();

        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] {AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP, AnonymizationAlgorithm.BEST_EFFORT_GENETIC};
        int[] timeLimits = new int[] {1000,5000,30000, 60000, 120000};
        
        int testRuns = 11;
        
        
            for(int timeLimit : timeLimits) {
                for(int testRun = 0; testRun < testRuns; testRun++) {
                    for (AnonymizationAlgorithm algorithm : algorithms) {
                    
                    TestConfiguration testConfig = new TestConfiguration();
                    
                    testConfig.algorithm = algorithm;
                    testConfig.timeLimit = timeLimit;
                    testConfig.dataset = BenchmarkDataset.SS13ACS;
                    testConfig.testRunNumber = testRun;
                    
                    if(testRun == 0)
                        testConfig.writeToFile = false;
                    
                    testConfigs.add(testConfig);
                }
            }
        }

        return testConfigs;
    }

}