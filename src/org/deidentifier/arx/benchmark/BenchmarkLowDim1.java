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
 *  Used to perform benchmarks on low/medium dim datasets.
 *  The utility is only measured once after reaching predefined time limits.
 */
public class BenchmarkLowDim1 extends AbstractBenchmark{

    BenchmarkLowDim1(String fileName) {
        super(fileName, true, false);
    }
    
    public static void main(String args[]) throws IOException {
        new BenchmarkLowDim1("NEW_results_IHIS_GA_WAN_MP03.csv").start();
    }

    @Override
    public void generateTestConfigurations(List<TestConfiguration> testConfigs) {

        
        // Definition of properties that will be varied for the Benchmark
        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.IHIS };
        
        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] { 
                                                                             AnonymizationAlgorithm.BEST_EFFORT_GENETIC,
                                                                              };
        
        // number of testruns
        int testRuns = 51;
        
        // iterate through all possible configuration permutations
        for (BenchmarkDataset dataset : datasets) {
            for (int testRun = 0; testRun < testRuns; testRun++) {
                for (AnonymizationAlgorithm algorithm : algorithms) {

                    TestConfiguration testConfig = new TestConfiguration();

                    testConfig.algorithm = algorithm;
                    testConfig.dataset = dataset;
                    testConfig.testRunNumber = testRun;
                    
                    testConfig.crossoverFraction = 0.4;
                    testConfig.mutationProbability = 0.3;
                    testConfig.timeLimit = 10000;

                    if (testRun == 0) testConfig.writeToFile = false;

                    testConfigs.add(testConfig);
                }
            }
        }

    }

}
