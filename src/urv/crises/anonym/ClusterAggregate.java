/**
 * Obtained from http://crises-deim.urv.cat/opendata/SPD_Science.zip
 */
package urv.crises.anonym;

import java.util.ArrayList;

/**
 * This class represents a cluster.
 * 
 * @author Sergio Martinez (Universitat Rovira i Virgili)
 */
public class ClusterAggregate {
	private ArrayList<Record>elements;
	private Record centroid;

	public ClusterAggregate() {
		this.elements = new ArrayList<>();
		this.centroid = null;
	}
	
	public void add(Record reg){
		this.elements.add(reg);
	}
	
	public void clear(){
		this.elements.clear();
	}
	
	public ArrayList<Record> getElements(){
		return this.elements;
	}

	public int getNumReg() {
		return elements.size();
	}

	public Record getCentroid(){
		if(centroid == null){
			calculateCentroid();
		}
		return centroid;
	}
	
	public void calculateCentroid(){
		Attribute atribute;
		int min, max;
		String newValue;
		int numAttr;
		
		numAttr = elements.get(0).attributes.length;
		centroid = new Record(0);
		for(int i=0; i<Record.numAttr; i++){
			atribute = new Attribute("0");
			centroid.attributes[i] = atribute;
		}
		for(int i=0; i<numAttr; i++){
			min = calculateMin(i);
			max = calculateMax(i);
			centroid.attributes[i].value1 = min;
			centroid.attributes[i].value2 = max;
			newValue = String.valueOf(((double)(max + min) / 2.0));
			centroid.attributes[i].value = newValue;
		}
	}
	
	private int calculateMin(int attr){
		return calculateAggregate(attr);
	}
	
	private int calculateMax(int attr){
		return calculateAggregate(attr);
	}
	
	private int calculateAggregate(int attr) {
		int mean = 0;
		
		for(Record reg:elements){
			mean += Integer.parseInt(reg.attributes[attr].value);
		}
		return (mean / elements.size());
	}
}
