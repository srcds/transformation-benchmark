/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.r;
import java.io.File;

import org.deidentifier.arx.benchmark.BenchmarkSetup;

/**
 * OS-specific functions for finding the R executable
 * 
 * @author Fabian Prasser
 * @author Alexander Beischl
 * @author Thuy Tran
 */
public class OS {

    /**
     * Enum for the OS type
     * 
     * @author Fabian Prasser
     */
    public static enum OSType {
        WINDOWS
    }
    
	/** Locations*/
	//TODO change to version independent wildcard matching
	private static String[] locationsWindows = {"C:\\Program Files\\R\\R-3.5.0\\bin",
			                                    "C:\\Program Files\\R\\R-3.3.2\\bin",
	                                            "C:\\Program Files\\R\\R-2.1.5.1\\bin",
	                                            BenchmarkSetup.LOCATION_OF_R}; 
    /** Executables*/
	private static String[] executablesWindows = {"R.exe"};
	
	/**
     * Returns the OS
     * @return
     */
    public static OSType getOS() {

        String os = System.getProperty("os.name").toLowerCase();

        if (os.indexOf("win") >= 0) {
            return OSType.WINDOWS;
        } else {
            throw new IllegalStateException("Unsupported operating system");
        }
    }

    /**
	 * Returns a path to the R executable or null if R cannot be found
	 * @return
	 */
	public static String getR() {
	    switch (getOS()) {
	    case WINDOWS:
	        return getPath(locationsWindows, executablesWindows);
	    default:
	        throw new IllegalStateException("Unknown operating system");
	    }
	}

    /**
     * Returns a path to the R executable or null if R cannot be found
     * @param folder The folder to look in
     * @return
     */
    public static String getR(String folder) {
        switch (getOS()) {
        case WINDOWS:
            return getPath(new String[]{folder}, executablesWindows);
        default:
            throw new IllegalStateException("Unknown operating system");
        }
    }
    
    /**
     * Returns the path of the R executable or null if R cannot be found
     * @return
     */
    private static String getPath(String[] locations, String[] executables) {
        
        // For each location
        for (String location : locations) {
            if (!location.endsWith(File.separator)) {
                location += File.separator;
            }
            
            // For each name of the executable
            for (String executable : executables) {
                try {
                    
                    // Check whether the file exists
                    File file = new File(location + executable);
                    if (file.exists()) {
                        
                        // Check if we have the permissions to run the file
                        ProcessBuilder builder = new ProcessBuilder(file.getCanonicalPath(), "--vanilla");
                        builder.start().destroy();
                        
                        // Return
                        return file.getCanonicalPath();
                    }
                } catch (Exception e) {
                    // Ignore: try the next location
                }
            }
        }
        
        // We haven't found anything
        return null;
    }

    /**
     * Returns the parameters for the R process
     * @param path 
     * @return
     */
    public static String[] getParameters(String path) {
        switch (getOS()) {
        case WINDOWS:
            return new String[]{path, "--vanilla", "--quiet", "--ess"};
        default:
            throw new IllegalStateException("Unknown operating system");
        }
       
    }

	public static String[] getPossibleExecutables() {
		switch (getOS()) {
		case WINDOWS:
			return executablesWindows;
		default:
			throw new IllegalStateException("Unknown operating system");
		}
	}
}
