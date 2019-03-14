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

package org.deidentifier.arx.utility;

import java.text.ParseException;
import java.util.Arrays;

import org.deidentifier.arx.utility.util.Functions;


/**
 * Implementation of the Loss measure as proposed by Soria-Comas.
 * 
 * @author Fabian Prasser
 */
public class UtilityMeasureSSE extends UtilityMeasure<Double> {
    
    private final String[][] input;
    
    /**
     * Input: the original input dataset without header
     * @param input
     */
    public UtilityMeasureSSE(String[][] input) {
        this.input = input;
    }
    
    
    /**
     * Output: the output dataset without header
     */
    @Override
    public Utility<Double> evaluate(String[][] output, int[] transformation) {
        try {
            return new UtilityDouble(Functions.calculateIL(Arrays.asList(this.input), Arrays.asList(output)));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
