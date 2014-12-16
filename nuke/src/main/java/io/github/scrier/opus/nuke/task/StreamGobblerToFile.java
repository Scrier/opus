package io.github.scrier.opus.nuke.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamGobblerToFile extends StreamGobbler {

	private static Logger log = LogManager.getLogger(StreamGobblerToFile.class);
	
	private PrintWriter out;
	
	public StreamGobblerToFile(File target) throws IOException {
		super(null);
		// FileWriter is targeting the buffer to append (second argument)
		// BufferedWriter is because FileWriter is expensive.
		// PrintWriter is for ease of printing information to BufferedWriter
		out = new PrintWriter(new BufferedWriter(new FileWriter(target, true)));
	}

	public StreamGobblerToFile(InputStream is, File target) throws FileNotFoundException {
		super(is);
		out = new PrintWriter(target);
	}

	@Override
	public void handleLine(String line) {
		out.write(line);
	}

	@Override
  public void onExit() {
		log.trace("onExit()");
	  out.close();
  }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "StreamGobblerToFile{is: " + getInputStream() + ", out:" + out + "}";
	}

}
