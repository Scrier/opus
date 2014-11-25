package io.github.scrier.opus.duke.commander;

public interface ICommandCallback {
	
	/**
	 * Method called when the procedure is terminated.
	 * @param nukeID the id of the nuke that has completed the command.
	 * @param state the state when terminated.
	 * @param query String with the query performed.
	 * @param result String with the result of the Query.
	 */
	public void finished(long nukeID, int state, String query, String result);
	
}
