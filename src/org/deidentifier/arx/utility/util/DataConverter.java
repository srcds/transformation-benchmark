/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx.utility.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.io.CSVSyntax;

import urv.crises.anonym.Record;

public class DataConverter {
    
    /**
     * Returns the header including QIs only
     * @param input
     * @return
     */
    public String[] getHeader(Data input) {
        DataHandle handle = input.getHandle();
        Set<String> qis = input.getDefinition().getQuasiIdentifyingAttributes();
        List<String> header = new ArrayList<>();
        for (int i = 0; i < handle.getNumColumns(); i++) {
            if (qis.contains(handle.getAttributeName(i))) {
                header.add(handle.getAttributeName(i));
            }
        }
        return header.toArray(new String[header.size()]);
    }
    
    /**
     * Returns the header
     * @param handle
     * @return
     */
    public String[] getHeader(DataHandle handle) {
        String[] header = new String[handle.getNumColumns()];
        for (int i = 0; i < header.length; i++) {
            header[i] = handle.getAttributeName(i);
        }
        return header;
    }

    /**
     * This function transformes an anonymized dataset
     * 
     * @param data, the anonymized dataset
     * @param header, the header
     */
    public String[][] toArray(ArrayList<Record> data, String[] header) {

        List<String[]> result = new ArrayList<>();
        for (Record rec : data) {
            String[] array = new String[header.length];
            for (int i = 0; i < header.length; i++) {
                String lower = String.valueOf(rec.getLower(i));
                String upper = String.valueOf(rec.getUpper(i));
                array[i] = "[" + lower + ";" + upper + "]";
            }
            result.add(array);
        }
        return result.toArray(new String[result.size()][]);
    }
    
    /**
     * Returns an array representation of the dataset containing only data of QIs
     * @param input
     * @return
     */
    public String[][] toArray(Data input) {
        DataHandle handle = input.getHandle();
        Set<String> qis = input.getDefinition().getQuasiIdentifyingAttributes();
        return toArray(handle, qis);
    }
    
    /**
     * Returns an array representation of the dataset containing only data of QIs
     * @param handle
     * @return
     */
    public String[][] toArray(DataHandle handle) {
        Set<String> qis = handle.getDefinition().getQuasiIdentifyingAttributes();
        return toArray(handle, qis);
    }
    
    /**
     * Returns an array representation of the dataset. Extracts all attributes that are defined
     * as quasi-identifiers in the given definition
     * 
     * @param handle
     * @param definition
     * @return
     */
    public String[][] toArray(DataHandle handle, DataDefinition definition) {
        
        List<Integer> indices = new ArrayList<Integer>();
        for (String attribute : definition.getQuasiIdentifyingAttributes()) {
            indices.add(handle.getColumnIndexOf(attribute));
        }
        
        List<String[]> list = new ArrayList<String[]>();
        Iterator<String[]> iter = handle.iterator();
        iter.next(); // Skip header
        for (; iter.hasNext();) {
            String[] input = iter.next();
            String[] output = new String[indices.size()];
            int i = 0;
            for (int index : indices) {
                output[i++] = input[index];
            }
            list.add(output);
        }
        return list.toArray(new String[list.size()][]);
    }
    
    /**
     * Returns an array representation of the subset, in which all rows that are not part of the
     * subset have been suppressed. This method does *not* preserve the order of tuples from the
     * input handle.
     * 
     * @param handle
     * @param definition
     * @param subset
     * @return
     */
    public String[][] toArray(DataHandle handle, DataDefinition definition, DataHandle subset) {
        
        List<Integer> indices = new ArrayList<Integer>();
        for (String attribute : definition.getQuasiIdentifyingAttributes()) {
            indices.add(handle.getColumnIndexOf(attribute));
        } 
        
        List<String[]> list = new ArrayList<String[]>();
        Iterator<String[]> iter = subset.iterator();
        iter.next(); // Skip header
        for (; iter.hasNext();) {
            String[] input = iter.next();
            String[] output = new String[indices.size()];
            int i = 0;
            for (int index : indices) {
                output[i++] = input[index];
            }
            list.add(output);
        }
        
        String[] suppressed = new String[indices.size()];
        Arrays.fill(suppressed, "*"); // TODO
        for (int i = 0; i < handle.getNumRows() - subset.getNumRows(); i++) {
            list.add(suppressed);
        }
        
        return list.toArray(new String[list.size()][]);
    }
    
    /**
     * Returns an array representation of the subset, in which all rows that are not part of the
     * subset have been suppressed. This method *does* preserve the order of tuples from the
     * input handle.
     * 
     * @param handle
     * @param definition
     * @param subset
     * @return
     */
    public String[][] toArray(DataHandle handle, DataDefinition definition, DataSubset subset) {

        List<Integer> indices = new ArrayList<Integer>();
        for (String attribute : definition.getQuasiIdentifyingAttributes()) {
            indices.add(handle.getColumnIndexOf(attribute));
        } 
        
        List<String[]> list = new ArrayList<String[]>();
        String[] suppressed = new String[indices.size()];
        Arrays.fill(suppressed, "*"); // TODO
        
        Iterator<String[]> iter = handle.iterator();
        iter.next(); // Skip header
        int row = 0;
        for (; iter.hasNext();) {
            if (subset.getSet().contains(row)) {
                String[] input = iter.next();
                String[] output = new String[indices.size()];
                int i = 0;
                for (int index : indices) {
                    output[i++] = input[index];
                }
                list.add(output);
                
            } else {
                list.add(suppressed);
                iter.next();
            }
            row++;
        }
        
        return list.toArray(new String[list.size()][]);
    }
    
    /**
     * Returns one array that contains all values of the given attribute
     * @param handle
     * @param attribute
     * @return
     */
    public String[] toArray(DataHandle handle, String attribute) {
        int col = handle.getColumnIndexOf(attribute);
        if (col == -1) { return null; }
        int rows = handle.getNumRows();
        String[] result = new String[rows];
        for (int row = 0; row < rows; row++) {
            result[row] = handle.getValue(row, col);
        }
        return result;
    }
    
    /**
     * Converts output from the UTD toolbox and sdcMicro
     * @param file
     * @param header
     * @return
     * @throws IOException 
     */
	public String[][] toArray(File file, boolean header) throws IOException {
		try {
		    DataHandle handle = Data.create(file, Charset.defaultCharset(), new CSVSyntax()).getHandle();
		    List<String[]> list = new ArrayList<>();
		    Iterator<String[]> iter = handle.iterator();
		    if (header) iter.next(); // Ignore header
		    while (iter.hasNext()) {
		    	list.add(iter.next());
		    }
		    for (String[] row : list) {
		    	for (int i=0; i<row.length; i++) {
		    		if (row[i].startsWith("\"") && row[i].endsWith("\"")) {
		    			row[i] = row[i].substring(1, row[i].length()-1);
		    		}
		    	}
		    }
		    return list.toArray(new String[list.size()][]);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

    /**
     * DataDefinition to Map
     * @param definition
     * @return
     */
    public Map<String, String[][]> toMap(DataDefinition definition) {
        
        Map<String, String[][]> map = new HashMap<String, String[][]>();
        for (String s : definition.getQuasiIdentifiersWithGeneralization()) {
            map.put(s, definition.getHierarchy(s));
        }
        return map;
    }

    /**
     * Returns an array representation of this handle including data for the qis
     * @param handle
     * @param qis
     * @return
     */
    private String[][] toArray(DataHandle handle, Set<String> qis) {
        Set<Integer> qiIndices = new HashSet<Integer>();
        
        Iterator<String[]> iter = handle.iterator();
        String[] header = iter.next();
        for (int i = 0; i < header.length; i++) {
            // store indices of qis
            if (qis.contains(header[i])) {
                qiIndices.add(i);
            }
        }
        List<String[]> list = new ArrayList<>();
        for (; iter.hasNext();) {
            List<String> newLine = new ArrayList<>();
            String[] line = iter.next();
            for (int i = 0; i < line.length; i++) {
                if (qiIndices.contains(i)) {
                    newLine.add(line[i]);
                }
            }
            list.add(newLine.toArray(new String[newLine.size()]));
        }
        return list.toArray(new String[list.size()][]);
    }
}
