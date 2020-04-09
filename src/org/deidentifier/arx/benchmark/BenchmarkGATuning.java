package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

public class BenchmarkGATuning extends AbstractBenchmark{

    BenchmarkGATuning(String fileName) {
        super(fileName, true, false);
    }

    public static void main(String args[]) throws IOException {
        new BenchmarkGATuning("results/results_gaTuning_tracked.csv").start();
        
    }

    @Override
    public void generateTestConfigurations(List<TestConfiguration> testConfigs) {


        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] { AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_GENETIC,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN };
        // AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[]
        // {AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP};

        double[] eliteFractions = new double[] { 0.1, 0.2, 0.3 };
        double[] crossoverFractions = new double[] { 0.1, 0.2, 0.3 };
        double[] mutationProbabilities = new double[] {0.1, 0.2, 0.3};

        int testRuns = 6;

        for (int testRun = 0; testRun < testRuns; testRun++) {
            for (double eliteFraction : eliteFractions) {
                for (double crossoverFraction : crossoverFractions) {
                    for (double mutationProbability : mutationProbabilities) {

                        TestConfiguration testConfig = new TestConfiguration();

                        testConfig.algorithm = AnonymizationAlgorithm.BEST_EFFORT_GENETIC;

                        testConfig.timeLimit = 100000;
                        testConfig.dataset = BenchmarkDataset.CHRONIC2010;
                        testConfig.testRunNumber = testRun;

                        testConfig.eliteFraction = eliteFraction;
                        testConfig.crossoverFraction = crossoverFraction;
                        testConfig.mutationProbability = mutationProbability;

                        if (testRun == 0) {
                            testConfig.writeToFile = false;
                            testConfig.timeLimit = 5000;
                        }

                        testConfigs.add(testConfig);
                    }
                }
            }
        }

    }

}
