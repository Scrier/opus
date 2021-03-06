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
package io.github.scrier.opus.duke.commander.state;

import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.duke.commander.BaseDukeProcedure;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base state for the FSM logic.
 * @author andreas.joelsson
 */
public abstract class State {
	
	private static Logger log = LogManager.getLogger(State.class);
	
	public final int ABORTED;
	public final int CREATED;
	public final int WAITING_FOR_NUKE;
	public final int RAMPING_UP;
	public final int PEAK_DELAY;
	public final int RAMPING_DOWN;
	public final int TERMINATING;
	public final int COMPLETED;
	
	private ClusterDistributorProcedure parent;
	private long timerID;
	
	public State(ClusterDistributorProcedure parent) {
		ABORTED = parent.ABORTED;
		CREATED = parent.CREATED;
		WAITING_FOR_NUKE = parent.WAITING_FOR_NUKE;
		RAMPING_UP = parent.RAMPING_UP;
		PEAK_DELAY = parent.PEAK_DELAY;
		RAMPING_DOWN = parent.RAMPING_DOWN;
		TERMINATING = parent.TERMINATING;
		COMPLETED = parent.COMPLETED;
		setParent(parent);
		setTimerID(parent.getUniqueID());
	}
	
	/**
	 * Method called each state change.
	 */
	public void init() {
		log.trace("init()");
		log.error("Default init state setting aborted from state: " + getState() + "."); 
		setState(ABORTED); 
	}
	
	/**
	 * Base handling on shutDown methods.
	 */
	public void shutDown() {
		log.trace("shutDown()");
		log.error("Default shutDown state setting aborted from state: " + getState() + "."); 
		setState(ABORTED); 
	}
	
	/**
	 * Base handling on update methods.
	 * @param data BaseNukeC
	 */
	public void updated(BaseDataC data)  {
		log.trace("updated(" + data + ")");
		log.error("Default update state setting aborted from state: " + getState() + "."); 
		setState(ABORTED); 
	}  

	/**
	 * Base handling on evicted methods.
	 * @param data BaseNukeC
	 */
	public void evicted(BaseDataC data) {
		log.trace("evicted(" + data + ")");
		log.error("Default update state setting aborted from state: " + getState() + ".");
		setState(ABORTED); 
	}

	/**
	 * Base handling on removed methods.
	 * @param key Long
	 */
	public void removed(Long key) {
		log.trace("removed(" + key + ")");
		log.error("Default update state setting aborted from state: " + getState() + ".");
		setState(ABORTED); 
	}

	/**
	 * Base handling on timeout methods.
	 * @param id long
	 */
	public void timeout(long id) {
		log.trace("timeout(" + id + ")");
		log.error("Default update state setting aborted from state: " + getState() + ".");
		setState(ABORTED); 
	}
	
	/**
	 * Method to get the name of the state for debugging.
	 * @return String with the correct state.
	 */
	public String getClassName() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * Propagated method from parent.
	 * @param state int with the state to set in parent.
	 */
	protected void setState(int state) {
		getParent().setState(state);
	}
	
	/**
	 * Propagated method from parent.
	 * @return the state from the parent.
	 */
	protected int getState() {
		return getParent().getState();
	}
	
	/**
	 * Method to start a timer in the parent.
	 * @param seconds number of seconds in the parent.
	 * @param timerID the id of the timer.
	 */
	protected void startTimeout(int seconds, long timerID) {
		parent.startTimeout(seconds, timerID);
	}
	
	/**
	 * Method to terminate the timeout for a given id.
	 * @param timerID long
	 */
	protected void terminateTimeout(long timerID) {
		parent.terminateTimeout(timerID);
	}
	
	/**
	 * Propagated method from parent
	 * @return long with the terminate id.
	 */
	protected long getTerminateID() {
		return parent.getTerminateID();
	}
	
	/**
	 * Propagated method from parent
	 * @return boolean with the status of nukes.
	 */
	protected boolean isNukesReady() {
		return parent.isNukesReady();
	}
	
	/**
	 * Propagated method from parent
	 * @return int with the users to increase each interval.
	 */
	protected int getUserIncrease() {
		return parent.getUserIncrease();
	}
	
	/**
	 * Propagated method from parent
	 * @return int with the maximum number of users to ramp up to.
	 */
	protected int getMaxUsers() {
		return parent.getMaxUsers();
	}
	
	/**
	 * @return the folder
	 */
	public String getFolder() {
		return parent.getFolder();
	}
	
	/**
	 * @return the command
	 */
	protected String getCommand() {
		return parent.getCommand();
	}
	
	/**
	 * @return the repeated
	 */
	protected boolean isRepeated() {
		return parent.isRepeated();
	}
	
	/**
	 * @return the peakDelaySeconds
	 */
	protected int getPeakDelaySeconds() {
		return parent.getPeakDelaySeconds();
	}
	
	/**
	 * Method to check if a specific timeout is active.
	 * @param id long with the id of the timeout to check for.
	 * @return boolean
	 */
  public boolean isTimeoutActive(long id) {
  	return parent.isTimeoutActive(id);
  }
	
	protected boolean registerProcedure(BaseDukeProcedure procedure) {
		return parent.registerProcedure(procedure);
	}
	
	/**
	 * @return the parent
	 */
	private ClusterDistributorProcedure getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	private void setParent(ClusterDistributorProcedure parent) {
		this.parent = parent;
	}

	/**
	 * @param timerID the timerID to set
	 */
  public void setTimerID(long timerID) {
	  this.timerID = timerID;
  }
  
	/**
	 * @return the timerID
	 */
  public long getTimerID() {
	  return timerID;
  }
	
}
