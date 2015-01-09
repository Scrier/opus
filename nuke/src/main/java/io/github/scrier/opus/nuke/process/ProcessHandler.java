/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Andreas Joelsson (andreas.joelsson@gmail.com)
 */
package io.github.scrier.opus.nuke.process;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessHandler {
	
	private static Logger log = LogManager.getLogger(ProcessHandler.class);
			
	private ProcessBuilder processBuilder;
	private String[] args;
	
	/**
	 * Constructor
	 * @param args String[]
	 */
	public ProcessHandler(String[] args) {
		log.trace("ProcessHandler(" + args + ")");
		this.args = args;
		processBuilder = new ProcessBuilder(this.args);
	}
	
	/**
	 * Method to set working directory
	 * @param workdir File
	 * @return ProcessBuilder
	 */
	public ProcessBuilder directory(File workdir) {
		log.trace("directory(" + workdir + ")");
		return processBuilder.directory(workdir);
	} 
	
	/**
	 * Method to redirect the error stream to standard output.
	 * @param redirect boolean 
	 * @return ProcessBuilder
	 */
	public ProcessBuilder redirectErrorStream(boolean redirect) {
		log.trace("redirectErrorStream(" + redirect + ")");
		return processBuilder.redirectErrorStream(redirect);
	}
	
	/**
	 * Method to start the process.
	 * @return Process
	 * @throws IOException Thrown from ProcessBuilder call to start().
	 */
	public Process start() throws IOException {
		log.trace("start()");
		return processBuilder.start();
	}
	
}
