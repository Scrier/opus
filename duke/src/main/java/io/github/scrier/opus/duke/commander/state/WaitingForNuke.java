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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;

/**
 * State handling for RampingUp transactions.
 * @author andreas.joelsson
 * {@code
 * WAITING_FOR_NUKE -> ABORTED
 * WAITING_FOR_NUKE -> RAMPING_UP
 * }
 */
public class WaitingForNuke extends State {

	private static Logger log = LogManager.getLogger(WaitingForNuke.class);
	
	private final static int DEFAULT_WAITING_FOR_NUKE_TIMEOUT = 5;
	
	private int waitingForNukeTimeout;
	
	/**
	 * Constructor
	 * @param parent ClusterDistributorProcedure instance.
	 */
	public WaitingForNuke(ClusterDistributorProcedure parent) {
	  this(parent, DEFAULT_WAITING_FOR_NUKE_TIMEOUT);
  }
	
	/**
	 * Constructor
	 * @param parent ClusterDistributorProcedure
	 * @param waitingForNukeTimeout int with the timeout to wait for a timeout. 
	 */
	public WaitingForNuke(ClusterDistributorProcedure parent, int waitingForNukeTimeout) {
	  super(parent);
	  setWaitingForNukeTimeout(waitingForNukeTimeout);
  }
	
	/**
	 * WaitingForNuke handling on init methods.
	 */
	@Override
	public void init() {
		log.trace("init()");
		log.info("Starting timeout for waiting for nukes every " + getWaitingForNukeTimeout() + " second.");
		startTimeout(getWaitingForNukeTimeout(), getTimerID());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() {
		log.trace("shutDown()");
		if( isTimeoutActive(getTimerID()) ) {
			log.info("Terminating timer with id: " + getTimerID() + ".");
			terminateTimeout(getTimerID());
		}
	}
	
	/**
	 * WaitingForNuke handling on update methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void updated(BaseDataC data)  {
		log.trace("updated(" + data + ")");
		assertState();
	}  

	/**
	 * WaitingForNuke handling on evicted methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void evicted(BaseDataC data) {
		log.trace("evicted(" + data + ")");
		assertState();
	}

	/**
	 * WaitingForNuke handling on removed methods.
	 * @param key Long
	 */
	@Override
	public void removed(Long key) {
		log.trace("removed(" + key + ")");
		assertState();
	}

	/**
	 * WaitingForNuke handling on timeout methods.
	 * @param id long
	 */
	@Override
	public void timeout(long id) {
		log.trace("timeout(" + id + ")");
		assertState();
		if( id == getTimerID() ) {
			handleTimerTick();
		} else if ( id == getTerminateID() ) {
			log.error("Received terminate timeout during state WAITING_FOR_NUKE.");
			setState(ABORTED); // not terminating as nothing needs to be terminated.
		} else {
			log.error("Received unknown timer id: " + id + " in state WAITING_FOR_NUKE.");
			setState(ABORTED);
		}
	}
	
	/**
	 * Method to handle next timer tick to create new instances of commands to execute.
	 */
	private void handleTimerTick() {
		log.trace("handleTimerTick()");
		if( true != isNukesReady() ) {
			log.info("Still waiting for nukes, starting new wait timer for " + getWaitingForNukeTimeout() + " seconds.");
			startTimeout(getWaitingForNukeTimeout(), getTimerID());
		} else {
			log.info("Changing state from WAITING_FOR_NUKE to RAMPING_UP.");
			setState(RAMPING_UP);
		}
	}

	/**
	 * @return the waitingForNukeTimeout
	 */
  public int getWaitingForNukeTimeout() {
	  return waitingForNukeTimeout;
  }

	/**
	 * @param waitingForNukeTimeout the waitingForNukeTimeout to set
	 */
  public void setWaitingForNukeTimeout(int waitingForNukeTimeout) {
	  this.waitingForNukeTimeout = waitingForNukeTimeout;
  }
  
  /**
   * Method to assure that we are called in the correct state.
   */
  private void assertState() {
  	if( WAITING_FOR_NUKE != getState() ) {
			log.error("Called state WAITING_FOR_NUKE(" + WAITING_FOR_NUKE + "), when in state " + getState() + ".");
			throw new RuntimeException("Called state WAITING_FOR_NUKE(" + WAITING_FOR_NUKE + "), when in state " + getState() + ".");
		} 
  }

}
