/**
 * Obtained from http://crises-deim.urv.cat/opendata/SPD_Science.zip
 */
package urv.crises.anonym;

public class Record {
	
	Attribute attributes[];
	static int numAttr;
	int id;
	
	public Record(int id) {
		attributes = new Attribute[numAttr];
		this.id = id;
	}
	
	public String[] getAttr(){
		String str[];
		
		str = new String[numAttr];
		
		for(int i=0; i<numAttr; i++){
			str[i] = attributes[i].value;
		}
		
		return str;
	}
	
	public String toString(){
		String s;
		
		s = "";
		for(int i=0; i<numAttr; i++){
			if(attributes[i] != null){
				s += attributes[i].value;
			}
			s += ",";
		}
		s = s.substring(0, s.length()-1);
		return s;
	}
	
	public String getLower(int i) {
	    return String.valueOf(attributes[i].value1);
	}
	
	public String getUpper(int i) {
        return String.valueOf(attributes[i].value2);
    }

}
