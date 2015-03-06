/**
8 * Licensed under the Apache License, Version 2.0 (the "License");
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

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;
import io.github.scrier.opus.duke.commander.CommandProcedure;
import io.github.scrier.opus.duke.commander.Context;
import io.github.scrier.opus.duke.commander.ICommandCallback;
import io.github.scrier.opus.duke.commander.INukeInfo;

/**
 * State handling for Ramping Down transactions.
 * @author andreas.joelsson
 * {@code
 * TERMINATE -> ABORTED
 * TERMINATE -> COMPLETED
 * }
 */
public class Terminating extends State implements ICommandCallback {

	private static Logger log = LogManager.getLogger(Terminating.class);
	
	private static int TERMINATE_UPDATE_SECONDS = 30;

	private int terminateTimeout;
	private List<Long> activeNukeCommands;
	private Context theContext = Context.INSTANCE;
	
	public Terminating(ClusterDistributorProcedure parent) {
	  this(parent, TERMINATE_UPDATE_SECONDS);
  }
	
	public Terminating(ClusterDistributorProcedure parent, int terminateTimeout) {
	  super(parent);
		setTerminateTimeout(terminateTimeout);
		setActiveNukeCommands(new ArrayList<Long>());
  }
	
	/**
	 * Terminating handling on init methods.
	 */
	@Override
	public void init() {
		log.trace("init()");
		List<INukeInfo> nukes = theContext.getNukes();
		log.info("Sending terminate command to " + nukes.size() + " nukes will terminate this applicatio in " + getTerminateTimeout() + " seconds.");
		for( INukeInfo info : nukes ) {
			registerProcedure(new CommandProcedure(info.getNukeID(), Shared.Commands.Execute.TERMINATE_EXECUTION, this));
			getActiveNukeCommands().add(info.getNukeID());
		}
		startTimeout(getTerminateTimeout(), getTimerID());
	}
	
	/**
	 * Terminating handling on update methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void updated(BaseDataC data)  {
		log.trace("updated(" + data + ")");
		assertState();
	}  

	/**
	 * Terminating handling on evicted methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void evicted(BaseDataC data) {
		log.trace("evicted(" + data + ")");
		assertState();
	}

	/**
	 * Terminating handling on removed methods.
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
			log.fatal("Received terminate timeout during state TERMINATING.");
			throw new RuntimeException("Received terminate timeout during state TERMINATING.");
		} else {
			log.fatal("Received unknown timer id: " + id + " in state TERMINATING.");
			throw new RuntimeException("Received terminate timeout during state TERMINATING.");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public void finished(long nukeID, int state, String query, String result) {
		log.trace("finished(" + nukeID + ", " + state + ", " + query + ", " + result + ")");
		if( COMPLETED == state ) {
			log.info("Stop Execution command received ok from node " + nukeID + " still " + (getActiveNukeCommands().size() - 1) + " remaining.");
			if( getActiveNukeCommands().contains(nukeID) ) {
				getActiveNukeCommands().remove(nukeID);
				if( true == getActiveNukeCommands().isEmpty() ) {
					log.info("All nukes has terminated successful, we are done.");
					theContext.terminateTimeout(getTimerID());
					setState(COMPLETED);
				}
			} else {
				log.fatal("Received status from unknown nuke with id: " + nukeID + ".");
				throw new RuntimeException("Received status from unknown nuke with id: " + nukeID + ".");
			}
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
		log.fatal("Received timeout of termination before nukes has terminated their processes, terminating.");
		throw new RuntimeException("Received timeout of termination before nukes has terminated their processes, terminating.");
	}

	/**
	 * @return the terminateTimeout
	 */
  public int getTerminateTimeout() {
    return terminateTimeout;
  }

	/**
	 * @param terminateTimeout the terminateTimeout to set
	 */
  public void setTerminateTimeout(int terminateTimeout) {
    this.terminateTimeout = terminateTimeout;
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
	 * Method to assure that we are called in the correct state.
	 */
	private void assertState() {
		if( TERMINATING != getState() ) {
			log.error("Called state TERMINATING(" + TERMINATING + "), when in state " + getState() + ".");
			throw new RuntimeException("Called state TERMINATING(" + TERMINATING + "), when in state " + getState() + ".");
		} 
	}

}
