package org.deidentifier.arx.benchmark;

import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;

import de.linearbits.subframe.Benchmark;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.algorithm.AbstractAlgorithm;
import org.deidentifier.arx.algorithm.AbstractAlgorithm.TimeUtilityTuple;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

public abstract class AbstractBenchmark {
    
    public static final boolean writeAllTrackedOptimums = true;

    private static final Benchmark BENCHMARK = new Benchmark(new String[] { "algorithm",
                                                                            "dataset",
                                                                            "k",
                                                                            "qids",
                                                                            "iterations",
                                                                            "timeLimit",
                                                                            "stepLimit",
                                                                            "limitByOptimalLoss",
                                                                            "batchNumber",
                                                                            "time",
                                                                            "utility"
                                                                            });
    File file;
    
    private HashMap<Integer, Data> inputDataHM = new HashMap<Integer,Data>();
    private HashMap<Integer, Double> optimalLossHM = new HashMap<Integer, Double>();
    
    AbstractBenchmark(String fileName){
        file = new File(fileName);
    }
    
    public void start() throws IOException {
        List<TestConfiguration> testConfigurations = generateTestConfigurations(); 
        for(TestConfiguration testConfiguration : testConfigurations) {
            System.out.println(testConfiguration);
            executeTest(testConfiguration);
        }
    }
    
    public abstract List<TestConfiguration> generateTestConfigurations();
    
    protected void executeTest(TestConfiguration testConfiguration) throws IOException {
        
        // reset lossLimit to avoid side effects
        AbstractAlgorithm.lossLimit = -1;
        
        // Copy benchmark config to arx config
        ARXConfiguration arxConfiguration = ARXConfiguration.create();
        arxConfiguration.setQualityModel(testConfiguration.model);
        arxConfiguration.addPrivacyModel(new KAnonymity(testConfiguration.k));
        arxConfiguration.setSuppressionLimit(testConfiguration.supression);
        arxConfiguration.setAlgorithm(testConfiguration.algorithm);
        arxConfiguration.setGeneticAlgorithmIterations(testConfiguration.iterations);
        arxConfiguration.setGeneticAlgorithmSubpopulationSize(testConfiguration.subpopulationSize);
        arxConfiguration.setGeneticAlgorithmEliteFraction(testConfiguration.eliteFraction);
        arxConfiguration.setGeneticAlgorithmCrossoverFraction(testConfiguration.crossoverFraction);
        arxConfiguration.setGeneticAlgorithmMutationProbability(testConfiguration.mutationProbability);
        arxConfiguration.setHeuristicSearchStepLimit(testConfiguration.stepLimit);
        arxConfiguration.setHeuristicSearchTimeLimit(testConfiguration.timeLimit);

        // find and set optimum as stop limit
        if(testConfiguration.limitByOptimalLoss)
            findAndSetOptimum(testConfiguration);
                 
        // load data
        Data input = getInputData(testConfiguration);
        
        // Init and start anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        long time = System.currentTimeMillis();
        ARXResult result = anonymizer.anonymize(input, arxConfiguration);
        time = System.currentTimeMillis() - time;
        
        // write result
        if (!writeAllTrackedOptimums) {
            if (testConfiguration.writeToFile) {
                BENCHMARK.addRun(String.valueOf(testConfiguration.algorithm),
                                 String.valueOf(testConfiguration.dataset),
                                 String.valueOf(testConfiguration.k),
                                 String.valueOf(testConfiguration.qids),
                                 String.valueOf(testConfiguration.iterations),
                                 String.valueOf(testConfiguration.timeLimit),
                                 String.valueOf(testConfiguration.stepLimit),
                                 String.valueOf(testConfiguration.limitByOptimalLoss),
                                 String.valueOf(testConfiguration.testRunNumber),
                                 String.valueOf(time),
                                 String.valueOf(result.getOutput()
                                                      .getStatistics()
                                                      .getQualityStatistics()
                                                      .getGranularity()
                                                      .getArithmeticMean()));
                BENCHMARK.getResults().write(file);
            }
        } else {
            List<TimeUtilityTuple> trackedOptimums = AbstractAlgorithm.getTrackedOptimums();
            for(TimeUtilityTuple trackedOptimum :  trackedOptimums) {
                BENCHMARK.addRun(String.valueOf(testConfiguration.algorithm),
                                 String.valueOf(testConfiguration.dataset),
                                 String.valueOf(testConfiguration.k),
                                 String.valueOf(testConfiguration.qids),
                                 String.valueOf(testConfiguration.iterations),
                                 String.valueOf(testConfiguration.timeLimit),
                                 String.valueOf(testConfiguration.stepLimit),
                                 String.valueOf(testConfiguration.limitByOptimalLoss),
                                 String.valueOf(testConfiguration.testRunNumber),
                                 String.valueOf(trackedOptimum.getTime()),
                                 String.valueOf(trackedOptimum.getUtility()));
                BENCHMARK.getResults().write(file);
                
            }
        }
    }
        
    private Data getInputData(TestConfiguration benchConfig) throws IOException {

        int qids = benchConfig.qids;
        if (qids == 0) {
            qids = BenchmarkSetup.getQuasiIdentifyingAttributes(benchConfig.dataset).length;
        }

        return BenchmarkSetup.getData(benchConfig.dataset, qids);
    }
    
    @Deprecated
    // Broken idea as an Data object is changed when its used for anonymization
    private Data getInputDataFromHashMap(TestConfiguration testConfiguration) throws IOException {
        
        Integer key = testConfiguration.hashInputConfig();
        
        if(!inputDataHM.containsKey(key)) {
            int qids = testConfiguration.qids;
            if (qids == 0) {
                qids = BenchmarkSetup.getQuasiIdentifyingAttributes(testConfiguration.dataset).length;
            }
            System.out.println("Created new InputData (" + String.valueOf(testConfiguration.dataset) + " , qids=" + qids + ") with key " + key);
            inputDataHM.put(key, BenchmarkSetup.getData(testConfiguration.dataset, qids));
        }
        
        return inputDataHM.get(key);
    }
    
    private void findAndSetOptimum(TestConfiguration testConfiguration) throws IOException {
        
        Integer key = testConfiguration.hashObjective();
        
        if(!optimalLossHM.containsKey(key)) {

            ARXConfiguration config = ARXConfiguration.create();
            config.setQualityModel(testConfiguration.model);
            config.addPrivacyModel(new KAnonymity(testConfiguration.k));
            config.setSuppressionLimit(testConfiguration.supression);
            config.setAlgorithm(AnonymizationAlgorithm.OPTIMAL);
            
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            ARXResult result = anonymizer.anonymize(getInputData(testConfiguration), config);
            
            optimalLossHM.put(key, Double.valueOf(result.getGlobalOptimum().getHighestScore().toString()));
            System.out.println("Created new Optimum (" + String.valueOf(testConfiguration.dataset) + " , qids=" + testConfiguration.qids + ") with key " + key);
        }
        
        AbstractAlgorithm.lossLimit = optimalLossHM.get(key);                         
    }
    
    class TestConfiguration{

        int testRunNumber = -1;
        boolean writeToFile = true;
        
        //Anonymization requirements and metrics
        final Metric<?>        model               = Metric.createLossMetric(0.5, AggregateFunction.ARITHMETIC_MEAN);
        int                    k                   = 5;
        double                 supression          = 1d;

        // Used algorithm
        AnonymizationAlgorithm algorithm;

        // GA specific settings
        int                    subpopulationSize   = 100;
        int                    iterations          = 50;
        double                 eliteFraction       = 0.2;
        double                 crossoverFraction   = 0.2;
        double                 mutationProbability = 0.2;

        // Limits (GA and LIGHTNING)
        int                    timeLimit           = Integer.MAX_VALUE;
        int                    stepLimit           = Integer.MAX_VALUE;
        boolean                limitByOptimalLoss  = false;

        // Input configuration
        BenchmarkDataset       dataset;
        int                    qids                = 0;
        
        Integer hashInputConfig() {
            return (int) (dataset.hashCode() + qids);
        }
        
        Integer hashObjective() {
            return (int) (hashInputConfig() + k + supression);
        }
        
        @Override
        public String toString() {
            String output = String.format("%s | %s | k=%d | qids=%d | RunNumber=%d", algorithm, dataset, k, qids, testRunNumber);
            return output;
        }

    }
    
}
