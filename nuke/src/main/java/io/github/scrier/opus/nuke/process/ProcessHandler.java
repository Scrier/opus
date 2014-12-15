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
