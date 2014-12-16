package io.github.scrier.opus.duke.commander.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
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
	
	public WaitingForNuke(ClusterDistributorProcedure parent) {
	  this(parent, DEFAULT_WAITING_FOR_NUKE_TIMEOUT);
  }
	
	public WaitingForNuke(ClusterDistributorProcedure parent, int waitingForNukeTimeout) {
	  super(parent);
	  setWaitingForNukeTimeout(DEFAULT_WAITING_FOR_NUKE_TIMEOUT);
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
	 * WaitingForNuke handling on update methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void updated(BaseNukeC data)  {
		log.trace("updated(" + data + ")");
	}  

	/**
	 * WaitingForNuke handling on evicted methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void evicted(BaseNukeC data) {
		log.trace("evicted(" + data + ")");
	}

	/**
	 * WaitingForNuke handling on removed methods.
	 * @param key Long
	 */
	@Override
	public void removed(Long key) {
		log.trace("removed(" + key + ")");
	}

	/**
	 * WaitingForNuke handling on timeout methods.
	 * @param id long
	 */
	@Override
	public void timeout(long id) {
		log.trace("timeout(" + id + ")");
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

}
