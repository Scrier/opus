package io.github.scrier.opus.nuke.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StreamGobbler extends Thread {

	private static Logger log = LogManager.getLogger(StreamGobbler.class);
	
	private InputStream inputStream;
	
	public StreamGobbler(InputStream is) {
		log.trace("StreamGobbler(" + is + ")");
		setInputStream(is);
	}
	
	/**
	 * Method to handle the output from the gobbler line by line.
	 * @param line String with the next line to handle.
	 */
	public abstract void handleLine(String line);
	
	public abstract void onExit();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			InputStreamReader isReader = new InputStreamReader(getInputStream());
			BufferedReader bReader = new BufferedReader(isReader);
			String line = null;
			while ( (line = bReader.readLine()) != null ) {
				handleLine(line);
			}
		} catch (IOException ioe) {
			log.error("IOException in " + this.getClass().getName() + ".", ioe);
		} finally {
			onExit();
		}
	}

	/**
	 * @return the inputStream
	 */
  public InputStream getInputStream() {
	  return inputStream;
  }

	/**
	 * @param inputStream the inputStream to set
	 */
  public void setInputStream(InputStream inputStream) {
	  this.inputStream = inputStream;
  }
	
}
