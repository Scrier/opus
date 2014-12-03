package io.github.scrier.opus.duke.commander;

import io.github.scrier.opus.common.nuke.NukeState;

public interface INukeInfo {
	
	/**
	 * Method to get the component associated with the info.
	 * @return long
	 */
	public long getNukeID();
	
	/**
	 * Method to get the number of users that is executing.
	 * @return int
	 */
	public int getNoOfUsers();
	
	/**
	 * Method to get the number of users that is requested to execute.
	 * @return int
	 */
	public int getRequestedNoOfUsers();
	
	/**
	 * Method to get the current state of the node.
	 * @return NukeState
	 */
	public NukeState getInfoState();
	
	/**
	 * Method to get the number of active commands that is running.
	 * @return int
	 */
	public int getNoOfActiveCommands();
	
	/**
	 * Method to get the total number of requested commands.
	 * @return int
	 */
	public int getNoOfRequestedCommands();
	
	/**
	 * Method to get the total number of completed commands.
	 * @return int
	 */
	public int getNoOfCompletedCommands();
}
