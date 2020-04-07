package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.AbstractBenchmark.TestConfiguration;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

public class PrintDatasetProperties extends AbstractBenchmark {

    public static void main(String args[]) throws IOException {
        System.out.println("Ja");
        new PrintDatasetProperties("dummy.csv").start();

    }
    
    PrintDatasetProperties(String fileName) {
        super(fileName);
    }

    @Override
    public List<TestConfiguration> generateTestConfigurations() {

        List<TestConfiguration> testConfigs = new ArrayList<TestConfiguration>();
        
        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.ADULT,
                                                               BenchmarkDataset.CUP,
                                                               BenchmarkDataset.FARS,
                                                               BenchmarkDataset.ATUS,
                                                               BenchmarkDataset.IHIS,
                                                               BenchmarkDataset.CREDITCARD,
                                                               BenchmarkDataset.MACH2019,
                                                               BenchmarkDataset.SS13ACS};
        
        
        for (BenchmarkDataset dataset : datasets) {

            TestConfiguration testConfig = new TestConfiguration();

            testConfig.algorithm = AnonymizationAlgorithm.BEST_EFFORT_GENETIC;
            testConfig.dataset = dataset;
            testConfig.gaIterations = 1;
            testConfig.writeToFile = false;
            testConfig.testRunNumber = 1337;
            testConfigs.add(testConfig);

        }
        
        
        return testConfigs;
    }

    
    
    
}
