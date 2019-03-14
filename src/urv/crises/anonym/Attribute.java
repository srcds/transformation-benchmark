/**
 * Obtained from http://crises-deim.urv.cat/opendata/SPD_Science.zip
 */
package urv.crises.anonym;

/**
 * This class represents an attribute.
 * 
 * @author Sergio Martinez (Universitat Rovira i Virgili)
 */
public class Attribute {

	String value;
	int value1;
	int value2;

	/**
	   * The constructor for this attribute.
	   * 
	   * @param String the attribute value
	   */
	public Attribute(String value) {
		int v;

		this.value = value;
		try {
			v = (int)Double.parseDouble(value);
			this.value1 = v;
			this.value2 = v;
		} catch (NumberFormatException e) {
			this.value1 = 0;
			this.value2 = 0;
		}
	}

}
