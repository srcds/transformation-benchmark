package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;


public class BenchmarkLowDim extends AbstractBenchmark{

    BenchmarkLowDim(String fileName) {
        super(fileName);
    }
    
    public static void main(String args[]) throws IOException {
        new BenchmarkLowDim("results_low_dim.csv").start();
    }
    


    @Override
    public List<TestConfiguration> generateTestConfigurations() {
        
        List<TestConfiguration> result = new ArrayList<TestConfiguration>();
        
        TestConfiguration config = new TestConfiguration();
        
        config.algorithm = AnonymizationAlgorithm.OPTIMAL;
        config.dataset = BenchmarkDataset.ATUS;
        
        result.add(config);
        return result;
    }

}
