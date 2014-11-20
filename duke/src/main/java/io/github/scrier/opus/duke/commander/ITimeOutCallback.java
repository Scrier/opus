package io.github.scrier.opus.duke.commander;

public interface ITimeOutCallback {
	
	/**
	 * Method called after a timeout is trigged.
	 * @param id long with the id scheduled.
	 */
	public abstract void timeOutTriggered(long id);

}
