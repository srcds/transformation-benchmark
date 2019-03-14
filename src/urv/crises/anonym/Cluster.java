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
public class Cluster {
	private ArrayList<Record>elements;
	private Record centroid;

	public Cluster() {
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
		String valor;
		int minimo;
		
		valor = elements.get(0).attributes[attr].value;
		minimo = Integer.parseInt(valor);
		for(Record reg:elements){
			valor = reg.attributes[attr].value;
			if(Integer.parseInt(valor) < minimo){
				minimo = Integer.parseInt(valor);
			}
		}
		return minimo;
	}
	
	private int calculateMax(int attr){
		String valor;
		int maximo;
		
		valor = elements.get(0).attributes[attr].value;
		maximo = Integer.parseInt(valor);
		for(Record reg:elements){
			valor = reg.attributes[attr].value;
			if(Integer.parseInt(valor) > maximo){
				maximo = Integer.parseInt(valor);
			}
		}
		return maximo;
	}
}
