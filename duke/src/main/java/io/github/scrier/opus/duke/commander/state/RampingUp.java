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
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeState;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;
import io.github.scrier.opus.duke.commander.CommandProcedure;
import io.github.scrier.opus.duke.commander.Context;
import io.github.scrier.opus.duke.commander.INukeInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RampingUp extends State {

	private static Logger log = LogManager.getLogger(RampingUp.class);
	
	protected static int DEFAULT_INTERVAL_SECONDS = 5;
	
	private int intervalSeconds;		///< Interval seconds to increase each rampup.
	private int localUserRampedUp;	///< Local information about issues commands.
	
	private Context theContext = Context.INSTANCE;
	
	/**
	 * Constructor
	 */
	public RampingUp(ClusterDistributorProcedure parent) {
	  this(parent, DEFAULT_INTERVAL_SECONDS);
  }
	
	public RampingUp(ClusterDistributorProcedure parent, int intervalSeconds) {
	  super(parent);
	  setIntervalSeconds(intervalSeconds);
	  setLocalUserRampedUp(0);
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
	public void updated(BaseDataC data)  {
		log.trace("updated(" + data + ")");
		assertState();
	}  

	/**
	 * RampingUp handling on evicted methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void evicted(BaseDataC data) {
		log.trace("evicted(" + data + ")");
		assertState();
	}

	/**
	 * RampingUp handling on removed methods.
	 * @param key Long
	 */
	@Override
	public void removed(Long key) {
		log.trace("removed(" + key + ")");
		assertState();
	}

	/**
	 * RampingUp handling on timeout methods.
	 * @param id long
	 */
	@Override
	public void timeout(long id) {
		log.trace("timeout(" + id + ")");
		assertState();
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
						registerProcedure(new CommandProcedure(command.getKey(), getCommand(), getFolder(), isRepeated()));
					}
				}
				log.info("Ramping up from " + getLocalUserRampedUp() + " to " + (getLocalUserRampedUp() + usersToAdd) + ", of a total of " + getMaxUsers() + ".");
				setLocalUserRampedUp(getLocalUserRampedUp() + usersToAdd);
				startTimeout(getIntervalSeconds(), getTimerID());
			}
		} else { ///@TODO could add checks here against the distributed status instead of local, lets wait and see how it works though.
			log.info("Changing state from RAMPING_UP to PEAK_DELAY.");
			setState(PEAK_DELAY);
		}
	}
	
  /** 
   * Method to get a suggestion of the number of items to use for distribution.
   * @param noOfThreads int with the number that we want to use.
   * @return Map with key Long and Integer value, where key is nukeid and value is amount.
   */
	public Map<Long, Integer> getDistributionSuggestion(int noOfThreads) {
		log.trace("getDistributionSuggestion(" + noOfThreads + ")");
		Map<Long, Integer> retValue = new HashMap<Long, Integer>();
		List<INukeInfo> availableNukes = theContext.getNukes(NukeState.RUNNING);
		int toExecute = noOfThreads;
		if( true == availableNukes.isEmpty() ) {
			log.error("No available nodes in state " + NukeState.RUNNING + ", cannot continue, was. " + availableNukes.size() +  ".");
			return null;
		} else {
			while( toExecute > 0 ) {
				INukeInfo minInfo = null;
				log.debug("Checking " + availableNukes.size() + " for who gets the ball.");
				for( INukeInfo info : availableNukes ) {
					if( minInfo == null ) {
						minInfo = info;
					} else {
						int minInfoAmount = getTotalUsers(retValue, minInfo);
						int infoAmount = getTotalUsers(retValue, info);
						log.debug("if( minInfoAmount[" + minInfoAmount + "] > infoAmount[" + infoAmount + "] )");
						if( minInfoAmount > infoAmount ) {
							log.debug("Changing info object from " + minInfo + " to " + info + ".");
							minInfo = info;
						}
					}
				}
				if( null == minInfo ) {
					throw new RuntimeException("Variable minInfo of type INukeInfo is null although no possible codepath leads to that.");
				} else if ( true == retValue.containsKey(minInfo.getNukeID()) ) {
					int value = retValue.get(minInfo.getNukeID());
					log.debug("Changing from " + value + " to " + (value + 1 ) + " commands to nuke id:" + minInfo.getNukeID() + ".");
					retValue.put(minInfo.getNukeID(), value + 1);
				} else {
					log.debug("Addding 1 command to nuke id: " + minInfo.getNukeID() + ".");
					retValue.put(minInfo.getNukeID(), 1);
				}
				minInfo.setRequestedNoOfThreads(minInfo.getRequestedNoOfThreads() + 1);
				toExecute--;
			}
		}
		log.debug("Returning a suggestion of " + retValue.size() + " nukes to handle distribution " + noOfThreads + " commands.");
		return retValue;
	}

	protected int getTotalUsers(Map<Long, Integer> availableNukes, INukeInfo info) {
		log.trace("getTotalUsers(" + availableNukes + ", " + info + ")");
		int retValue = info.getRequestedNoOfThreads();
		if( availableNukes.containsKey(info.getNukeID()) ) {
			log.debug("Adding " + availableNukes.get(info.getNukeID()) + " to " + retValue + ".");
			retValue += availableNukes.get(info.getNukeID());
		}
		return retValue;
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

	/**
	 * @return the localUserRampedUp
	 */
  public int getLocalUserRampedUp() {
	  return localUserRampedUp;
  }

	/**
	 * @param localUserRampedUp the localUserRampedUp to set
	 */
  public void setLocalUserRampedUp(int localUserRampedUp) {
	  this.localUserRampedUp = localUserRampedUp;
  }
  
  /**
   * Method to assure that we are called in the correct state.
   */
  private void assertState() {
  	if( RAMPING_UP != getState() ) {
			log.error("Called state RAMPING_UP(" + RAMPING_UP + "), when in state " + getState() + ".");
			throw new RuntimeException("Called state RAMPING_UP(" + RAMPING_UP + "), when in state " + getState() + ".");
		} 
  }
	
}
