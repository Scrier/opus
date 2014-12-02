package io.github.scrier.opus.duke.commander;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeState;

public class ClusterDistributorProcedure extends BaseDukeProcedure implements ITimeOutCallback {

	private static Logger log = LogManager.getLogger(ClusterDistributorProcedure.class);

	public final int WAITING_FOR_NUKE = CREATED + 1;
	public final int RAMPING_UP       = CREATED + 2;
	public final int PEAK_DELAY       = CREATED + 3;
	public final int RAMPING_DOWN     = CREATED + 4;

	private int minNodes;
	private int maxUsers;
	private int intervalSeconds;
	private int userIncrease;
	private int peakDelaySeconds;
	private int terminateSeconds;
	private int waitingForNukeUpdateSeconds;
	private int rampDownUpdateSeconds;
	private boolean repeated;
	private String command;
	private String folder;

	private int localUserRampedUp;
	
	private long timerID;
	private long terminateID;

	private State[] states = { new Aborted(), new Created(), new WaitingForNuke(),
														 new RampingUp(), new PeakDelay(), new RampingDown() };

	public ClusterDistributorProcedure() {
		setMinNodes(0);
		setMaxUsers(0);
		setIntervalSeconds(0);
		setUserIncrease(0);
		setPeakDelaySeconds(0);
		setTerminateSeconds(0);
		setWaitingForNukeUpdateSeconds(5);
		setRampDownUpdateSeconds(5);
		setRepeated(false);
		setCommand("");
		setFolder("");
		setTimerID(-1L);
		setTerminateID(-1L);
		setLocalUserRampedUp(0);
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
			int exTime = getExecutionTime();
			if ( exTime > getTerminateSeconds() ) {
				log.error("Calculated execcutiontime: " + Shared.Methods.formatTime(exTime) + " time overlaps the terminate time: " + Shared.Methods.formatTime(getTerminateSeconds()) + ".");
				setState(ABORTED);
			} else {
				log.info("Calculated execution time (excluding rampdown) is " + Shared.Methods.formatTime(exTime) + ".");
				log.info("Starting timeout for execution to go off in " + Shared.Methods.formatTime(getTerminateSeconds()) + ".");
				startTimeout(getTerminateSeconds(), getTerminateID(), this);
				if( true == isNukesReady() ) {
					log.info("Starting rampup phase with " + getUserIncrease() + " every " + getIntervalSeconds() + " seconds.");
					startTimeout(getIntervalSeconds(), getTimerID(), ClusterDistributorProcedure.this);
					setState(RAMPING_UP);
				} else {
					log.info("Starting timeout for waiting for nukes.");
					startTimeout(getWaitingForNukeUpdateSeconds(), getTimerID(), this);
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnUpdated(BaseNukeC data) {
		log.trace("handleOnUpdated(" + data + ")");
		try {
			states[getState()].updated(data);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed().updated(data);
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
	public int handleOnEvicted(BaseNukeC data) {
		log.trace("handleOnEvicted(" + data + ")");
		try {
		states[getState()].evicted(data);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed().evicted(data);
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
	public int handleOnRemoved(BaseNukeC data) {
		log.trace("handleOnRemoved(" + data + ")");
		try {
		states[getState()].removed(data);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed().removed(data);
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
			log.debug("states[" + getState() + "].timeout(" + id + ");");
			states[getState()].timeout(id);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed().timeout(id);
			} else {
				log.error("Received out of bound exception in state: " + getState() + ".", e);
			}
		}
		theContext.getCommander().intializeProcedures();
	}

	/**
	 * @return the minNodes
	 */
	private int getMinNodes() {
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
	private int getMaxUsers() {
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
	private int getIntervalSeconds() {
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
	private int getUserIncrease() {
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
	private int getPeakDelaySeconds() {
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
	private int getTerminateSeconds() {
		return terminateSeconds;
	}

	/**
	 * @param terminateSeconds the terminateSeconds to set
	 */
	private void setTerminateSeconds(int terminateSeconds) {
		this.terminateSeconds = terminateSeconds;
	}

	/**
	 * @return the waitingForNukeUpdateSeconds
	 */
  public int getWaitingForNukeUpdateSeconds() {
	  return waitingForNukeUpdateSeconds;
  }

	/**
	 * @param waitingForNukeUpdateSeconds the waitingForNukeUpdateSeconds to set
	 */
  public void setWaitingForNukeUpdateSeconds(int waitingForNukeUpdateSeconds) {
	  this.waitingForNukeUpdateSeconds = waitingForNukeUpdateSeconds;
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
	 * @return the repeated
	 */
	private boolean isRepeated() {
		return repeated;
	}

	/**
	 * @param repeated the repeated to set
	 */
	private void setRepeated(boolean repeated) {
		this.repeated = repeated;
	}

	/**
	 * @return the command
	 */
	private String getCommand() {
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
	private String getFolder() {
		return folder;
	}

	/**
	 * @param folder the folder to set
	 */
	private void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * @return the userRampedUp
	 */
  public int getLocalUserRampedUp() {
	  return localUserRampedUp;
  }

	/**
	 * @param userRampedUp the userRampedUp to set
	 */
  public void setLocalUserRampedUp(int userRampedUp) {
	  this.localUserRampedUp = userRampedUp;
  }

	/**
	 * @return the timerID
	 */
	private long getTimerID() {
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
  public void setTerminateID(long terminateID) {
	  this.terminateID = terminateID;
  }
  
  /**
   * Method to check that the correct number of nukes are in correct state.
   * @return
   */
  private boolean isNukesReady() {
  	log.trace("isNukesReady()");
  	log.debug((getMinNodes() <= theContext.getNukes(NukeState.RUNNING).size()) + " = " + getMinNodes() + " <= " + theContext.getNukes(NukeState.RUNNING).size() + ".");
  	return (getMinNodes() <= theContext.getNukes(NukeState.RUNNING).size());
  }
	
	public Map<Long, Integer> getDistributionSuggestion(int noOfUsers) {
		log.trace("getDistributionSuggestion(" + noOfUsers + ")");
		Map<Long, Integer> retValue = new HashMap<Long, Integer>();
		List<INukeInfo> availableNukes = theContext.getNukes(NukeState.RUNNING);
		int toExecute = noOfUsers;
		if( true == availableNukes.isEmpty() ) {
			log.error("No available nodes in state " + NukeState.RUNNING + ", cannot continue, was. " + availableNukes.size() +  ".");
			return null;
		} else {
			while( toExecute > 0 ) {
				INukeInfo minInfo = null;
				for( INukeInfo info : availableNukes ) {
					if( minInfo == null ) {
						minInfo = info;
					} else {
						int minAmount = getTotalUsers(retValue, minInfo);
						int infoAmount = getTotalUsers(retValue, info);
						if( minAmount > infoAmount ) {
							log.debug("Changing info object from " + minInfo + " to " + info + ".");
							minInfo = info;
						}
					}
				}
				if( null == minInfo ) {
					throw new RuntimeException("Variable minInfo of type INukeInfo is null although no possible codepath leads to that.");
				} else if ( retValue.containsKey(minInfo.getNukeID()) ) {
					int value = retValue.get(minInfo.getNukeID());
					retValue.put(minInfo.getNukeID(), value + 1);
				} else {
					retValue.put(minInfo.getNukeID(), 1);
				}
				toExecute--;
			}
			
		}
		return retValue;
	}
	
	private int getTotalUsers(Map<Long, Integer> availableNukes, INukeInfo info) {
		log.trace("getTotalUsers(" + availableNukes + ", " + info + ")");
		int retValue = info.getRequestedNoOfUsers();
		if( availableNukes.containsKey(info.getNukeID()) ) {
			retValue += availableNukes.get(info.getNukeID());
		}
		return retValue;
	}
	
	/**
	 * Method to get the number of users from the distributed nukes.
	 * @return int
	 */
	private int getDistributedNumberOfUsers() {
		log.trace("getDistributedNumberOfUsers()");
		int retValue = 0;
		for( INukeInfo info : theContext.getNukes() ) {
			retValue += info.getNoOfUsers();
		}
		return retValue;
	}
	
	/**
	 * Method to approximate the execution time of the test.
	 * @return int in seconds.
	 */
	private int getExecutionTime() {
		log.trace("getExecutionTime()");
		int intervals = (int)((getMaxUsers() / getUserIncrease())) + (getMaxUsers() % getUserIncrease() > 0 ? 1 : 0);
		log.debug(intervals + " = " + (int)((getMaxUsers() / getUserIncrease())) + " + " + (getMaxUsers() % getUserIncrease() > 0 ? 1 : 0));
		int retValue = intervals * getIntervalSeconds();
		log.debug(retValue + " = " + intervals + " * " + getIntervalSeconds() + ")");
		retValue += getPeakDelaySeconds();
		return retValue;
	}
	
	/**
	 * Base state for the FSM logic.
	 * @author andreas.joelsson
	 */
	abstract class State {
		
		private final Logger logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.ClusterDistributorProcedure.State");
		
		/**
		 * Base handling on update methods.
		 * @param data BaseNukeC
		 */
		public void updated(BaseNukeC data)  {
			logLocal.trace("updated(" + data + ")");
			logLocal.error("Default update state setting aborted from state: " + getState() + "."); 
			setState(ABORTED); 
		}  

		/**
		 * Base handling on evicted mmethods.
		 * @param data BaseNukeC
		 */
		public void evicted(BaseNukeC data) {
			logLocal.trace("evicted(" + data + ")");
			logLocal.error("Default update state setting aborted from state: " + getState() + ".");
			setState(ABORTED); 
		}

		/**
		 * Base handling on removed mmethods.
		 * @param data BaseNukeC
		 */
		public void removed(BaseNukeC data) {
			logLocal.trace("removed(" + data + ")");
			logLocal.error("Default update state setting aborted from state: " + getState() + ".");
			setState(ABORTED); 
		}

		/**
		 * Base handling on timeout mmethods.
		 * @param id long
		 */
		public void timeout(long id) {
			logLocal.trace("timeout(" + id + ")");
			logLocal.error("Default update state setting aborted from state: " + getState() + ".");
			setState(ABORTED); 
		}
	}
	
	/**
	 * State handling for Aborted transactions.
	 * @author andreas.joelsson
	 * @code * -> ABORTED
	 */
	private class Aborted extends State {}

	/**
	 * State handling for Created transactions.
	 * @author andreas.joelsson
	 * @code * -> ABORTED
	 */
	private class Created extends State {}
	
	/**
	 * State handling for Completed transactions
	 * @author andreas.joelsson
	 * @code * -> ABORTED
	 */
	private class Completed extends State {}
	
	private class WaitingForNuke extends State {

		private final Logger logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.ClusterDistributorProcedure.WaitingForNuke");
		
		/**
		 * RampingUp handling on update methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void updated(BaseNukeC data)  {
			logLocal.trace("updated(" + data + ")");
		}  

		/**
		 * RampingUp handling on evicted methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void evicted(BaseNukeC data) {
			logLocal.trace("evicted(" + data + ")");
		}

		/**
		 * RampingUp handling on removed methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void removed(BaseNukeC data) {
			logLocal.trace("removed(" + data + ")");
		}

		/**
		 * RampingUp handling on timeout methods.
		 * @param id long
		 */
		@Override
		public void timeout(long id) {
			logLocal.trace("timeout(" + id + ")");
			if( id == getTimerID() ) {
				handleTimerTick();
			} else if ( id == getTerminateID() ) {
				logLocal.error("Received terminate timeout during state WAITING_FOR_NUKE.");
				setState(ABORTED);
			} else {
				logLocal.error("Received unknown timer id: " + id + " in state WAITING_FOR_NUKE.");
				setState(ABORTED);
			}
		}
		
		/**
		 * Method to handle next timer tick to create new instances of commands to execute.
		 */
		private void handleTimerTick() {
			logLocal.trace("handleTimerTick()");
			if( true != isNukesReady() ) {
				log.info("Still waiting for nukes, starting new wait timer for " + getWaitingForNukeUpdateSeconds() + " seconds.");
				startTimeout(getWaitingForNukeUpdateSeconds(), getTimerID(), ClusterDistributorProcedure.this);
			} else {
				log.info("Starting rampup phase with " + getUserIncrease() + " every " + getIntervalSeconds() + " seconds.");
				startTimeout(getIntervalSeconds(), getTimerID(), ClusterDistributorProcedure.this);
				log.info("Chaning state from WAITING_FOR_NUKE to RAMPING_UP.");
				setState(RAMPING_UP);
			}
		}
		
	}

	/**
	 * State handling for RampingUp transactions.
	 * @author andreas.joelsson
	 * {@code
	 * RAMPING_UP -> ABORTED
	 * RAMPING_UP -> PEAK_DELAY
	 * }
	 */
	private class RampingUp extends State {
		
		private final Logger logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.ClusterDistributorProcedure.RampingUp");
		
		/**
		 * Constructor
		 */
		public RampingUp() {
			
		}

		/**
		 * RampingUp handling on update methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void updated(BaseNukeC data)  {
			logLocal.trace("updated(" + data + ")");
		}  

		/**
		 * RampingUp handling on evicted methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void evicted(BaseNukeC data) {
			logLocal.trace("evicted(" + data + ")");
		}

		/**
		 * RampingUp handling on removed methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void removed(BaseNukeC data) {
			logLocal.trace("removed(" + data + ")");
		}

		/**
		 * RampingUp handling on timeout methods.
		 * @param id long
		 */
		@Override
		public void timeout(long id) {
			logLocal.trace("timeout(" + id + ")");
			if( id == getTimerID() ) {
				handleTimerTick();
			} else if ( id == getTerminateID() ) {
				logLocal.error("Received terminate timeout during state RAMPING_UP.");
				setState(ABORTED);
			} else {
				logLocal.error("Received unknown timer id: " + id + " in state RAMPING_UP.");
				setState(ABORTED);
			}
		}
		
		/**
		 * Method to handle next timer tick to create new instances of commands to execute.
		 */
		private void handleTimerTick() {
			logLocal.trace("handleTimerTick()");
			if( getLocalUserRampedUp() < getMaxUsers() ) {
				int usersToAdd = ( getMaxUsers() - getLocalUserRampedUp() ) > getUserIncrease() ? 
						getUserIncrease() : getMaxUsers() - getLocalUserRampedUp();
				Map<Long, Integer> distribution = getDistributionSuggestion(usersToAdd);
				if( null == distribution ) {
					log.error("No available nodes in state " + NukeState.RUNNING + ", cannot continue.");
					throw new RuntimeException("No available nodes in state " + NukeState.RUNNING + ", cannot continue.");
				} else {
					for( Entry<Long, Integer> command : distribution.entrySet() ) {
						logLocal.debug("Sending " + command.getValue() + " commands to nuke with id: " + command.getKey() + ".");
						for( int i = 0; i < command.getValue(); i++ ) {
							registerProcedure(new CommandProcedure(command.getKey(), getCommand(), CommandState.EXECUTE, isRepeated()));
						}
					}
					logLocal.info("Ramping up from " + getLocalUserRampedUp() + " to " + (getLocalUserRampedUp() + usersToAdd) + ", of a total of " + getMaxUsers() + ".");
					setLocalUserRampedUp(getLocalUserRampedUp() + usersToAdd);
					startTimeout(getIntervalSeconds(), getTimerID(), ClusterDistributorProcedure.this);
				}
			} else { ///@TODO could add checks here against the distributed status instead of local, lets wait and see how it works though.
				startTimeout(getPeakDelaySeconds(), getTimerID(), ClusterDistributorProcedure.this);
				// Maybe start shorter timer and let the PEAK DELAY state handle when dist users is synced to max before starting peak delay.
				logLocal.info("We have reached peak and we stay idle for " + Shared.Methods.formatTime(getPeakDelaySeconds()) + " before ramping down.");
				log.info("Changing state from RAMPING_UP to PEAK_DELAY.");
				setState(PEAK_DELAY);
			}
		}
		
	}

	/**
	 * State handling for Peak Delay transactions.
	 * @author andreas.joelsson
	 * {@code
	 * PEAK_DELAY -> ABORTED
	 * PEAK_DELAY -> RAMPING_DOWN
	 * }
	 */
	private class PeakDelay extends State {
		
		private final Logger logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.ClusterDistributorProcedure.PeakDelay");
		
		/**
		 * Constructor
		 */
		public PeakDelay() {
			
		}

		/**
		 * PeakDelay handling on update methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void updated(BaseNukeC data)  {
			logLocal.trace("updated(" + data + ")");
		}  

		/**
		 * PeakDelay handling on evicted methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void evicted(BaseNukeC data) {
			logLocal.trace("evicted(" + data + ")");
		}

		/**
		 * PeakDelay handling on removed methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void removed(BaseNukeC data) {
			logLocal.trace("removed(" + data + ")");
		}

		/**
		 * PeakDelay handling on timeout methods.
		 * @param id long
		 */
		@Override
		public void timeout(long id) {
			logLocal.trace("timeout(" + id + ")");
			if( id == getTimerID() ) {
				handleTimerTick();
			} else if ( id == getTerminateID() ) {
				logLocal.error("Received terminate timeout during state PEAK_DELAY.");
				setState(ABORTED);
			} else {
				logLocal.error("Received unknown timer id: " + id + " in state PEAK_DELAY.");
				setState(ABORTED);
			}
		}
		
		/**
		 * Method to handle next timer tick to create new instances of commands to execute.
		 */
		private void handleTimerTick() {
			logLocal.trace("handleTimerTick()");
			startTimeout(1, getTimerID(), ClusterDistributorProcedure.this);
			setState(RAMPING_DOWN);
		}
		
	}

	/**
	 * State handling for Ramping Down transactions.
	 * @author andreas.joelsson
	 * {@code
	 * RAMPING_DOWN -> ABORTED
	 * RAMPIND_DOWN -> COMPLETED
	 * }
	 */
	private class RampingDown extends State implements ICommandCallback {
		
		private final Logger logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.ClusterDistributorProcedure.RampingDown");
		
		private int oldUsers;
		private boolean doOnce;
		private List<Long> activeNukeCommands;
		
		/**
		 * Constructor
		 */
		public RampingDown() {
			setOldUsers(-1);
			setDoOnce(true);
			setActiveNukeCommands(new ArrayList<Long>());
		}
		
		/**
		 * RampingDown handling on update methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void updated(BaseNukeC data)  {
			logLocal.trace("updated(" + data + ")");
		}  

		/**
		 * RampingDown handling on evicted methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void evicted(BaseNukeC data) {
			logLocal.trace("evicted(" + data + ")");
		}

		/**
		 * RampingDown handling on removed methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void removed(BaseNukeC data) {
			logLocal.trace("removed(" + data + ")");
		}

		/**
		 * RampingDown handling on timeout methods.
		 * @param id long
		 */
		@Override
		public void timeout(long id) {
			logLocal.trace("timeout(" + id + ")");
			if( id == getTimerID() ) {
				handleTimerTick();
			} else if ( id == getTerminateID() ) {
				logLocal.error("Received terminate timeout during state RAMPING_DOWN.");
				setState(ABORTED);
			} else {
				logLocal.error("Received unknown timer id: " + id + " in state RAMPING_DOWN.");
				setState(ABORTED);
			}
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
    public void finished(long nukeID, int state, String query, String result) {
			logLocal.trace("finished(" + nukeID + ", " + state + ", " + query + ", " + result + ")");
			if( COMPLETED == getState() ) {
				logLocal.info("Stop Execution command received ok from node " + nukeID + " still " + (getActiveNukeCommands().size() - 1) + " remaining.");
				if( getActiveNukeCommands().contains(nukeID) ) {
					getActiveNukeCommands().remove(nukeID);
					if( true != isTimeoutActive(getTimerID()) ) {
						logLocal.info("Starting the first timeout for RAMPING_DOWN class as we received the first command acceptance.");
						startTimeout(getRampDownUpdateSeconds(), getTimerID(), ClusterDistributorProcedure.this);
					}
				} else {
					throw new RuntimeException("Received status from unknown nuke with id: " + nukeID + ".");
				}
			} else {
				throw new RuntimeException("Received finish from nukeid " + nukeID + " but unhandled state: " + state + ".");
			}
		}
		
		/**
		 * Handle timer ticks.
		 */
		private void handleTimerTick() {
			logLocal.trace("handleTimerTick()");
			if( true == isDoOnce() ) {
				setOldUsers(getMaxUsers());
				List<INukeInfo> nukes = theContext.getNukes();
				logLocal.info("Sending stop command to " + nukes.size() + " nukes.");
				for( INukeInfo info : nukes ) {
					registerProcedure(new CommandProcedure(info.getNukeID(), Shared.Commands.Execute.STOP_EXECUTION, CommandState.EXECUTE, this));
					getActiveNukeCommands().add(info.getNukeID());
				}
				setDoOnce(false);
			} else {
				int activeUsers = getDistributedNumberOfUsers();
				if( activeUsers != getOldUsers() ) {
					logLocal.info("Ramping down from " + getOldUsers() + " to " + activeUsers + ".");
					setOldUsers(activeUsers);
				} else {
					logLocal.info("Ramping down unchanged at " + getOldUsers() + ".");
				}
				if( 0 == activeUsers ) {
					logLocal.info("All users ramped down. we are done.");
					setState(COMPLETED);
				} else {
					startTimeout(getRampDownUpdateSeconds(), getTimerID(), ClusterDistributorProcedure.this);
				}
			}
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
		
	}

}
