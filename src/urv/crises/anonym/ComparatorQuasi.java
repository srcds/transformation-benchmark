/**
 * Obtained from http://crises-deim.urv.cat/opendata/SPD_Science.zip
 */
package urv.crises.anonym;

import java.util.Comparator;

/**
 * This class compare two records based on their quasi-identifier
 * attributes.
 * 
 * @author Sergio Martinez (Universitat Rovira i Virgili)
 */
public class ComparatorQuasi implements Comparator<Record>{
	
	static String zero[];
	static int numAttri;
	
	public static void setAttributeSortCriteria(int numAttr){
		numAttri = numAttr;
		zero = new String[numAttr];
		for(int i=0; i<zero.length; i++){
			zero[i] = "0";
		}
	}
	
	public int compare(Record o1, Record o2) {
		String str1[], str2[];
		
		str1 = o1.getAttr();
		str2 = o2.getAttr();
		
		double dis1 = Distances.euclideanDistNorm(str1, zero);
		double dis2 = Distances.euclideanDistNorm(str2, zero);
		
		if(dis1 > dis2){
			return 1;
		}
		if(dis1 < dis2){
			return -1;
		}
		
		return 0;
	}

}
