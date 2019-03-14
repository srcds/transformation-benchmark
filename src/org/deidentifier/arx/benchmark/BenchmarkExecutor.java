package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkQualityModel;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkTransformationModel;
import org.deidentifier.arx.exceptions.RollbackRequiredException;

public abstract class BenchmarkExecutor {
	
	/** Dataset*/
	protected final BenchmarkDataset dataset;
	/** Transformation*/
	protected final BenchmarkTransformationModel transformation;
	/** QIs*/
	protected final int qis;
	/** K*/
	protected final int k;
	/** Timelimit*/
	protected final int timeLimit;
	
	/**
	 * Creates a new instance
	 * @param dataset
	 * @param transformation
	 * @param quality
	 * @param qis
	 * @param k
	 * @param timeLimit
	 */
	public BenchmarkExecutor(BenchmarkDataset dataset, BenchmarkTransformationModel transformation, int qis, int k, int timeLimit) {
		this.dataset = dataset;
		this.transformation = transformation;
		this.qis = qis;
		this.k = k;
		this.timeLimit = timeLimit;
	}
	
	/**
	 * Return utility
	 * @throws RollbackRequiredException
	 * @throws IOException
	 */
	public abstract Map<BenchmarkQualityModel, Double> analyze() throws RollbackRequiredException, IOException;
	
	/**
	 * Anonymize
	 * @throws RollbackRequiredException
	 * @throws IOException
	 * @throws TimeoutException 
	 */
	public abstract void anonymize() throws RollbackRequiredException, IOException, BenchmarkTimeoutException;
	
	/**
     * Return map containing 0 utility
     * @return
     */
	public abstract Map<BenchmarkQualityModel, Double> getTimeoutResult();

    /**
	 * Prepare
	 * @throws RollbackRequiredException
	 * @throws IOException
	 */
	public abstract void prepare() throws RollbackRequiredException, IOException;

    /**
     * Returns a suppressed dataset
     * @param dataset
     * @return
     */
    protected String[][] suppress(String[][] dataset) {
        String[][] result = new String[dataset.length][];
        String[] suppressed = new String[dataset[0].length];
        Arrays.fill(suppressed, "*");
        for (int i = 0; i < dataset.length; i++) {
            result[i] = suppressed;
        }
        return result;
    }
}
