package io.github.scrier.opus.nuke.process;

import java.io.File;
import java.io.IOException;

public class ProcessHandler {
	
	private ProcessBuilder processBuilder;
	private String[] args;
	
	/**
	 * Constructor
	 * @param args String[]
	 */
	public ProcessHandler(String[] args) {
		this.args = args;
		processBuilder = new ProcessBuilder(this.args);
	}
	
	/**
	 * Method to set working directory
	 * @param workdir File
	 * @return ProcessBuilder
	 */
	public ProcessBuilder directory(File workdir) {
		return processBuilder.directory(workdir);
	} 
	
	/**
	 * Method to redirect the error stream to standard output.
	 * @param redirect boolean 
	 * @return ProcessBuilder
	 */
	public ProcessBuilder redirectErrorStream(boolean redirect) {
		return processBuilder.redirectErrorStream(redirect);
	}
	
	/**
	 * Method to start the process.
	 * @return Process
	 * @throws IOException
	 */
	public Process start() throws IOException {
		return processBuilder.start();
	}
	
}
