/**
 * Obtained from http://crises-deim.urv.cat/opendata/SPD_Science.zip
 */
package urv.crises.anonym;

import java.util.Comparator;

/**
 * This class compare two records based on their sensitive
 * attributes.
 * 
 * @author Sergio Martinez (Universitat Rovira i Virgili)
 */
public class ComparatorSensitive implements Comparator<Record>{
	static int attrSortCriteria;
	int res;
	
	public static void setAttributeSortCriteria(int attr){
		attrSortCriteria = attr;
	}
	
	public int compare(Record o1, Record o2) {
		String v1, v2;
		
		v1 = o1.getAttr()[attrSortCriteria];
		v2 = o2.getAttr()[attrSortCriteria];
		res = Integer.parseInt(v1) - Integer.parseInt(v2);
		return res;
	}
}
