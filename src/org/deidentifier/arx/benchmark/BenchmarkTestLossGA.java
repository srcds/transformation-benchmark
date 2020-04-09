package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.AbstractBenchmark.TestConfiguration;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;


public class BenchmarkTestLossGA extends AbstractBenchmark{

    BenchmarkTestLossGA(String fileName) {
        super(fileName, true, false);
    }
    
    public static void main(String args[]) throws IOException {
        new BenchmarkTestLossGA("empty.csv").start();
    }

    @Override
    public void generateTestConfigurations(List<TestConfiguration> testConfigs) {
        
        
        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.ADULT,
                                                               BenchmarkDataset.CUP,
                                                               BenchmarkDataset.FARS,
                                                               BenchmarkDataset.ATUS,
                                                               BenchmarkDataset.IHIS,
                                                               BenchmarkDataset.CREDITCARD,
                                                               BenchmarkDataset.MACH2019,
                                                               BenchmarkDataset.SS13ACS };
        
        for (BenchmarkDataset dataset : datasets) {

            TestConfiguration testConfig = new TestConfiguration();

            testConfig.algorithm = AnonymizationAlgorithm.BEST_EFFORT_GENETIC;
            testConfig.dataset = dataset;
            testConfig.timeLimit = 30000;
            testConfig.writeToFile = false;

            testConfigs.add(testConfig);

        }

    }

}
