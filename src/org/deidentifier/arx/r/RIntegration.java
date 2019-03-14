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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Java integration with R. 
 * 
 * @author Fabian Prasser
 */
public class RIntegration {

	/** Debug flag */
	private static final boolean DEBUG = true;

	/** List of all processes launched */
	private static final List<Process> PROCESSES = new ArrayList<Process>();

	/**
	 * Adds a process to the global ist
	 * @param p
	 */
	public static synchronized void addProcess(Process p) {
		PROCESSES.add(p);
	}
    
	/**
	 * Kills all processes in the global list
	 */
	public static synchronized void killProcesses() {
		for (Process p : PROCESSES) {
			try {
				p.destroyForcibly();
			} catch (Exception e) {
				// Nothing we can do about this
			}
		}
	}

	/**
	 * Removes a process from the global list
	 * @param p
	 */
	public static synchronized void removeProcess(Process p) {
		try {
			PROCESSES.remove(p);
		} catch (Exception e) {
			// Nothing we can do about this
		}
	}

	/** Process */
	private Process process;

    /**
	 * Creates a new instance
	 * @param path
	 * @throws IOException 
	 */
	public RIntegration(final String path) throws IOException {
	    
	    // Check args
	    if (path == null) {
	        throw new NullPointerException("Argument must not be null");
	    }
	    
	    // Create process
	    ProcessBuilder builder = new ProcessBuilder(OS.getParameters(path))
	                                 .redirectErrorStream(true); // Redirect stderr to stdout

		// Start
		this.process = builder.start();
		addProcess(this.process);
		final Reader reader = new InputStreamReader(this.process.getInputStream());

		// Attach process to buffer
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int character = reader.read();
					while (character != -1) {
						System.out.print((char)character);
						character = reader.read();
					}
				} catch (Exception e) {
					debug(e);
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}

    /**
	 * Executes a command
	 * 
	 * @param command
	 */
	public void execute(String command) {
	    if (this.process == null) {
	        throw new IllegalStateException("Process already terminated!");
	    }
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream()));
            writer.write(command);
            writer.newLine();
            writer.flush();
    	    System.out.println(command);
        } catch (Exception e) {
            debug(e);
            shutdown(0);
        }
	}
	
	/**
     * Returns whether R is alive
     * @return
     */
    public boolean isAlive() {
        return this.process != null;
    }
	
	/**
	 * Closes R
     * @param timeout Make negative if no timeout should be implemented
	 */
    public void shutdown(final int timeout) {
        if (this.process != null) {
            try {
            	this.execute("quit(save = \"no\", status = 0, runLast = FALSE)");
            	// Kill after timeout
            	if (timeout >= 0) {
                	new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(timeout);
                                RIntegration.this.process.destroyForcibly();
                                RIntegration.this.process.waitFor();
                                removeProcess(RIntegration.this.process);
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                	});
            	}
				this.process.waitFor();
			} catch (InterruptedException e) {
				// Ignore silently
			}
            RIntegration.this.process = null;
        }
    }
	
	/**
	 * Debug helper
	 * @param exception
	 */
	private void debug(Exception exception) {
        if (DEBUG) {
            exception.printStackTrace();
        }
    }
}