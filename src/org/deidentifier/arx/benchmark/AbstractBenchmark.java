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
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

public abstract class AbstractBenchmark {

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
            executeTest(testConfiguration);
        }
    }
    
    public abstract List<TestConfiguration> generateTestConfigurations();
    
    protected void executeTest(TestConfiguration testConfig) throws IOException {
        
        ARXConfiguration config = ARXConfiguration.create();
        
        config.setQualityModel(testConfig.model);
        config.addPrivacyModel(new KAnonymity(testConfig.k));
        config.setSuppressionLimit(testConfig.supression);
        
        config.setAlgorithm(testConfig.algorithm);
        
        config.setGeneticAlgorithmIterations(testConfig.iterations);
        config.setGeneticAlgorithmSubpopulationSize(testConfig.subpopulationSize);
        config.setGeneticAlgorithmEliteFraction(testConfig.eliteFraction);
        config.setGeneticAlgorithmCrossoverFraction(testConfig.crossoverFraction);
        config.setGeneticAlgorithmMutationProbability(testConfig.mutationProbability);

        config.setHeuristicSearchStepLimit(testConfig.stepLimit);
        config.setHeuristicSearchTimeLimit(testConfig.timeLimit);

        if(testConfig.limitByOptimalLoss)
            findAndSetOptimum(testConfig);
                 
        Data input = getInputData(testConfig);
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        long time = System.currentTimeMillis();
        ARXResult result = anonymizer.anonymize(input, config);
        time = System.currentTimeMillis() - time;
        
        if(testConfig.writeToFile) {
            BENCHMARK.addRun(String.valueOf(testConfig.algorithm),
                             String.valueOf(testConfig.dataset),
                             String.valueOf(testConfig.k),
                             String.valueOf(testConfig.qids),
                             String.valueOf(testConfig.iterations),
                             String.valueOf(testConfig.timeLimit),
                             String.valueOf(testConfig.stepLimit),
                             String.valueOf(testConfig.limitByOptimalLoss),
                             String.valueOf(testConfig.batchNumber),
                             String.valueOf(time),
                             String.valueOf(result.getOutput().getStatistics().getQualityStatistics().getGranularity().getArithmeticMean()));
            BENCHMARK.getResults().write(file);
        }
    }
        
    private Data getInputData(TestConfiguration benchConfig) throws IOException {
        
        Integer key = benchConfig.hashInputConfig();
        
        if(!inputDataHM.containsKey(key)) {
            int qids = benchConfig.qids;
            if (qids == 0) {
                qids = BenchmarkSetup.getQuasiIdentifyingAttributes(benchConfig.dataset).length;
            }
            inputDataHM.put(key, BenchmarkSetup.getData(benchConfig.dataset, qids));
        }
        
        return inputDataHM.get(key);
    }
    
    private void findAndSetOptimum(TestConfiguration benchConfig) throws IOException {
        
        Integer key = benchConfig.hashObjective();
        
        if(!optimalLossHM.containsKey(key)) {
            AbstractAlgorithm.lossLimit = -1;
            
            ARXConfiguration config = ARXConfiguration.create();
            config.setQualityModel(benchConfig.model);
            config.addPrivacyModel(new KAnonymity(benchConfig.k));
            config.setSuppressionLimit(benchConfig.supression);
            config.setAlgorithm(AnonymizationAlgorithm.OPTIMAL);
            
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            ARXResult result = anonymizer.anonymize(getInputData(benchConfig), config);
            
            optimalLossHM.put(key, Double.valueOf(result.getGlobalOptimum().getHighestScore().toString()));
        }
        
        AbstractAlgorithm.lossLimit = optimalLossHM.get(key);                         
    }
    
    class TestConfiguration{

        int batchNumber = -1;
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
            return (int) (hashInputConfig() * model.hashCode() + k + supression);
        }

    }
    
}
