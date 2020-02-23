package org.deidentifier.arx.benchmark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkAlgorithm;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkQualityModel;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkTransformationModel;
import org.deidentifier.arx.exceptions.RollbackRequiredException;

import de.linearbits.subframe.Benchmark;
import de.linearbits.subframe.analyzer.ValueBuffer;

/**
 * Main benchmark class.
 * 
 * @author Fabian Prasser
 */
public class BenchmarkExperiment {

    /** The benchmark instance */
    private static final Benchmark BENCHMARK = new Benchmark(new String[] { "Dataset", "k", "QIs", "Algorithm", "Quality", "Transformation" });

    /** TIME */
    public static final int        TIME      = BENCHMARK.addMeasure("Time");

    /** UTILITY */
    public static final int        UTILITY   = BENCHMARK.addMeasure("Utility");

    /** FILE */
    private static File            FILE      = new File("results/arx_ga.csv");

	
    
	
	
    /**
     * Main entry point
     * 
     * @param args
     * @throws IOException
     * @throws RollbackRequiredException 
     */
    public static void main(String[] args) throws IOException, RollbackRequiredException {

        // Init
        BENCHMARK.addAnalyzer(TIME, new ValueBuffer());
        BENCHMARK.addAnalyzer(UTILITY, new ValueBuffer());
        
        // Standard all config
        BenchmarkDataset[] datasets = new BenchmarkDataset[] {  BenchmarkDataset.ADULT,
                                                                BenchmarkDataset.CUP,
                                                                BenchmarkDataset.FARS,
                                                                BenchmarkDataset.ATUS,
                                                                BenchmarkDataset.IHIS,
                                                                BenchmarkDataset.SS13ACS};
        
        BenchmarkTransformationModel[] transformations = new BenchmarkTransformationModel[] {
                                                                                             BenchmarkTransformationModel.MULTI_DIMENSIONAL_GENERALIZATION,
                                                                                             BenchmarkTransformationModel.LOCAL_GENERALIZATION,
                                                                                             };
        
        BenchmarkAlgorithm[] algorithms = new BenchmarkAlgorithm[] {    //BenchmarkAlgorithm.SANCHEZ,
                                                                        BenchmarkAlgorithm.ARX
                                                                        //BenchmarkAlgorithm.MONDRIAN
                                                                        };
        
        //int[] ks = new int[] {2, 3, 5, 10};
        int[] ks = new int[] {5};
        
        int[] vals = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        

        
        // Read from command line
        if (args != null && args.length > 0) {
            datasets = new BenchmarkDataset[]{BenchmarkDataset.valueOf(args[0])};
            transformations = new BenchmarkTransformationModel[]{BenchmarkTransformationModel.valueOf(args[1])};
            algorithms = new BenchmarkAlgorithm[]{BenchmarkAlgorithm.valueOf(args[2])};
            ks = new int[]{Integer.valueOf(args[3])};
            vals = new int[]{Integer.valueOf(args[4])};
            //FILENAME = args[5];
        }
        
        // For each data set
        for (BenchmarkDataset dataset : datasets) {

            // For each transformation model
			for (BenchmarkTransformationModel transformation : transformations) {
            	
            	  // For each algorithm
				for (BenchmarkAlgorithm algorithm : algorithms) {
            	
                    // Run
            		for (int k : ks) {

                        // Run
                        for (int val : vals) {
                            benchmark(dataset, transformation, algorithm, val, k);
                        }
            		}
            	}
            }
        }
    }
    
    /**
     * Performs the benchmark
     * @param dataset
     * @param transformation
     * @param algorithm
     * @param quality
     * @param qis
     * @param k
     * @throws IOException
     * @throws RollbackRequiredException
     */
    private static void benchmark(BenchmarkDataset dataset, BenchmarkTransformationModel transformation,
								  BenchmarkAlgorithm algorithm, int qis, int k) throws IOException, RollbackRequiredException {

        // Forbidden combinations
        if (transformation == BenchmarkTransformationModel.LOCAL_GENERALIZATION) {
            if (algorithm == BenchmarkAlgorithm.MONDRIAN) {
            	// Only for ARX and Sanchez
            	return;
            }
        }
        if (transformation == BenchmarkTransformationModel.MULTI_DIMENSIONAL_GENERALIZATION) {
            if (algorithm == BenchmarkAlgorithm.SANCHEZ) {
            	// Only for ARX and Mondrian
            	return;
            }
        }
        
    	System.out.println(String.valueOf(dataset) + "/" + String.valueOf(k) + "/" + String.valueOf(qis) + "/" + String.valueOf(algorithm) + "/" + String.valueOf(transformation));
        
		// Perform
		BenchmarkExecutor executor;
		switch (algorithm) {
		case ARX:
			executor = new BenchmarkExecutorARX(dataset, transformation, qis, k, BenchmarkSetup.TIME_LIMIT);
			break;
		case MONDRIAN:
			executor = new BenchmarkExecutorMondrian(dataset, transformation, qis, k, BenchmarkSetup.TIME_LIMIT);
			break;
		case SANCHEZ:
			executor = new BenchmarkExecutorSanchez(dataset, transformation, qis, k, BenchmarkSetup.TIME_LIMIT);
			break;
		default:
			throw new IllegalArgumentException("Unknown algorithm");
		}
		
		// Prepare
		executor.prepare();

		// Anonymize
		double time = 0d;
		try {
			long start = System.currentTimeMillis();
			executor.anonymize();
			time = System.currentTimeMillis() - start;
	    } catch (Exception e) {
	    	
			// Done
			return;
	    }

		// Analyze
        Map<BenchmarkQualityModel, Double> utility = executor.analyze();
        if (utility == null) {

            // Done
            return;
        }
        
        // Store
        for (Entry<BenchmarkQualityModel, Double> entry : utility.entrySet()) {
			BENCHMARK.addRun(String.valueOf(dataset), String.valueOf(k), String.valueOf(qis), String.valueOf(algorithm), String.valueOf(entry.getKey().toString()), String.valueOf(transformation));
	        BENCHMARK.addValue(TIME, time);
	        BENCHMARK.addValue(UTILITY, entry.getValue());
        }
		
		// Save
		BENCHMARK.getResults().write(FILE);
	}
}
