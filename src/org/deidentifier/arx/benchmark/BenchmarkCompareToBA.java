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
public class BenchmarkCompareToBA extends AbstractBenchmark{

    BenchmarkCompareToBA(String fileName) {
        super(fileName, true, false);
    }
    
    public static void main(String args[]) throws IOException {
        new BenchmarkCompareToBA("results_compare_to_ba_0sup_v2.csv").start();
    }

    @Override
    public void generateTestConfigurations(List<TestConfiguration> testConfigs) {

        
        // Definition of properties that will be varied for the Benchmark
        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.ADULT,
                                                               
                                                               BenchmarkDataset.FARS,
                                                               BenchmarkDataset.ATUS,
                                                               BenchmarkDataset.IHIS, 
                                                               BenchmarkDataset.SS13ACS};
        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] { 
                                                                             AnonymizationAlgorithm.BEST_EFFORT_GENETIC,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN };
        
        // number of testruns
        int testRuns = 3;
        
        // iterate through all possible configuration permutations
        for (BenchmarkDataset dataset : datasets) {
            for (int testRun = 0; testRun < testRuns; testRun++) {
                for (AnonymizationAlgorithm algorithm : algorithms) {

                    TestConfiguration testConfig = new TestConfiguration();

                    testConfig.algorithm = algorithm;
                    testConfig.dataset = dataset;
                    testConfig.testRunNumber = testRun;
                    
                    //testConfig.subpopulationSize = 50;
                    testConfig.supression = 0d;

                    if (algorithm.equals(AnonymizationAlgorithm.BEST_EFFORT_GENETIC)) {
                        testConfig.gaIterations = 50;
                    } else {
                        testConfig.stepLimit = 100;
                    }
                       
                    
                    if (testRun == 0) testConfig.writeToFile = false;

                    testConfigs.add(testConfig);
                }
            }
        }

    }

}
