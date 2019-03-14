/**
 * Obtained from http://crises-deim.urv.cat/opendata/SPD_Science.zip
 */
package urv.crises.anonym;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeConverter {
	
	private Map<String, String> intervalToValue = new HashMap<>();
	private Map<String, Integer> intervalIndex = new HashMap<>();
	
	private Map<String, String> valueToInterval = new HashMap<>();
	
	public AttributeConverter(List<String> intervals) {
		for(String interval : intervals) {
			int value = (int)parse(interval);
			
			String interval2 = interval;
			if(interval.contains(" ")) {
				interval2 = "\"" + interval + "\"";		// appending char " at the begin and end of the interval string
			}
			intervalToValue.put(interval2, String.valueOf(value));
			intervalIndex.put(interval2,  intervalIndex.size());
			valueToInterval.put(String.valueOf(value), interval2);
			//System.out.println("Converting '" + interval + "' (" + interval2 + ") to '" + String.valueOf(value) + "'");
			intervalIndex.put(interval2,  intervalIndex.size());
		}
	}
	
	private double parse(String interval) {
		double value = 0d;
		
		// strip of " characters
		if(interval.startsWith("[") && interval.endsWith("["))
		//interval.substring(interval.indexOf("\"") + 1, interval.lastIndexOf("\""));
		interval = interval.trim();
		
		if (interval.startsWith(">=")) {
			value = Double.valueOf(interval.substring(2, interval.length()).trim());
		}
		else if (interval.startsWith("[") && interval.endsWith("[")) {
			interval = interval.substring(1, interval.length() - 1);		// remove leading and ending characters
			String[] s = interval.split(",");
			if (s.length != 2) {
				throw new IllegalStateException("Cannot parse, expected interval: " + interval);
			}
			
			try {
				double v1 = Double.valueOf(s[0].trim());
				double v2 = Double.valueOf(s[1].trim());
				value = (v1 + v2) / 2.0d;		// get middle value
			}
			catch(NullPointerException e)  {
				throw new IllegalStateException("Cannot parse: " + interval);
			}
		}
		else {
			throw new IllegalStateException("Cannot parse: " + interval);
		}
		
		return value;
	}
	
	public String getValueFromInterval(String interval) {
		if(this.intervalToValue.containsKey(interval)) {
			return this.intervalToValue.get(interval);
		}
		
		return null;
	}
	
	public String getIntervalOfValue(String value) {
		if(this.valueToInterval.containsKey(value)) {
			return this.valueToInterval.get(value);
		}
		
		return null;
	}
	
	public String getIndexFromInterval(String interval) {
		if(this.intervalIndex.containsKey(interval)) {
			return String.valueOf(this.intervalIndex.get(interval));
		}
		
		return null;
	}

}
