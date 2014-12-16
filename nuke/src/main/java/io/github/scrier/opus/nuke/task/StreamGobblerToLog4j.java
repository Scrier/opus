package io.github.scrier.opus.nuke.task;

import java.io.InputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamGobblerToLog4j extends StreamGobbler {

	public final Logger log;
	
	private Level logLevel;
	
	public StreamGobblerToLog4j(int process) {
		this(null, "DEBUG", process);
	}
	
	public StreamGobblerToLog4j(String level, int process) {
		this(null, level, process);
	}
	
	public StreamGobblerToLog4j(InputStream is, String level, int process) {
	  super(is);
	  log = LogManager.getLogger("StreamGobbler:process-" + process);
	  selectLogLevel(level);
  }

	@Override
  public void handleLine(String line) {
	  log.log(logLevel, line);
  }

	@Override
  public void onExit() {
		log.log(logLevel, "onExit()");
  }
	
	public void selectLogLevel(String level) {
		if( level.matches("(?i:TRACE)") ) {
			logLevel = Level.TRACE;
		} else if( level.matches("(?i:DEBUG)") ) {
			logLevel = Level.DEBUG;
		} else if( level.matches("(?i:INFO)") ) {
			logLevel = Level.INFO;
		} else if( level.matches("(?i:WARN)") ) {
			logLevel = Level.WARN;
		} else if( level.matches("(?i:ERROR)") ) {
			logLevel = Level.ERROR;
		} else if( level.matches("(?i:FATAL)") ) {
			logLevel = Level.FATAL;
		} else {
			logLevel = Level.DEBUG;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "StreamGobblerToLog4j{is: " + getInputStream() + ", logLevel:" + logLevel + "}";
	}

}
