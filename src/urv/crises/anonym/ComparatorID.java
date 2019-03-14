/**
 * Obtained from http://crises-deim.urv.cat/opendata/SPD_Science.zip
 */
package urv.crises.anonym;

import java.util.Comparator;

/**
 * This class compare two records based on their ID.
 * 
 * @author Sergio Martinez (Universitat Rovira i Virgili)
 */
public class ComparatorID implements Comparator<Record>{
	
	public int compare(Record o1, Record o2) {
		return o1.id - o2.id;
	}


}
