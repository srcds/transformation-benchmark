/**
 * Obtained from http://crises-deim.urv.cat/opendata/SPD_Science.zip
 */
package urv.crises.anonym;

import java.util.ArrayList;

/**
 * This class provides functions for distance calculation.
 * 
 * @author Sergio Martinez (Universitat Rovira i Virgili)
 */
public class Distances {
	
	static double typicalDev[];
	
	/**
	 * This function calculates the euclidean distance between two records.
	 * 
	 * @author Sergio Mart�nez (Universitat Rovira i Virgili)
	 */
	public static double euclideanDistNorm(String c1[], String c2[]){
		double dis, partial, partial1, cn1, cn2;
		int numAttr;
		
		numAttr = c2.length;
		dis = 0;
		partial = 0;
		for(int i=0; i<numAttr; i++){
			cn1 = Double.parseDouble(c1[i]);
			cn2 = Double.parseDouble(c2[i]);
			partial1 = (cn1-cn2) / typicalDev[i];
			partial += (partial1 * partial1);
		}
		dis = Math.sqrt(partial);
		return dis;
	}
	
	/**
	 * This function calculates the euclidean distance between a record
	 * and a generalized record.
	 * 
	 * @author Sergio Mart�nez (Universitat Rovira i Virgili)
	 */
	public static double euclideanDistNorm(String c1[], Record c2){
		double dis, partial, partial1, cn1, cn2;
		double min, max;
		int numAttrQuasi;
		
		numAttrQuasi = c1.length;
		dis = 0;
		partial = 0;
		for(int i=0; i<numAttrQuasi; i++){
			cn1 = Double.parseDouble(c1[i]);
			min = c2.attributes[i].value1;
			max = c2.attributes[i].value2;
			if((max - cn1) > (cn1 - min)){
				cn2 = max;
			}
			else{
				cn2 = min;
			}
			if(typicalDev[i] == 0.0){
				partial1 = 0.0;
			}
			else{
				partial1 = (cn1-cn2) / typicalDev[i];
			}
			partial += (partial1 * partial1);
		}
		dis = Math.sqrt(partial);
		return dis;
	}
	
	/**
	 * This function calculates the typical deviation of all attributes
	 * 
	 * @author Sergio Mart�nez (Universitat Rovira i Virgili)
	 */
	public static void calculateTypicalDeviations(ArrayList<String[]>data){
		double var[];
		int numAttr;
		double max[];
		double min[];
		
		numAttr = data.get(0).length;
		typicalDev = new double[numAttr];
		max = new double[numAttr];
		min = new double[numAttr];
		
		for(int i=0; i<numAttr; i++){
			var = new double[data.size()];
			max[i] = Double.parseDouble(data.get(0)[i]);
			min[i] = Double.parseDouble(data.get(0)[i]);
			for(int j=0; j<data.size(); j++){
				var[j] = Double.parseDouble(data.get(j)[i]);
				if(var[j] > max[i]){
					max[i] = var[j];
				}
				if(var[j] < min[i]){
					min[i] = var[j];
				}
			}
			typicalDev[i] = calculateTypicalDeviation(var);
		}
	}
	
	/**
	 * This function calculates the typical deviation of an attribute
	 * 
	 * @author Sergio Mart�nez (Universitat Rovira i Virgili)
	 */
	private static double calculateTypicalDeviation(double var[]){
		double tipicalDev, medianVar, partial;
		
		medianVar = 0;
		for(int i=0; i<var.length; i++){
			medianVar += var[i];
		}
		medianVar /= var.length;
		
		tipicalDev = 0;
		for(int i=0; i<var.length; i++){
			partial = var[i] - medianVar;
			partial = partial * partial;
			tipicalDev += partial;
		}
		tipicalDev /= (var.length - 1);
		tipicalDev = Math.sqrt(tipicalDev);
		
		return tipicalDev;
	}

}
