package org.deidentifier.arx.utility.util;

import java.text.ParseException;
import java.util.List;

/**
 * This class provides functions for anonymization. Adopted for ARX.
 * 
 * @author Sergio Martinez (Universitat Rovira i Virgili)
 */
public class Functions {
	
	/**
	 * This function calculates the information loss of the anonymized with
	 * respect to the original dataset.
	 * 
	 * @param input
	 * @param output
	 * @return the information loss
	 * @throws ParseException 
	 */
	public static double calculateIL(List<String[]> input, List<String[]> output) throws ParseException {
		double IL = 0;
		Distances distances = new Distances(input);
		for(int i=0; i<input.size(); i++){
			IL += distances.euclideanDistNorm(input.get(i), output.get(i));
		}
		IL /= distances.getNumAttributes();
		IL /= input.size();
		return IL / distances.getMaxEuclideanDistance();
	}

    /**
     * This function calculates the information loss of the anonymized with
     * respect to the original dataset.
     * 
     * @param input
     * @param output
     * @return the information loss
     * @throws ParseException 
     */
    public static double calculateGranularity(List<String[]> input, List<String[]> output) throws ParseException {
    	
        double IL = 0;
        Distances distances = new Distances(input);
        for(int i=0; i<input.size(); i++){
            IL += distances.granularity(output.get(i));
        }
        IL /= distances.getNumAttributes();
        IL /= input.size();
        return IL;
    }

    /**
     * This function calculates the relative average equivalence class size
     * 
     * @param input
     * @param output
     * @return the information loss
     * @throws ParseException 
     */
    public static double calculateAverageClassSize(List<String[]> input, List<String[]> output) throws ParseException {
        Distances distances = new Distances(input);
        double minAECS = distances.averageClassSize(input);
        double maxAECS = input.size();
        double outputAECS = distances.averageClassSize(output);
        return (outputAECS - minAECS) / (maxAECS - minAECS);
    }
}
