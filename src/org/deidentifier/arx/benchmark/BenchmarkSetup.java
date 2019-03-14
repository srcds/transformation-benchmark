package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;

/**
 * Setup class for benchmarks
 * @author Fabian Prasser
 */
public class BenchmarkSetup {

    /**
     * Algorithms
     */
	public static enum BenchmarkAlgorithm {
		ARX, MONDRIAN, SANCHEZ, SDCMICRO
	}

	/**
	 * Datasets
	 *
	 */
	public static enum BenchmarkDataset {
		ADULT, CUP, FARS, ATUS, IHIS, SS13ACS
	}

    /**
     * Quality models
     */
	public static enum BenchmarkQualityModel {
		LOSS, NUENTROPY, SSE
	}

    /**
     * Transformation models
     */
	public static enum BenchmarkTransformationModel {
		MULTI_DIMENSIONAL_GENERALIZATION, LOCAL_GENERALIZATION, CELL_SUPPRESSION,
	}

    /** 1 hour*/
    public static final int TIME_LIMIT = 3600000;
    
    /** Location of R executable*/
	public static final String LOCATION_OF_R = "C:\\Program Files\\R\\R-3.5.2\\bin";
    
    /**
     * Returns the generalization hierarchy for the dataset and attribute
     * @param dataset
     * @param attribute
     * @return
     * @throws IOException
     */
    public static Hierarchy getCellSuppressionHierarchy(BenchmarkDataset dataset, String attribute) throws IOException {
    	
        DefaultHierarchy hierarchy = Hierarchy.create();
        Data data = getData(dataset);
        int col = data.getHandle().getColumnIndexOf(attribute);
        String[] values = data.getHandle().getDistinctValues(col);
        for (String value : values) {
            hierarchy.add(value, "*");
        }
        return hierarchy;
    }

    /**
     * Configures and returns the dataset
     * @param dataset
     * @param tm
     * @param qis
     * @return
     * @throws IOException
     */

    public static Data getData(BenchmarkDataset dataset, BenchmarkTransformationModel tm, int qis) throws IOException {

        Data data = getProjectedDataset(getData(dataset), Arrays.copyOf(getQuasiIdentifyingAttributes(dataset), qis));
        int num = 0;
        for (String qi : getQuasiIdentifyingAttributes(dataset)) {
            if (tm == BenchmarkTransformationModel.CELL_SUPPRESSION) {
            	data.getDefinition().setAttributeType(qi, getCellSuppressionHierarchy(dataset, qi));
            } else {
            	data.getDefinition().setAttributeType(qi, getHierarchy(dataset, qi));
            }
            num++;
            if (num == qis) {
            	break;
            }
        }

        return data;
    }

    /**
     * Returns labels for the paper
     * @param dataset
     * @return
     */
	public static String getDataLabel(BenchmarkDataset dataset) {
		switch (dataset) {
		case ADULT:
			return "US Census";
		case CUP:
			return "Competition";
		case FARS:
			return "Crash Statistics";
		case ATUS:
			return "Time Use Survey";
		case IHIS:
			return "Health Interviews";
		case SS13ACS:
			return "Community Survey";
		}
		throw new IllegalArgumentException("Unknown dataset: " + dataset);
	};

    /**
     * Returns labels for the paper
     * @param dataset
     * @return
     */
	public static String getDataLabel(BenchmarkQualityModel quality) {
		switch (quality) {
		case LOSS:
			return "Granularity";
		case SSE:
			return "SSE";
		case NUENTROPY:
			return "Non-Uniform Entropy";
		}
		throw new IllegalArgumentException("Unknown quality model: " + quality);
	}
    
    /**
     * Returns labels for the paper
     * @param transformation
     * @return
     */
	public static String getDataLabel(BenchmarkTransformationModel transformation) {
		switch (transformation) {
		case LOCAL_GENERALIZATION:
			return "Local generalization";
		case CELL_SUPPRESSION:
			return "Cell suppression";
		case MULTI_DIMENSIONAL_GENERALIZATION:
			return "Multi-dimensional global generalization";
		}
		throw new IllegalArgumentException("Unknown transformation model: " + transformation);
	}

    /**
     * Returns the generalization hierarchy for the dataset and attribute
     * @param dataset
     * @param attribute
     * @return
     * @throws IOException
     */
    public static Hierarchy getHierarchy(BenchmarkDataset dataset, String attribute) throws IOException {
        switch (dataset) {
        case ADULT:
            return Hierarchy.create("hierarchies/adult_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case ATUS:
            return Hierarchy.create("hierarchies/atus_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case CUP:
            return Hierarchy.create("hierarchies/cup_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case FARS:
            return Hierarchy.create("hierarchies/fars_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case IHIS:
            return Hierarchy.create("hierarchies/ihis_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case SS13ACS:
            return Hierarchy.create("hierarchies/ss13acs_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        default:
            throw new IllegalArgumentException("Unknown dataset");
        }
    }

    /**
     * Returns the number of columns in the given data set
     * @param dataset
     * @return
     * @throws IOException
     */
    public static int getNumColumns(BenchmarkDataset dataset) throws IOException {
        return getData(dataset).getHandle().getNumColumns();
    }

    /**
     * Returns the number of records in the given data set
     * @param dataset
     * @return
     * @throws IOException
     */
    public static int getNumRecords(BenchmarkDataset dataset) throws IOException {
        return getData(dataset).getHandle().getNumRows();
    }

    /**
     * Returns the quasi-identifiers for the data set
     * @param dataset
     * @return
     */
    public static String[] getQuasiIdentifyingAttributes(BenchmarkDataset dataset) {
        switch (dataset) {
        case ADULT:
            return new String[] {
                                  "sex",
                                  "age",
                                  "race",
                                  "marital-status",
                                  "education",
                                  "native-country",
                                  "workclass",
                                  "occupation",
                                  "salary-class" };
        case ATUS:
            return new String[] {
                                  "Region",
                                  "Age",
                                  "Sex",
                                  "Race",
                                  "Marital status",
                                  "Citizenship status",
                                  "Birthplace",
                                  "Highest level of school completed",
                                  "Labor force status"
            };
        case CUP:
            return new String[] {
                                  "ZIP",
                                  "AGE",
                                  "GENDER",
                                  "INCOME",
                                  "STATE",
                                  "RAMNTALL",
                                  "NGIFTALL",
                                  "MINRAMNT"
            };
        case FARS:
            return new String[] {
                                  "iage",
                                  "irace",
                                  "ideathmon",
                                  "ideathday",
                                  "isex",
                                  "ihispanic",
                                  "istatenum",
                                  "iinjury"
            };
        case IHIS:
            return new String[] {
                                  "YEAR",
                                  "QUARTER",
                                  "REGION",
                                  "PERNUM",
                                  "AGE",
                                  "MARSTAT",
                                  "SEX",
                                  "RACEA",
                                  "EDUC"
            };
        case SS13ACS:
            return new String[] {
            		"Insurance purchased",
            		"Workclass",
            		"Divorced",
            		"Income",
            		"Sex",
            		"Mobility",
            		"Military service",
            		"Self-care",
            		"Grade level",
            		"Married",
            		"Education",
            		"Widowed",
            		"Cognitive",
            		"Insurance Medicaid",
            		"Ambulatory",
            		"Living with grandchildren",
            		"Age",
            		"Insurance employer",
            		"Citizenship",
            		"Indian Health Service",
            		"Independent living",
            		"Weight",
            		"Insurance Medicare",
            		"Hearing",
            		"Marital status",
            		"Vision",
            		"Insurance Veteran's Association",
            		"Relationship",
            		"Insurance Tricare",
            		"Childbirth"
            };

        default:
            throw new RuntimeException("Invalid dataset");
        }
    }

    /**
     * Returns a dataset
     * @param dataset
     * @return
     * @throws IOException
     */
    private static Data getData(BenchmarkDataset dataset) throws IOException {
    	String filename = null;
		switch (dataset) {
		case ADULT:
			filename = "adult_int.csv";
			break;
		case CUP:
			filename = "cup_int.csv";
			break;
		case FARS:
			filename = "fars_int.csv";
			break;
		case ATUS:
			filename = "atus_int.csv";
			break;
		case IHIS:
			filename = "ihis_int.csv";
			break;
		case SS13ACS:
			filename = "ss13acs_int.csv";
			break;
		default:
			throw new RuntimeException("Invalid dataset");
		}
		return Data.create("data/" + filename, Charset.defaultCharset(), ';');
    }

    /**
     * Projects data
     * @param data
     * @param qis
     * @return
     */
    private static Data getProjectedDataset(Data data, String[] qis) {
		DataHandle handle = data.getHandle();
		List<String[]> output = new ArrayList<>();
		output.add(qis);
		for (int i = 0; i < handle.getNumRows(); i++) {
			String[] record = new String[qis.length];
			for (int j = 0; j < qis.length; j++) {
				record[j] = handle.getValue(i, handle.getColumnIndexOf(qis[j]));
			}
			output.add(record);
		}
		return Data.create(output);
	};
}
