/*
 * Source code of our CBMS 2014 paper "A benchmark of globally-optimal 
 *      methods for the de-identification of biomedical data"
 *      
 * Copyright (C) 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.benchmark;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkAlgorithm;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkQualityModel;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkTransformationModel;

import de.linearbits.objectselector.SelectorBuilder;
import de.linearbits.subframe.analyzer.Analyzer;
import de.linearbits.subframe.analyzer.buffered.BufferedArithmeticMeanAnalyzer;
import de.linearbits.subframe.graph.Field;
import de.linearbits.subframe.graph.Function;
import de.linearbits.subframe.graph.Labels;
import de.linearbits.subframe.graph.Plot;
import de.linearbits.subframe.graph.PlotHistogramClustered;
import de.linearbits.subframe.graph.PlotLinesClustered;
import de.linearbits.subframe.graph.Point3D;
import de.linearbits.subframe.graph.Series3D;
import de.linearbits.subframe.io.CSVFile;
import de.linearbits.subframe.io.CSVLine;
import de.linearbits.subframe.render.GnuPlotParams;
import de.linearbits.subframe.render.GnuPlotParams.KeyPos;
import de.linearbits.subframe.render.LaTeX;
import de.linearbits.subframe.render.PlotGroup;

public class BenchmarkAnalysis {
	
	/**
	 * Analyzer used to extract values
	 * 
	 * @author Fabian Prasser
	 */
	private static class MeanAnalyzer extends BufferedArithmeticMeanAnalyzer {

		/**
		 * Constructor
		 * @param label
		 * @param length
		 * @param i
		 * @param d
		 */
		public MeanAnalyzer(String label, int length, int i, double d) {
			super(label, length, i, d);
		}

		/**
		 * Constructor
		 */
		public MeanAnalyzer() {
			super();
		}

		@Override
		public String getValue() {
	        if (count==0) throw new RuntimeException("No values specified!");
	        double result = 0d;
	        double _count = 0d;
	        for (int i=0; i<count; i++){
	        	// Ignore zero
	        	if (values[i] != 0d) {
	        		_count++;
		            result += values[i];
	        	}
	        }
	        if (_count == 0d) {
	        	return "0.0";
	        }
	        return String.valueOf(result / (double)_count);
		}

		@Override
		public Analyzer<Double> newInstance() {
			return new MeanAnalyzer(super.getLabel(), super.values.length, 0, super.growthRate);
		}
		
	}
    
    /**
     * Main
     * @param args
     * @throws IOException
     * @throws ParseException 
     */
    public static void main(String[] args) throws IOException, ParseException {

    	// k
        generateRuntime("results-sanchez", 
        					BenchmarkTransformationModel.LOCAL_GENERALIZATION, 
        					BenchmarkAlgorithm.ARX,
        					BenchmarkAlgorithm.SANCHEZ,
        					true);
        generateUtility("results-sanchez", 
        					BenchmarkTransformationModel.LOCAL_GENERALIZATION, 
        					BenchmarkAlgorithm.ARX,
        					BenchmarkAlgorithm.SANCHEZ,
        					BenchmarkQualityModel.LOSS,
        					true);
        generateUtility("results-sanchez", 
							BenchmarkTransformationModel.LOCAL_GENERALIZATION, 
							BenchmarkAlgorithm.ARX,
							BenchmarkAlgorithm.SANCHEZ,
							BenchmarkQualityModel.SSE,
        					true);
        generateRuntime("results-mondrian", 
							BenchmarkTransformationModel.MULTI_DIMENSIONAL_GENERALIZATION, 
							BenchmarkAlgorithm.ARX,
							BenchmarkAlgorithm.MONDRIAN,
        					true);
		generateUtility("results-mondrian", 
							BenchmarkTransformationModel.MULTI_DIMENSIONAL_GENERALIZATION, 
							BenchmarkAlgorithm.ARX,
							BenchmarkAlgorithm.MONDRIAN,
							BenchmarkQualityModel.LOSS,
        					true);
		generateUtility("results-mondrian", 
							BenchmarkTransformationModel.MULTI_DIMENSIONAL_GENERALIZATION, 
							BenchmarkAlgorithm.ARX,
							BenchmarkAlgorithm.MONDRIAN,
							BenchmarkQualityModel.SSE,
        					true);
		
		// QI
        generateRuntime("results-sanchez", 
						BenchmarkTransformationModel.LOCAL_GENERALIZATION, 
						BenchmarkAlgorithm.ARX,
						BenchmarkAlgorithm.SANCHEZ,
						false);
		generateUtility("results-sanchez", 
						BenchmarkTransformationModel.LOCAL_GENERALIZATION, 
						BenchmarkAlgorithm.ARX,
						BenchmarkAlgorithm.SANCHEZ,
						BenchmarkQualityModel.LOSS,
						false);
		generateUtility("results-sanchez", 
						BenchmarkTransformationModel.LOCAL_GENERALIZATION, 
						BenchmarkAlgorithm.ARX,
						BenchmarkAlgorithm.SANCHEZ,
						BenchmarkQualityModel.SSE,
						false);
		generateRuntime("results-mondrian", 
						BenchmarkTransformationModel.MULTI_DIMENSIONAL_GENERALIZATION, 
						BenchmarkAlgorithm.ARX,
						BenchmarkAlgorithm.MONDRIAN,
						false);
		generateUtility("results-mondrian", 
						BenchmarkTransformationModel.MULTI_DIMENSIONAL_GENERALIZATION, 
						BenchmarkAlgorithm.ARX,
						BenchmarkAlgorithm.MONDRIAN,
						BenchmarkQualityModel.LOSS,
						false);
		generateUtility("results-mondrian", 
						BenchmarkTransformationModel.MULTI_DIMENSIONAL_GENERALIZATION, 
						BenchmarkAlgorithm.ARX,
						BenchmarkAlgorithm.MONDRIAN,
						BenchmarkQualityModel.SSE,
						false);
    }

    /**
     * Plot for k-scaling utility
     * @param file
     * @param transformation
     * @param algorithm1
     * @param algorithm2
     * @param k 
     * @param quality1
     * @param quality2
     * @throws IOException 
     * @throws ParseException 
     */
	private static void generateUtility(String file, BenchmarkTransformationModel transformation,
			BenchmarkAlgorithm algorithm1, BenchmarkAlgorithm algorithm2, BenchmarkQualityModel quality, boolean k) throws IOException, ParseException {

    	// Open file
    	CSVFile csv = new CSVFile(new File("results/results-arx.csv"));
    	append(csv, new File("results/" + file + ".csv"));

        // Prepare
        List<Plot<?>> plots = new ArrayList<Plot<?>>();
        
        // For each dataset
        boolean first = true;
        for (BenchmarkDataset dataset : new BenchmarkDataset[] {	BenchmarkDataset.ADULT,
														        	BenchmarkDataset.CUP,
														        	BenchmarkDataset.FARS,
														        	BenchmarkDataset.ATUS,
														        	BenchmarkDataset.IHIS,
														        	BenchmarkDataset.SS13ACS}){
        
	        // Collect data for all algorithms
	        Series3D series = null;
	        for (BenchmarkAlgorithm algorithm : new BenchmarkAlgorithm[] {algorithm1, algorithm2}) {
	
	            Series3D _series = getSeries(csv, dataset, transformation, algorithm, quality, k ? "k" : "QIs", "Utility");
	            if (series == null) series = _series;
	            else series.append(_series);
	        }
	        
	        // Plot
	        if (series != null && !series.getData().isEmpty()) {

	            // Transform utility into percent
				series.transform(new Function<Point3D>() {
					@Override
					public Point3D apply(Point3D t) {
						return new Point3D(t.x, t.y, String.valueOf(Double.valueOf(t.z) * 100d));
					}
				});

	            // Fix series
	            addMissingDataPoints(series);
	            
	            // Create plot
				if (k) {
					plots.add(new PlotHistogramClustered(BenchmarkSetup.getDataLabel(dataset),
                            new Labels(k ? "Parameter k" : "Number of QIs", !first ? "" : BenchmarkSetup.getDataLabel(quality) + " [%]"),
                            series));
				} else {
					plots.add(new PlotLinesClustered(BenchmarkSetup.getDataLabel(dataset),
                            new Labels(k ? "Parameter k" : "Number of QIs", !first ? "" : BenchmarkSetup.getDataLabel(quality) + " [%]"),
                            series));
				}
	        }
	        first = false;
        }

        // Printing
        GnuPlotParams params = new GnuPlotParams();
        params.rotateXTicks = 0;
        params.printValues = false;
        params.size = 0.5;
        params.keypos = KeyPos.NONE;
        params.font = ",10";
        params.printValuesFormatString= "%.0f";
        params.minY = 0d;
        params.maxY = 100d;
        params.width = 2d;
        params.height = 2d;

        List<PlotGroup> groups = new ArrayList<>();
        groups.add(new PlotGroup("Utility over "+(k ? "k" : "QIs")+" for transformation model: " + BenchmarkSetup.getDataLabel(transformation), plots, params, 1d/6d));
        String filename = "results/" + file + "-method-" + BenchmarkSetup.getDataLabel(transformation).toLowerCase() + "-utility-" + BenchmarkSetup.getDataLabel(quality) + "-" +
                     (k ? "k" : "qis");
        LaTeX.plot(groups, filename, false);
	}

	/**
	 * Make sure that we have measurement points for all methods
	 * @param series
	 */
    private static void addMissingDataPoints(Series3D series) {
    	
    	// Collect base data
    	Set<String> valuesX = new HashSet<>();
    	Set<String> valuesY = new HashSet<>();
    	for (Point3D p : series.getData()) {
    		valuesX.add(p.x);
    		valuesY.add(p.y);
    	}
    	
    	// Add x if missing
    	for (String x : valuesX) {

        	// Add y if missing
        	for (String y : valuesY) {    		
	    		
	    		// Check if contained
	    		boolean contained = false;
	        	for (Point3D p : series.getData()) {
	        		if (p.x.equals(x) && p.y.equals(y)) {
	        			contained = true;
	        			break;
	        		}
	        	}
	        	
	        	// Add if missing
	        	if (!contained) {
	        		series.getData().add(new Point3D(x, y, "0"));
	        	}
        	}
    	}
    	
    	// Sort
    	series.sort(new Comparator<Point3D>() {
			@Override
			public int compare(Point3D arg0, Point3D arg1) {
				int cmp1 = Integer.valueOf(arg0.x).compareTo(Integer.valueOf(arg1.x));
				int cmp2 = arg0.y.compareTo(arg1.y);
				int cmp3 = Double.valueOf(arg0.z).compareTo(Double.valueOf(arg1.z));
				if (cmp1 != 0) {
					return cmp1;
				} else {
					if (cmp2 != 0) {
						return cmp2;
					} else {
						return cmp3;
					}
				}
			}
		});
	}

	/**
     * Plot for k-scaling runtime
     * @param file
     * @param transformation
     * @param algorithm1
     * @param algorithm2
     * @param k 
     * @throws IOException 
     * @throws ParseException 
     */
    private static void generateRuntime(String file,	BenchmarkTransformationModel transformation,
														BenchmarkAlgorithm algorithm1,
														BenchmarkAlgorithm algorithm2, boolean k) throws IOException, ParseException {

    	// Open file
    	CSVFile csv = new CSVFile(new File("results/results-arx.csv"));
    	append(csv, new File("results/" + file + ".csv"));

        // Prepare
        List<Plot<?>> plots = new ArrayList<Plot<?>>();
        
        // For each dataset
        boolean first = true;
        for (BenchmarkDataset dataset : new BenchmarkDataset[] {	BenchmarkDataset.ADULT,
														        	BenchmarkDataset.CUP,
														        	BenchmarkDataset.FARS,
														        	BenchmarkDataset.ATUS,
														        	BenchmarkDataset.IHIS,
														        	BenchmarkDataset.SS13ACS}){
        
	        // Collect data for all algorithms
	        Series3D series = null;
	        for (BenchmarkAlgorithm algorithm : new BenchmarkAlgorithm[] {algorithm1, algorithm2}) {
	
	            Series3D _series = getSeries(csv, dataset, transformation, algorithm, BenchmarkQualityModel.LOSS, k ? "k" : "QIs", "Time");
	            if (series == null) series = _series;
	            else series.append(_series);
	        }
	        
	        // Plot
	        if (series != null && !series.getData().isEmpty()) {

	            // Transform execution times from millis to seconds
	                series.transform(new Function<Point3D>(){
	                    @Override
	                    public Point3D apply(Point3D t) {
	                        return new Point3D(t.x, t.y, String.valueOf(Double.valueOf(t.z)/1000d));
	                    }
	                });
	                
	            // Fix series
	            addMissingDataPoints(series);
	                
	            // Create plot
				if (k) {
					plots.add(new PlotHistogramClustered(BenchmarkSetup.getDataLabel(dataset),
							new Labels(k ? "Parameter k" : "Number of QIs", !first ? "" : "Execution time [s]"), series));
				} else {
					plots.add(new PlotLinesClustered(BenchmarkSetup.getDataLabel(dataset),
							new Labels(k ? "Parameter k" : "Number of QIs", !first ? "" : "Execution time [s]"), series));
				}
	        }
	        first = false;
        }

        // Printing
        GnuPlotParams params = new GnuPlotParams();
        params.rotateXTicks = 0;
        params.printValues = false;
        params.size = 0.5;
        params.font = ",10";
        params.logY = true;
        params.keypos = KeyPos.NONE;
        params.printValuesFormatString= "%.0f";
        params.width = 2d;
        params.height = 2d;
        
        List<PlotGroup> groups = new ArrayList<>();
        groups.add(new PlotGroup("Scaling over " + (k ? "k" : "QIs") + " for transformation model: " + BenchmarkSetup.getDataLabel(transformation), plots, params,  1d/6d));
        String filename = "results/" + file + "-method-" + BenchmarkSetup.getDataLabel(transformation).toLowerCase() + "-scalability-" + (k ? "k" : "qis");
        LaTeX.plot(groups, filename, false);
    }

    /**
     * Appends the second file
     * @param csv
     * @param file
     * @throws IOException 
     */
    private static void append(CSVFile csv, File file) throws IOException {
		
    	if (!file.exists()) {
    		return;
    	}
    	CSVFile other = new CSVFile(file);
    	Iterator<CSVLine> iter = other.iterator();
    	if (!iter.hasNext()) {
    		return;
    	} else {
    		iter.next();
    	}
    	if (!iter.hasNext()) {
    		return;
    	} else {
    		iter.next();
    	}
    	while (iter.hasNext()) {
    		csv.addLine(iter.next().getData());
    	}
	}

	/**
     * Returns a series
     * @param file
     * @param dataset
     * @param transformation
     * @param algorithm
     * @param quality
     * @param attributeX
     * @param attributeZ
     * @return
     * @throws ParseException
     */
    private static Series3D getSeries(	CSVFile file, BenchmarkDataset dataset, BenchmarkTransformationModel transformation,
    									BenchmarkAlgorithm algorithm, BenchmarkQualityModel quality, String attributeX, String attributeZ) throws ParseException {

        // Select data for the given algorithm
    	SelectorBuilder<String[]> builder = file.getSelectorBuilder()
												.field("Dataset")
												.equals(dataset.toString())
												.and()
												.field("Algorithm")
												.equals(algorithm.toString())
												.and()
												.field("Transformation")
												.equals(transformation.toString())
												.and()
												.field("Quality")
												.equals(quality.toString());

    	// Use only 5-anonymity, if scaling over QIs
    	if (attributeX.equals("QIs")) {
    		builder = builder.and().field("k").equals("5");
    	}
    	// Use only all QIs, if scaling over k
    	if (attributeX.equals("k")) {
    		builder = builder.and().field("QIs").equals(String.valueOf(BenchmarkSetup.getQuasiIdentifyingAttributes(dataset).length));
    	}

        // Create series
        Series3D series = new Series3D(file, builder.build(), 
                                       new Field(attributeX),
                                       new Field("Algorithm"),
                                       new Field(attributeZ, Analyzer.VALUE), new MeanAnalyzer());
        // Done
        return series;
	}
}