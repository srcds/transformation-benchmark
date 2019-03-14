/**
 * Obtained from http://crises-deim.urv.cat/opendata/SPD_Science.zip
 */
package urv.crises.anonym;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class provides functions for anonymization.
 * 
 * @author Sergio Martinez (Universitat Rovira i Virgili)
 */
public class Functions {

    /**
     * This function applies k-anonymization to a dataset loaded with
     * loadFile() function.
     * 
     * @param dataOri, the dataset
     * @param k, the desired k level
     * @return the anonymized version of the dataset that fullfils k-anonymity
     */
    public static ArrayList<Record> kAnonymize(ArrayList<String[]> dataOri, int k) {
        ArrayList<Record> dataAnom = new ArrayList<>();
        int pos, remain, numReg, numAttrQuasi, numAttr, cont;
        Cluster cluster;
        Record record;
        Attribute attribute;
        String value;

        Distances.calculateTypicalDeviations(dataOri);
        numAttrQuasi = dataOri.get(0).length;
        numAttr = dataOri.get(0).length;
        Record.numAttr = numAttr;
        cont = 0;
        for (String reg[] : dataOri) {
            record = new Record(cont);
            cont++;
            for (int i = 0; i < numAttr; i++) {
                value = reg[i];
                attribute = new Attribute(value);
                record.attributes[i] = attribute;
            }
            dataAnom.add(record);
        }

        Functions.sortByQuasi(dataAnom);

        cluster = new Cluster();
        numReg = dataAnom.size();
        pos = 0;
        remain = numReg;
        while (remain >= (2 * k)) {
            for (int i = 0; i < k; i++) {
                cluster.add(dataAnom.get(pos));
                pos++;
            }
            cluster.calculateCentroid();
            pos -= k;
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < numAttrQuasi; j++) {
                    dataAnom.get(pos).attributes[j].value = cluster.getCentroid().attributes[j].value;
                    dataAnom.get(pos).attributes[j].value1 = cluster.getCentroid().attributes[j].value1;
                    dataAnom.get(pos).attributes[j].value2 = cluster.getCentroid().attributes[j].value2;
                }
                pos++;
            }
            cluster.clear();
            remain = numReg - pos;
        }
        for (int i = 0; i < remain; i++) {
            cluster.add(dataAnom.get(pos));
            pos++;
        }
        cluster.calculateCentroid();
        pos -= remain;
        for (int i = 0; i < remain; i++) {
            for (int j = 0; j < numAttrQuasi; j++) {
                dataAnom.get(pos).attributes[j].value = cluster.getCentroid().attributes[j].value;
                dataAnom.get(pos).attributes[j].value1 = cluster.getCentroid().attributes[j].value1;
                dataAnom.get(pos).attributes[j].value2 = cluster.getCentroid().attributes[j].value2;
            }
            pos++;
        }

        Collections.sort(dataAnom, new ComparatorID());
        return dataAnom;
    }

    /**
     * This function applies k-anonymization + t-closeness to a dataset loaded with
     * loadFile() function.
     * 
     * @param dataOri, the dataset
     * @param k, the desired k level
     * @param t, the desired t closeness
     * @return the anonymized version of the dataset that fullfils k-anonymity and t-closeness
     */
    public static ArrayList<Record> kAnonymize_tCloseness(int k, double t, ArrayList<String[]> dataOri) {
        ArrayList<Record> dataAnom = new ArrayList<>();
        ArrayList<Cluster> clustersK = new ArrayList<Cluster>();
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        Record record, r;
        Attribute attribute;
        String value;
        int cont, n;
        int remain, numAttrQuasi, numAttr, attrSensitive;
        int numItem, index, numClustersK, remainder;
        double kPrime;
        Cluster clusterTemp;

        System.out.println("Anonymizing kAnonymity / tCloseness k = " + k + " / t = " + t);

        Distances.calculateTypicalDeviations(dataOri);

        numAttrQuasi = dataOri.get(0).length - 1;
        numAttr = dataOri.get(0).length;
        attrSensitive = dataOri.get(0).length - 1;
        Record.numAttr = numAttr;
        cont = 0;
        for (String reg[] : dataOri) {
            record = new Record(cont);
            cont++;
            for (int i = 0; i < numAttr; i++) {
                value = reg[i];
                attribute = new Attribute(value);
                record.attributes[i] = attribute;
            }
            dataAnom.add(record);
        }

        System.out.print("Sorting for sensitive attribute...");
        Functions.sortBySensitive(dataAnom, attrSensitive);
        System.out.println("done");

        n = dataAnom.size();
        kPrime = n / (2 * (n - 1) * t + 1);
        if (k > kPrime) {
            numClustersK = k;
        } else {
            numClustersK = ((int) kPrime) + 1;
        }
        numItem = dataAnom.size() / numClustersK;
        remainder = dataAnom.size() % numClustersK;

        if (remainder >= numItem) {
            numClustersK = numClustersK + (remainder / numItem);
        }

        System.out.print("Creating k subsets(" + numClustersK + ")...");
        index = 0;
        for (int i = 0; i < numClustersK; i++) {
            clusterTemp = new Cluster();
            for (int j = 0; j < numItem; j++) {
                r = dataAnom.get(index);
                clusterTemp.add(r);
                index++;
            }
            clustersK.add(clusterTemp);
        }

        if (index < dataAnom.size()) { // remain records in a cluster
            clusterTemp = new Cluster();
            for (int i = index; i < dataAnom.size(); i++) {
                r = dataAnom.get(i);
                clusterTemp.add(r);
            }
            clustersK.add(clusterTemp);
        }
        System.out.println("done");

        System.out.print("Sorting for quasi-identifier attributes each subset...");
        cont = 1;
        for (Cluster cluster : clustersK) {
            Functions.sortByQuasi(cluster.getElements());
            cont++;
        }
        System.out.println("done");

        System.out.print("Creating clusters...");
        remain = dataAnom.size();
        dataAnom.clear();
        index = 0;
        while (remain > 0) {
            clusterTemp = new Cluster();
            for (Cluster cluster : clustersK) {
                if (cluster.getElements().size() > index) {
                    clusterTemp.add(cluster.getElements().get(index)); // the next record is added
                    remain--;
                }
            }
            index++;
            clusters.add(clusterTemp);
        }
        System.out.println("done");

        System.out.print("Anonymizing...");
        cont = 0;
        for (Cluster cluster : clusters) {
            cluster.calculateCentroid();
            for (Record reg : cluster.getElements()) {
                for (int j = 0; j < numAttrQuasi; j++) {
                    reg.attributes[j].value = cluster.getCentroid().attributes[j].value;
                    reg.attributes[j].value1 = cluster.getCentroid().attributes[j].value1;
                    reg.attributes[j].value2 = cluster.getCentroid().attributes[j].value2;
                }
                dataAnom.add(reg);
                cont++;
            }
        }
        System.out.println("done");

        System.out.print("ReArranging...");
        Collections.sort(dataAnom, new ComparatorID());
        System.out.println("done");

        return dataAnom;
    }

    /**
     * This function applies coarsening to a dataset loaded with
     * loadFile() function.
     * 
     * @param dataOri, the dataset
     * @param resolution, the desired resolution level
     * @return the anonymized version of the dataset according the resolution
     */
    public static ArrayList<Record> coarsen(ArrayList<String[]> dataOri, int resolution) {
        ArrayList<Record> dataAnom = new ArrayList<>();
        Record record;
        int cont, numAttrQuasi, numAttr;
        String value;
        int valueInt;
        Attribute attribute;
        int value1[][];
        int value2[][];

        System.out.println("Coarsening with resolution = " + resolution + " ...");

        Distances.calculateTypicalDeviations(dataOri);

        numAttrQuasi = dataOri.get(0).length;
        numAttr = dataOri.get(0).length;
        Record.numAttr = numAttr;
        cont = 0;
        for (String reg[] : dataOri) {
            record = new Record(cont);
            cont++;
            for (int i = 0; i < numAttr; i++) {
                value = reg[i];
                attribute = new Attribute(value);
                record.attributes[i] = attribute;
            }
            dataAnom.add(record);
        }

        if (resolution != 100) {
            value1 = new int[numAttrQuasi][resolution];
            value2 = new int[numAttrQuasi][resolution];

            for (int i = 0; i < numAttrQuasi; i++) {
                calculateIntervals(dataOri, i, resolution, value1, value2);
            }

            for (int i = 0; i < numAttrQuasi; i++) {
                for (Record reg : dataAnom) {
                    valueInt = Integer.parseInt(reg.attributes[i].value);
                    for (int j = 0; j < resolution; j++) {
                        if (valueInt >= value1[i][j] && valueInt <= value2[i][j]) {
                            reg.attributes[i].value1 = value1[i][j];
                            reg.attributes[i].value2 = value2[i][j];
                            break;
                        }
                    }
                }
            }
        }

        return dataAnom;
    }

    /**
     * This function calculates the information loss of the anonymized with
     * respect to the original dataset.
     * 
     * @param dataOri, the original dataset, loaded with loadFile() function
     * @param dataAnom, the anonymized dataset, resulting of kAnonymize(),
     *            kAnonymize_tCloseness() or coarsen() functions
     * @return the information loss
     */
    public static double calculateIL(ArrayList<String[]> dataOri, ArrayList<Record> dataAnom) {
        double IL, partial;
        String regOri[];
        Record regAnom;
        int numAttrQuasi;

        numAttrQuasi = dataOri.get(0).length;
        IL = 0;
        for (int i = 0; i < dataOri.size(); i++) {
            regOri = dataOri.get(i);
            regAnom = dataAnom.get(i);
            partial = Distances.euclideanDistNorm(regOri, regAnom);
            IL += partial;
        }
        IL /= numAttrQuasi;
        IL /= dataOri.size();
        return IL;
    }

    /**
     * This function calculates the number of attribute disclosures
     * in an anonymized dataset.
     * 
     * @param data, the anonymized dataset, resulting of kAnonymize(),
     *            kAnonymize_tCloseness() or coarsen() functions
     * @return the number of records with attribute disclosure
     */
    public static int calculateAttributeDisclosure(ArrayList<Record> data) {
        String temp;
        Record reg1;
        int controlUnique;
        HashMap<String, Integer[]> control = new HashMap<String, Integer[]>();
        HashMap<String, ArrayList<Integer>> controlEquals = new HashMap<String, ArrayList<Integer>>();
        Integer rangesCluster[];
        ArrayList<Integer> controlEqualsTemp;
        int value1, value2;
        int index, numAttrQuasi, attrSensitive;
        int numRangesSensitive, range;
        int totalRange[];
        int ranges[][];

        System.out.print("Calculating attribute disclosures...");

        ranges = Functions.rangesSensitiveManual();

        numRangesSensitive = ranges.length;

        numAttrQuasi = data.get(0).attributes.length - 1;
        attrSensitive = data.get(0).attributes.length - 1;
        for (int i = 0; i < data.size(); i++) {
            reg1 = data.get(i);
            temp = "";
            for (int k = 0; k < numAttrQuasi; k++) {
                value1 = reg1.attributes[k].value1;
                value2 = reg1.attributes[k].value2;
                temp += "(" + value1 + "-" + value2 + ")" + ",";
            }
            temp = temp.substring(0, temp.length() - 1);
            rangesCluster = control.get(temp);
            index = calculateWhichRange(ranges, reg1.attributes[attrSensitive].value);
            if (rangesCluster != null) {
                rangesCluster[index]++;
                control.put(temp, rangesCluster);
                controlEqualsTemp = controlEquals.get(temp);
                controlEqualsTemp.add(i);
                controlEquals.put(temp, controlEqualsTemp);
            } else {
                rangesCluster = new Integer[numRangesSensitive];
                for (int p = 0; p < numRangesSensitive; p++) {
                    rangesCluster[p] = new Integer(0);
                }
                rangesCluster[index]++;
                control.put(temp, rangesCluster);
                controlEqualsTemp = new ArrayList<Integer>();
                controlEqualsTemp.add(i);
                controlEquals.put(temp, controlEqualsTemp);
            }
        }
        range = 0;
        totalRange = new int[numRangesSensitive];
        for (String s : control.keySet()) {
            rangesCluster = control.get(s);
            controlUnique = 0;
            for (int p = 0; p < numRangesSensitive; p++) {
                if (rangesCluster[p] > 0) {
                    controlUnique++;
                    range = p;
                }
            }
            if (controlUnique == 1) { // they have attribute disclosure
                totalRange[range] += rangesCluster[range];
            }
        }

        return totalRange[1];
    }

    /**
     * This function calculates the number of reidentifications
     * in an anonymized dataset.
     * 
     * @param data, the anonymized dataset, resulting of kAnonymize(),
     *            kAnonymize_tCloseness() or coarsen() functions
     * @return the number of unique records
     */
    public static int calculateUniques(ArrayList<Record> data) {
        HashMap<String, Integer> control = new HashMap<String, Integer>();
        String temp;
        Integer cont;
        int countUniques;
        int value1, value2, numAttrQuasi;
        Record reg1;

        System.out.print("Calculating reidentifications...");

        numAttrQuasi = data.get(0).attributes.length - 1;
        for (int i = 0; i < data.size(); i++) {
            reg1 = data.get(i);
            temp = "";
            for (int k = 0; k < numAttrQuasi; k++) {
                value1 = reg1.attributes[k].value1;
                value2 = reg1.attributes[k].value2;
                temp += "(" + value1 + "-" + value2 + ")" + ",";
            }
            temp = temp.substring(0, temp.length() - 1);
            cont = control.get(temp);
            if (cont != null) {
                cont++;
                control.put(temp, cont);
            } else {
                control.put(temp, 1);
            }
        }

        countUniques = 0;
        for (String s : control.keySet()) {
            cont = control.get(s);
            if (cont == 1) {
                countUniques++;
            }
        }
        return countUniques;
    }

    /**
     * This function loads an original dataset as input in the
     * anonymization process.
     * 
     * @param fileName, the file name
     * @param data, the data collection
     * @return the header of the original dataset
     */
    public static String[] loadFile(String fileName, ArrayList<String[]> data) {
        File archivo = new File(fileName);
        FileReader fr;
        String line;
        String strTemp[];
        String header[] = null;
        data.clear();
        try {
            fr = new FileReader(archivo);
            BufferedReader br = new BufferedReader(fr);
            line = br.readLine();
            header = line.split(",");
            while ((line = br.readLine()) != null) {
                strTemp = line.split(",");
                data.add(strTemp);
            }
            br.close();
            fr.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return header;
    }

    /**
     * This function saves an anonymized dataset to disc
     * 
     * @param data, the anonymized dataset
     * @param header, the header
     * @param fileName, the file name
     */
    public static void guardaArxiuAnonim(ArrayList<Record> data, String[] header, String fileName) {
        File file;
        FileWriter fr;
        BufferedWriter br;
        String str, strValue1, strValue2;
        int numQi;

        System.out.print("Saving file: " + fileName + "...");

        numQi = header.length;
        try {
            file = new File(fileName);
            fr = new FileWriter(file);
            br = new BufferedWriter(fr);

            str = "";
            for (int i = 0; i < header.length; i++) {
                str += header[i] + ",";
            }
            str = str.substring(0, str.length() - 1);
            br.write(str);
            br.newLine();

            for (Record reg : data) {
                str = "";
                for (int i = 0; i < numQi; i++) {
                    strValue1 = String.valueOf(reg.attributes[i].value1);
                    strValue2 = String.valueOf(reg.attributes[i].value2);
                    str += "[" + strValue1 + ";" + strValue2 + "]"; 
                    if (i<numQi - 1) str += ",";
                }
//                str += String.valueOf(reg.atributes[attrSensitive].value);
                br.write(str);
                br.newLine();
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done");
    }

    private static int[][] rangesSensitiveManual() {
        int numRangesSensitive = 2;
        int rangs[][] = new int[numRangesSensitive][2];

        rangs[0][0] = 0;
        rangs[0][1] = 100000;
        rangs[1][0] = 100001;
        rangs[1][1] = 10000000;

        return rangs;
    }

    private static int calculateWhichRange(int ranges[][], String valor) {
        int sensitiveValue, range;

        range = 0;
        sensitiveValue = Integer.parseInt(valor);
        for (int i = 0; i < ranges.length; i++) {
            if (sensitiveValue >= ranges[i][0] && sensitiveValue <= ranges[i][1]) {
                range = i;
                break;
            }
        }
        return range;
    }

    private static void calculateIntervals(ArrayList<String[]> data, int attr, int resolution, int valor1[][], int valor2[][]) {
        HashMap<Integer, Integer> control = new HashMap<Integer, Integer>();
        ArrayList<Integer> items = new ArrayList<>();
        String strTemp[];
        int temp;
        Integer cont;
        int numLabels, index;
        double range, pos;

        for (int i = 0; i < data.size(); i++) {
            strTemp = data.get(i);
            if (strTemp.length < 10) {
                temp = -1;
            } else {
                temp = Integer.parseInt(strTemp[attr]);
            }
            cont = control.get(temp);
            if (cont != null) {
                cont++;
                control.put(temp, cont);
            } else {
                control.put(temp, 1);
            }
        }

        for (Integer s : control.keySet()) {
            items.add(s);
        }

        Collections.sort(items);

        numLabels = control.size();

        range = (double) numLabels / (double) resolution;
        pos = 0;
        index = (int) pos;
        for (int i = 0; i < (resolution - 1); i++) {
            valor1[attr][i] = items.get(index);
            pos += range;
            index = (int) (pos - 1);
            valor2[attr][i] = items.get(index);
            index++;
        }
        valor1[attr][resolution - 1] = items.get(index);
        valor2[attr][resolution - 1] = items.get(items.size() - 1);
    }

    private static void sortByQuasi(ArrayList<Record> data) {

        ComparatorQuasi.setAttributeSortCriteria(data.get(0).getAttr().length);
        Collections.sort(data, new ComparatorQuasi());
    }

    private static void sortBySensitive(ArrayList<Record> data, int attr) {
        ComparatorSensitive.setAttributeSortCriteria(attr);
        Collections.sort(data, new ComparatorSensitive());
    }

}
