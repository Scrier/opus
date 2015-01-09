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
	public int actualNumberOfThreads;
	
	public NukeInfoMock(int requestedNoOfUsers) {
		this(requestedNoOfUsers, NukeState.RUNNING);
	}
	
	public NukeInfoMock(int requestedNoOfThreads, NukeState infoState) {
		this.requestedNoOfUsersReturned = requestedNoOfThreads;
		this.infoStateReturned = infoState;
		this.nukeIdReturn = ++nukeIdCounter;
  }

	@Override
  public long getNukeID() {
    return nukeIdReturn;
  }

	@Override
  public int getNoOfThreads() {
    return noOfUsersReturn;
  }

	@Override
  public int getRequestedNoOfThreads() {
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
  public int getActualNoOfThreads() {
	  return actualNumberOfThreads;
  }

	@Override
  public void setRequestedNoOfThreads(int threads) {
    this.requestedNoOfUsersReturned = threads;
  }

}
