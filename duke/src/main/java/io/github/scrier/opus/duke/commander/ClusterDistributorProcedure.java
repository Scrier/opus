/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Andreas Joelsson (andreas.joelsson@gmail.com)
 */
package io.github.scrier.opus.duke.commander;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.nuke.NukeState;
import io.github.scrier.opus.duke.commander.state.Aborted;
import io.github.scrier.opus.duke.commander.state.Completed;
import io.github.scrier.opus.duke.commander.state.Created;
import io.github.scrier.opus.duke.commander.state.PeakDelay;
import io.github.scrier.opus.duke.commander.state.RampingDown;
import io.github.scrier.opus.duke.commander.state.RampingUp;
import io.github.scrier.opus.duke.commander.state.State;
import io.github.scrier.opus.duke.commander.state.Terminating;
import io.github.scrier.opus.duke.commander.state.WaitingForNuke;

/**
 * Class that handles the distribution of work between nodes.
 * @author Andreas Joelsson
 */
public class ClusterDistributorProcedure extends BaseDukeProcedure implements ITimeOutCallback {

	private static Logger log = LogManager.getLogger(ClusterDistributorProcedure.class);

	public final int WAITING_FOR_NUKE = CREATED + 1;	///< State handling the waiting for available nuke.
	public final int RAMPING_UP       = CREATED + 2;	///< State handling the ramping up phase.
	public final int PEAK_DELAY       = CREATED + 3;	///< State handling the peak delay phase
	public final int RAMPING_DOWN     = CREATED + 4;	///< State handling the ramping down phase.
	public final int TERMINATING      = CREATED + 5;	///< State when handling terminate phase.

	private int minNodes;	///< minimum number of nodes before we start ramping up.
	private int maxUsers;	///< How many user or commands should be issued before peak.
	private int intervalSeconds;	///< Which interval we should increase active commands.
	private int userIncrease;		///< Number of users increase each interval
	private int peakDelaySeconds;	///< How long in seconds the peak should hold
	private int terminateSeconds;	///< How many seconds from start the application can run before terminating.
	private boolean repeated;		///< Issues if commands should be repeated or not.
	private boolean shutDownOnce;	///< guard for only doing one shutdown.
	private String command;			///< Command to issue to the nodes.
	private String folder;			///< What folder each node should execute the command from
	
	private long timerID;			///< id for the timer tick callback
	private long terminateID;		///< id for the terminate tick callback.

	private State[] states;		///< State array holding logic for each state.
	
	/**
	 * Constructor
	 */
	public ClusterDistributorProcedure() {
		setMinNodes(0);
		setMaxUsers(0);
		setIntervalSeconds(0);
		setUserIncrease(0);
		setPeakDelaySeconds(0);
		setTerminateSeconds(0);
		setRepeated(false);
		setShutDownOnce(true);
		setCommand("");
		setFolder("");
		setTimerID(-1L);
		setTerminateID(-1L);
		setStates(new State[TERMINATING + 1]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		log.trace("init()");
		int noOfProcedures = getCommander().getProcedures(ClusterDistributorProcedure.class).size();
		noOfProcedures += getCommander().getProceduresToAdd(ClusterDistributorProcedure.class).size();
		if( 1 != noOfProcedures ) {
			log.error("We have more than one cluster distributor running, expected 1 but was " + noOfProcedures + ", aborting.");
			setState(ABORTED);
		} else {
			setMinNodes(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_MINIMUM_NODES)));
			setMaxUsers(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_MAX_USERS)));
			setIntervalSeconds(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_INTERVAL)));
			setUserIncrease(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_USER_INCREASE)));
			setPeakDelaySeconds(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_PEAK_DELAY)));
			setTerminateSeconds(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_TERMINATE)));
			setRepeated(Boolean.parseBoolean(getSetting(Shared.Settings.EXECUTE_REPEATED)));
			setCommand(getSetting(Shared.Settings.EXECUTE_COMMAND));
			setFolder(getSetting(Shared.Settings.EXECUTE_FOLDER));
			setTimerID(getUniqueID());
			setTerminateID(getUniqueID());
			states[ABORTED] = new Aborted(this);
			states[CREATED] = new Created(this);
			states[WAITING_FOR_NUKE] = new WaitingForNuke(this);
			states[RAMPING_UP] = new RampingUp(this, getIntervalSeconds()); 
			states[PEAK_DELAY] = new PeakDelay(this);
			states[RAMPING_DOWN] = new RampingDown(this);
			states[TERMINATING] = new Terminating(this);
			int exTime = getExecutionTime();
			if ( exTime > getTerminateSeconds() ) {
				log.error("Calculated execcutiontime: " + Shared.Methods.formatTime(exTime) + " time overlaps the terminate time: " + Shared.Methods.formatTime(getTerminateSeconds()) + ".");
				setState(ABORTED);
			} else {
				log.info("Calculated execution time (excluding rampdown) is " + Shared.Methods.formatTime(exTime) + ".");
				log.info("Starting timeout for execution to go off in " + Shared.Methods.formatTime(getTerminateSeconds()) + ".");
				startTimeout(getTerminateSeconds(), getTerminateID(), this);
				if( true == isNukesReady() ) {
					setState(RAMPING_UP);
				} else {
					setState(WAITING_FOR_NUKE);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() throws Exception {
		log.trace("shutDown()");
		if( true == isShutDownOnce() ) {
			setShutDownOnce(false);
			log.info("Sequence is done, shutting down client");
			theContext.getBaseAoC().shutDown();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnUpdated(BaseDataC data) {
		log.trace("handleOnUpdated(" + data + ")");
		try {
			log.debug("states[" + states[getState()].getClass().getSimpleName() + "].updated(" + data + ");");
			states[getState()].updated(data);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed(this).updated(data);
			} else {
				log.error("Received out of bound exception in state: " + getState() + ".", e);
			}
		}
		return getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnEvicted(BaseDataC data) {
		log.trace("handleOnEvicted(" + data + ")");
		try {
			log.debug("states[" + states[getState()].getClass().getSimpleName() + "].evicted(" + data + ");");
			states[getState()].evicted(data);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed(this).evicted(data);
			} else {
				log.error("Received out of bound exception in state: " + getState() + ".", e);
			}
		}
		return getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnRemoved(Long key) {
		log.trace("handleOnRemoved(" + key + ")");
		try {
			log.debug("states[" + states[getState()].getClass().getSimpleName() + "].removed(" + key + ");");
			states[getState()].removed(key);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed(this).removed(key);
			} else {
				log.error("Received out of bound exception in state: " + getState() + ".", e);
			}
		}
		return getState();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void timeOutTriggered(long id) {
		log.trace("timeOutTriggered(" + id + ")");
		try {
			log.debug("states[" + states[getState()].getClass().getSimpleName() + "].timeout(" + id + ");");
			states[getState()].timeout(id);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed(this).timeout(id);
			} else {
				log.error("Received out of bound exception in state: " + getState() + ".", e);
			}
		}
		theContext.getCommander().intializeProcedures();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStateChanged(int state) {
		log.trace("onStateChanged(" + state + ")");
		try {
			log.debug("states[" + states[state].getClass().getSimpleName() + "].init();");
			states[getState()].init();
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed(this).init();
			} else {
				log.error("Received out of bound exception in state: " + getState() + ".", e);
			}
		}
	}

	/**
	 * @return the minNodes
	 */
	protected int getMinNodes() {
		return minNodes;
	}

	/**
	 * @param minNodes the minNodes to set
	 */
	private void setMinNodes(int minNodes) {
		this.minNodes = minNodes;
	}

	/**
	 * @return the maxUsers
	 */
	public int getMaxUsers() {
		return maxUsers;
	}

	/**
	 * @param maxUsers the maxUsers to set
	 */
	private void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}

	/**
	 * @return the intervalSeconds
	 */
	protected int getIntervalSeconds() {
		return intervalSeconds;
	}

	/**
	 * @param intervalSeconds the intervalSeconds to set
	 */
	private void setIntervalSeconds(int intervalSeconds) {
		this.intervalSeconds = intervalSeconds;
	}

	/**
	 * @return the userIncrease
	 */
	public int getUserIncrease() {
		return userIncrease;
	}

	/**
	 * @param userIncrease the userIncrease to set
	 */
	private void setUserIncrease(int userIncrease) {
		this.userIncrease = userIncrease;
	}

	/**
	 * @return the peakDelaySeconds
	 */
	public int getPeakDelaySeconds() {
		return peakDelaySeconds;
	}

	/**
	 * @param peakDelaySeconds the peakDelaySeconds to set
	 */
	private void setPeakDelaySeconds(int peakDelaySeconds) {
		this.peakDelaySeconds = peakDelaySeconds;
	}

	/**
	 * @return the terminateSeconds
	 */
	protected int getTerminateSeconds() {
		return terminateSeconds;
	}

	/**
	 * @param terminateSeconds the terminateSeconds to set
	 */
	private void setTerminateSeconds(int terminateSeconds) {
		this.terminateSeconds = terminateSeconds;
	}

	/**
	 * @return the repeated
	 */
	public boolean isRepeated() {
		return repeated;
	}

	/**
	 * @param repeated the repeated to set
	 */
	private void setRepeated(boolean repeated) {
		this.repeated = repeated;
	}

	/**
	 * @return the shutDownOnce
	 */
	protected boolean isShutDownOnce() {
	  return shutDownOnce;
  }

	/**
	 * @param shutDownOnce the shutDownOnce to set
	 */
	private void setShutDownOnce(boolean shutDownOnce) {
	  this.shutDownOnce = shutDownOnce;
  }

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	private void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the folder
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * @param folder the folder to set
	 */
	private void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * @return the timerID
	 */
	public long getTimerID() {
		return timerID;
	}

	/**
	 * @param timerID the timerID to set
	 */
	private void setTimerID(long timerID) {
		this.timerID = timerID;
	}

	/**
	 * @return the terminateID
	 */
	public long getTerminateID() {
	  return terminateID;
  }

	/**
	 * @param terminateID the terminateID to set
	 */
	private void setTerminateID(long terminateID) {
	  this.terminateID = terminateID;
  }
	
	/**
	 * Method used for testing.
	 * @return
	 */
	public void setStates(State[] states) {
		this.states = states;
	}
	
	/**
	 * Method used for testing.
	 * @return
	 */
	protected State[] getStates() {
		return states;
	}
  
  /**
   * Method to check that the correct number of nukes are in correct state.
   * @return boolean
   */
  public boolean isNukesReady() {
  	log.trace("isNukesReady()");
  	log.debug((getMinNodes() <= theContext.getNukes(NukeState.RUNNING).size()) + " = " + getMinNodes() + " <= " + theContext.getNukes(NukeState.RUNNING).size() + ".");
  	return (getMinNodes() <= theContext.getNukes(NukeState.RUNNING).size());
  }
	
	/**
	 * Method to approximate the execution time of the test.
	 * @return int in seconds.
	 */
	protected int getExecutionTime() {
		log.trace("getExecutionTime()");
		int intervals = (int)((getMaxUsers() / getUserIncrease())) + (getMaxUsers() % getUserIncrease() > 0 ? 1 : 0);
		log.debug(intervals + " = " + (int)((getMaxUsers() / getUserIncrease())) + " + " + (getMaxUsers() % getUserIncrease() > 0 ? 1 : 0));
		int retValue = intervals * getIntervalSeconds();
		log.debug(retValue + " = " + intervals + " * " + getIntervalSeconds() + ")");
		retValue += getPeakDelaySeconds();
		return retValue;
	}
	
	/**
	 * Method to start a timer from the states.
	 * @param time int with the time in seconds to start.
	 * @param timerID long with the id of the timer.
	 */
	public void startTimeout(int time, long timerID) {
		log.trace("startTimeout(" + time + ", " + timerID + ")");
		startTimeout(time, timerID, this);
	}

}
