package io.github.scrier.opus.duke.commander.state;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeState;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;
import io.github.scrier.opus.duke.commander.CommandProcedure;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RampingUp extends State {

	private static Logger log = LogManager.getLogger(RampingUp.class);
	
	private static int DEFAULT_INTERVAL_SECONDS = 5;
	
	private int intervalSeconds;
	
	/**
	 * Constructor
	 */
	public RampingUp(ClusterDistributorProcedure parent) {
	  this(parent, DEFAULT_INTERVAL_SECONDS);
  }
	
	public RampingUp(ClusterDistributorProcedure parent, int intervalSeconds) {
	  super(parent);
	  setIntervalSeconds(intervalSeconds);
  }

	
	/**
	 * RampingUp handling on init methods.
	 */
	@Override
	public void init() {
		log.trace("init()");
		log.info("Starting rampup phase with " + getUserIncrease() + " every " + getIntervalSeconds() + " seconds.");
		startTimeout(getIntervalSeconds(), getTimerID());
	}

	/**
	 * RampingUp handling on update methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void updated(BaseNukeC data)  {
		log.trace("updated(" + data + ")");
	}  

	/**
	 * RampingUp handling on evicted methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void evicted(BaseNukeC data) {
		log.trace("evicted(" + data + ")");
	}

	/**
	 * RampingUp handling on removed methods.
	 * @param key Long
	 */
	@Override
	public void removed(Long key) {
		log.trace("removed(" + key + ")");
	}

	/**
	 * RampingUp handling on timeout methods.
	 * @param id long
	 */
	@Override
	public void timeout(long id) {
		log.trace("timeout(" + id + ")");
		if( id == getTimerID() ) {
			handleTimerTick();
		} else if ( id == getTerminateID() ) {
			log.error("Received terminate timeout during state RAMPING_UP.");
			setState(TERMINATING);
		} else {
			log.fatal("Received unknown timer id: " + id + " in state RAMPING_UP.");
			throw new RuntimeException("Received unknown timer id: " + id + " in state RAMPING_UP.");
		}
	}
	
	/**
	 * Method to handle next timer tick to create new instances of commands to execute.
	 */
	private void handleTimerTick() {
		log.trace("handleTimerTick()");
		if( getLocalUserRampedUp() < getMaxUsers() ) {
			int usersToAdd = ( getMaxUsers() - getLocalUserRampedUp() ) > getUserIncrease() ? 
					getUserIncrease() : getMaxUsers() - getLocalUserRampedUp();
			Map<Long, Integer> distribution = getDistributionSuggestion(usersToAdd);
			if( null == distribution ) {
				log.fatal("No available nodes in state " + NukeState.RUNNING + ", cannot continue.");
				throw new RuntimeException("No available nodes in state " + NukeState.RUNNING + ", cannot continue.");
			} else {
				for( Entry<Long, Integer> command : distribution.entrySet() ) {
					log.debug("Sending " + command.getValue() + " commands to nuke with id: " + command.getKey() + ".");
					for( int i = 0; i < command.getValue(); i++ ) {
						registerProcedure(new CommandProcedure(command.getKey(), getCommand(), getFolder(), CommandState.EXECUTE, isRepeated()));
					}
				}
				log.info("Ramping up from " + getLocalUserRampedUp() + " to " + (getLocalUserRampedUp() + usersToAdd) + ", of a total of " + getMaxUsers() + ".");
				setLocalUserRampedUp(getLocalUserRampedUp() + usersToAdd);
				startTimeout(getIntervalSeconds(), getTimerID(), ClusterDistributorProcedure.this);
			}
		} else { ///@TODO could add checks here against the distributed status instead of local, lets wait and see how it works though.
			log.info("Changing state from RAMPING_UP to PEAK_DELAY.");
			setState(PEAK_DELAY);
		}
	}


	/**
	 * @return the intervalSeconds
	 */
  public int getIntervalSeconds() {
	  return intervalSeconds;
  }


	/**
	 * @param intervalSeconds the intervalSeconds to set
	 */
  public void setIntervalSeconds(int intervalSeconds) {
	  this.intervalSeconds = intervalSeconds;
  }
	
}
