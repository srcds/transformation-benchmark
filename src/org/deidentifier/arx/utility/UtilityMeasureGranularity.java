package org.deidentifier.arx.utility;

import java.text.ParseException;
import java.util.Arrays;

import org.deidentifier.arx.utility.util.Functions;

public class UtilityMeasureGranularity extends UtilityMeasure<Double> {

	private final String[][] input;
    
    /**
     * Input: the original input dataset without header
     * @param input
     */
    public UtilityMeasureGranularity(String[][] input) {
        this.input = input;
    }
    
    
    /**
     * Output: the output dataset without header
     */
    @Override
	public Utility<Double> evaluate(String[][] output, int[] transformation) {
    	try {
            return new UtilityDouble(Functions.calculateGranularity(Arrays.asList(this.input), Arrays.asList(output)));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
	}
}
