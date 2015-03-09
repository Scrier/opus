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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;
import io.github.scrier.opus.duke.commander.Context;
import io.github.scrier.opus.duke.commander.ICommandCallback;
import io.github.scrier.opus.duke.commander.INukeInfo;

/**
 * State handling for Ramping Down transactions.
 * @author andreas.joelsson
 * {@code
 * RAMPING_DOWN -> TERMINATE
 * RAMPING_DOWN -> ABORTED
 * RAMPIND_DOWN -> COMPLETED
 * }
 */
public class RampingDown extends State implements ICommandCallback {

	private static Logger log = LogManager.getLogger(RampingDown.class);

	private static int RAMPDOWN_UPDATE_SECONDS = 5;

	private int oldUsers;
	private boolean doOnce;
	private List<Long> activeNukeCommands;
	private int rampDownUpdateSeconds;	///< Update interval for calculating rampdown

	private Context theContext = Context.INSTANCE;

	public RampingDown(ClusterDistributorProcedure parent) {
		this(parent, RAMPDOWN_UPDATE_SECONDS);
	}

	public RampingDown(ClusterDistributorProcedure parent, int rampDownSeconds) {
		super(parent);
		setOldUsers(-1);
		setDoOnce(true);
		setActiveNukeCommands(new ArrayList<Long>());
		setRampDownUpdateSeconds(rampDownSeconds);
	}

	/**
	 * RampingDown handling on init methods.
	 */
	@Override
	public void init() {
		log.trace("init()");
		handleTimerTick();
	}

	/**
	 * RampingDown handling on update methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void updated(BaseDataC data)  {
		log.trace("updated(" + data + ")");
		assertState();
	}  

	/**
	 * RampingDown handling on evicted methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void evicted(BaseDataC data) {
		log.trace("evicted(" + data + ")");
		assertState();
	}

	/**
	 * RampingDown handling on removed methods.
	 * @param key Long
	 */
	@Override
	public void removed(Long key) {
		log.trace("removed(" + key + ")");
		assertState();
	}

	/**
	 * RampingDown handling on timeout methods.
	 * @param id long
	 */
	@Override
	public void timeout(long id) {
		log.trace("timeout(" + id + ")");
		assertState();
		if( id == getTimerID() ) {
			handleTimerTick();
		} else if ( id == getTerminateID() ) {
			log.error("Received terminate timeout during state RAMPING_DOWN.");
			setState(TERMINATING);
		} else {
			log.error("Received unknown timer id: " + id + " in state RAMPING_DOWN.");
			throw new RuntimeException("Received unknown timer id: " + id + " in state RAMPING_DOWN.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finished(long nukeID, long processID, int state, String query, String result) {
		log.trace("finished(" + nukeID + ", " + processID + ", " + state + ", " + query + ", " + result + ")");
		if( COMPLETED == state ) {
			log.info("Stop Execution command received ok from node " + nukeID + " still " + (getActiveNukeCommands().size() - 1) + " remaining.");
			if( getActiveNukeCommands().contains(nukeID) ) {
				getActiveNukeCommands().remove(nukeID);
				if( true != isTimeoutActive(getTimerID()) ) {
					log.info("Starting the first timeout for RAMPING_DOWN class as we received the first command acceptance.");
					startTimeout(getRampDownUpdateSeconds(), getTimerID());
				}
			} else {
				log.fatal("Received status from unknown nuke with id: " + nukeID + ".");
				throw new RuntimeException("Received status from unknown nuke with id: " + nukeID + ".");
			}
		} else if ( TERMINATING == getState() ) {
			// better handling later?
			log.debug("Received finish from nukeid " + nukeID + " when we already changed state.");
		} else {
			log.fatal("Received finish from nukeid " + nukeID + " but unhandled state: " + state + ".");
			throw new RuntimeException("Received finish from nukeid " + nukeID + " but unhandled state: " + state + ".");
		}
	}

	/**
	 * Handle timer ticks.
	 */
	private void handleTimerTick() {
		log.trace("handleTimerTick()");
		if( true == isDoOnce() ) {
			setOldUsers(getMaxUsers());
			List<INukeInfo> nukes = theContext.getNukes();
			log.info("Sending stop command to " + nukes.size() + " nukes.");
			for( INukeInfo info : nukes ) {
				//TODO Stop command to be send.
				getActiveNukeCommands().add(info.getNukeID());
			}
			if( false == isTimeoutActive(getTimerID()) ) {
				startTimeout(getRampDownUpdateSeconds(), getTimerID());
			}
			setDoOnce(false);
		} else {
			int activeUsers = getDistributedNumberOfUsers();
			if( activeUsers != getOldUsers() ) {
				log.info("Ramping down from " + getOldUsers() + " to " + activeUsers + ".");
				setOldUsers(activeUsers);
			} else {
				log.info("Ramping down unchanged at " + getOldUsers() + ".");
			}
			if( 0 == activeUsers && 0 == getActiveNukeCommands().size() ) {
				log.info("All users ramped down. we are done.");
				setState(COMPLETED);
			} else {
				log.info("We have " + activeUsers + " active and waiting for " + getActiveNukeCommands().size() + " stop commands.");
				startTimeout(getRampDownUpdateSeconds(), getTimerID());
			}
		}
	}

	/**
	 * Method to get the number of users from the distributed nukes.
	 * @return int
	 */
	protected int getDistributedNumberOfUsers() {
		log.trace("getDistributedNumberOfUsers()");
		int retValue = 0;
		for( INukeInfo info : theContext.getNukes() ) {
			retValue += info.getNoOfThreads();
		}
		return retValue;
	}

	/**
	 * @return the oldUsers
	 */
	public int getOldUsers() {
		return oldUsers;
	}

	/**
	 * @param oldUsers the oldUsers to set
	 */
	public void setOldUsers(int oldUsers) {
		this.oldUsers = oldUsers;
	}

	/**
	 * @return the doOnce
	 */
	public boolean isDoOnce() {
		return doOnce;
	}

	/**
	 * @param doOnce the doOnce to set
	 */
	public void setDoOnce(boolean doOnce) {
		this.doOnce = doOnce;
	}

	/**
	 * @return the activeNukeCommands
	 */
	public List<Long> getActiveNukeCommands() {
		return activeNukeCommands;
	}

	/**
	 * @param activeNukeCommands the activeNukeCommands to set
	 */
	public void setActiveNukeCommands(List<Long> activeNukeCommands) {
		this.activeNukeCommands = activeNukeCommands;
	}

	/**
	 * @return the rampDownUpdateSeconds
	 */
	public int getRampDownUpdateSeconds() {
		return rampDownUpdateSeconds;
	}

	/**
	 * @param rampDownUpdateSeconds the rampDownUpdateSeconds to set
	 */
	public void setRampDownUpdateSeconds(int rampDownUpdateSeconds) {
		this.rampDownUpdateSeconds = rampDownUpdateSeconds;
	}

	/**
	 * Method to assure that we are called in the correct state.
	 */
	private void assertState() {
		if( RAMPING_DOWN != getState() ) {
			log.error("Called state RAMPING_DOWN(" + RAMPING_DOWN + "), when in state " + getState() + ".");
			throw new RuntimeException("Called state RAMPING_DOWN(" + RAMPING_DOWN + "), when in state " + getState() + ".");
		} 
	}

}
