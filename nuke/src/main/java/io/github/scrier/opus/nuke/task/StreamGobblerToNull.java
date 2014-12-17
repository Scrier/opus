package io.github.scrier.opus.nuke.task;

import java.io.InputStream;

public class StreamGobblerToNull extends StreamGobbler {

	public StreamGobblerToNull(InputStream is) {
	  super(is);
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void handleLine(String line) {
		// do nothing.
  }

	@Override
  public void onExit() {
	  // do nothing
  }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "StreamGobblerToNull{is: " + getInputStream() + "}";
	}

}
