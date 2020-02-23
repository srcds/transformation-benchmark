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
import java.util.List;

import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

import de.linearbits.objectselector.SelectorBuilder;
import de.linearbits.subframe.graph.Field;
import de.linearbits.subframe.graph.Labels;
import de.linearbits.subframe.graph.Plot;
import de.linearbits.subframe.graph.PlotHistogram;
import de.linearbits.subframe.graph.Series2D;
import de.linearbits.subframe.io.CSVFile;
import de.linearbits.subframe.render.GnuPlotParams;
import de.linearbits.subframe.render.GnuPlotParams.KeyPos;
import de.linearbits.subframe.render.LaTeX;
import de.linearbits.subframe.render.PlotGroup;

public class BenchmarkAnalysisThierry {
	
    
    /**
     * Main
     * @param args
     * @throws IOException
     * @throws ParseException 
     */
    public static void main(String[] args) throws IOException, ParseException {

    	// Open file
    	CSVFile csv = new CSVFile(new File("results/results-thierry.csv"));

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
	        Series2D series = getSeries(csv, dataset);
	        
					plots.add(new PlotHistogram(BenchmarkSetup.getDataLabel(dataset),
                            new Labels("X", "Y"),
                            series));
				
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
        groups.add(new PlotGroup("Title", plots, params, 1d/6d));
        LaTeX.plot(groups, "results/results-thierry.pdf", false);
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
    private static Series2D getSeries(	CSVFile file, BenchmarkDataset dataset) throws ParseException {

        // Select data for the given algorithm
    	SelectorBuilder<String[]> builder = file.getSelectorBuilder()
												.field("Dataset")
												.equals(dataset.toString());

        // Create series
        Series2D series = new Series2D(file, builder.build(), new Field("Utility"), new Field("Test"));
        
        // Done
        return series;
	}
}