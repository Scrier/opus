package io.github.scrier.opus.duke.commander.state;

import io.github.scrier.opus.common.nuke.NukeState;
import io.github.scrier.opus.duke.commander.INukeInfo;

public class NukeInfoMock  implements INukeInfo {
	
	static long nukeIdCounter = 0;
	
	public long nukeIdReturn;
	public int noOfUsersReturn;
	public int requestedNoOfUsersReturned;
	public NukeState infoStateReturned;
	public int noOfActiveCommandsReturned;
	public int noOfRequestedCommandsReturned;
	public int noOfCompletedCommandsReturned;
	
	public NukeInfoMock(int requestedNoOfUsers) {
		this(requestedNoOfUsers, NukeState.RUNNING);
	}
	
	public NukeInfoMock(int requestedNoOfUsers, NukeState infoState) {
		this.requestedNoOfUsersReturned = requestedNoOfUsers;
		this.infoStateReturned = infoState;
		this.nukeIdReturn = ++nukeIdCounter;
  }

	@Override
  public long getNukeID() {
    return nukeIdReturn;
  }

	@Override
  public int getNoOfUsers() {
    return noOfUsersReturn;
  }

	@Override
  public int getRequestedNoOfUsers() {
    return requestedNoOfUsersReturned;
  }

	@Override
  public NukeState getInfoState() {
    return infoStateReturned;
  }

	@Override
  public int getNoOfActiveCommands() {
    return noOfActiveCommandsReturned;
  }

	@Override
  public int getNoOfRequestedCommands() {
    return noOfRequestedCommandsReturned;
  }

	@Override
  public int getNoOfCompletedCommands() {
    return noOfCompletedCommandsReturned;
  }

	@Override
  public void setRequestedNoOfUsers(int users) {
    this.requestedNoOfUsersReturned = users;
  }

}
