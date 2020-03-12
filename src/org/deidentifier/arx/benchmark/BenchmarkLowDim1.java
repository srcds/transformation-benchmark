package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;


public class BenchmarkLowDim1 extends AbstractBenchmark{

    BenchmarkLowDim1(String fileName) {
        super(fileName);
    }
    
    public static void main(String args[]) throws IOException {
        new BenchmarkLowDim1("results_low_dim1_tracked.csv").start();
    }

    @Override
    public List<TestConfiguration> generateTestConfigurations() {
        
        List<TestConfiguration> testConfigs = new ArrayList<TestConfiguration>();
        
        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.ADULT,BenchmarkDataset.CUP,BenchmarkDataset.FARS,BenchmarkDataset.ATUS,BenchmarkDataset.IHIS };
        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] {AnonymizationAlgorithm.OPTIMAL, AnonymizationAlgorithm.BEST_EFFORT_GENETIC};
        int testRuns = 11;
        
        for (BenchmarkDataset dataset : datasets) {
            for (int testRun = 0; testRun < testRuns; testRun++) {
                for (AnonymizationAlgorithm algorithm : algorithms) {

                    TestConfiguration testConfig = new TestConfiguration();

                    testConfig.algorithm = algorithm;
                    testConfig.dataset = dataset;
                    testConfig.testRunNumber = testRun;

                    if (testRun == 0) testConfig.writeToFile = false;

                    testConfigs.add(testConfig);
                }
            }
        }

        return testConfigs;
    }

}
