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
	private int waitingForNukeUpdateSeconds;	///< Interval to check for available nodes.
	private int rampDownUpdateSeconds;	///< Update interval for calculating rampdown
	private boolean repeated;		///< Issues if commands should be repeated or not.
	private boolean shutDownOnce;	///< guard for only doing one shutdown.
	private String command;			///< Command to issue to the nodes.
	private String folder;			///< What folder each node should execute the command from

	private int localUserRampedUp;	///< Local information about issues commands.
	
	private long timerID;			///< id for the timer tick callback
	private long terminateID;		///< id for the terminate tick callback.

	private State[] states = { new Aborted(), new Created(), new WaitingForNuke(),
														 new RampingUp(), new PeakDelay(), new RampingDown(), new Terminating() };
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
		setWaitingForNukeUpdateSeconds(5);
		setRampDownUpdateSeconds(5);
		setRepeated(false);
		setShutDownOnce(true);
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
	public int handleOnUpdated(BaseNukeC data) {
		log.trace("handleOnUpdated(" + data + ")");
		try {
			log.debug("states[" + states[getState()].getClass().getSimpleName() + "].updated(" + data + ");");
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
			log.debug("states[" + states[getState()].getClass().getSimpleName() + "].evicted(" + data + ");");
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
	public int handleOnRemoved(Long key) {
		log.trace("handleOnRemoved(" + key + ")");
		try {
			log.debug("states[" + states[getState()].getClass().getSimpleName() + "].removed(" + key + ");");
			states[getState()].removed(key);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed().removed(key);
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
				new Completed().timeout(id);
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
				new Completed().init();
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
	protected void setMinNodes(int minNodes) {
		this.minNodes = minNodes;
	}

	/**
	 * @return the maxUsers
	 */
	protected int getMaxUsers() {
		return maxUsers;
	}

	/**
	 * @param maxUsers the maxUsers to set
	 */
	protected void setMaxUsers(int maxUsers) {
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
	protected void setIntervalSeconds(int intervalSeconds) {
		this.intervalSeconds = intervalSeconds;
	}

	/**
	 * @return the userIncrease
	 */
	protected int getUserIncrease() {
		return userIncrease;
	}

	/**
	 * @param userIncrease the userIncrease to set
	 */
	protected void setUserIncrease(int userIncrease) {
		this.userIncrease = userIncrease;
	}

	/**
	 * @return the peakDelaySeconds
	 */
	protected int getPeakDelaySeconds() {
		return peakDelaySeconds;
	}

	/**
	 * @param peakDelaySeconds the peakDelaySeconds to set
	 */
	protected void setPeakDelaySeconds(int peakDelaySeconds) {
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
	protected void setTerminateSeconds(int terminateSeconds) {
		this.terminateSeconds = terminateSeconds;
	}

	/**
	 * @return the waitingForNukeUpdateSeconds
	 */
  protected int getWaitingForNukeUpdateSeconds() {
	  return waitingForNukeUpdateSeconds;
  }

	/**
	 * @param waitingForNukeUpdateSeconds the waitingForNukeUpdateSeconds to set
	 */
  protected void setWaitingForNukeUpdateSeconds(int waitingForNukeUpdateSeconds) {
	  this.waitingForNukeUpdateSeconds = waitingForNukeUpdateSeconds;
  }

	/**
	 * @return the rampDownUpdateSeconds
	 */
  protected int getRampDownUpdateSeconds() {
	  return rampDownUpdateSeconds;
  }

	/**
	 * @param rampDownUpdateSeconds the rampDownUpdateSeconds to set
	 */
  protected void setRampDownUpdateSeconds(int rampDownUpdateSeconds) {
	  this.rampDownUpdateSeconds = rampDownUpdateSeconds;
  }

	/**
	 * @return the repeated
	 */
	protected boolean isRepeated() {
		return repeated;
	}

	/**
	 * @param repeated the repeated to set
	 */
	protected void setRepeated(boolean repeated) {
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
	protected void setShutDownOnce(boolean shutDownOnce) {
	  this.shutDownOnce = shutDownOnce;
  }

	/**
	 * @return the command
	 */
	protected String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	protected void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the folder
	 */
	protected String getFolder() {
		return folder;
	}

	/**
	 * @param folder the folder to set
	 */
	protected void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * @return the userRampedUp
	 */
  protected int getLocalUserRampedUp() {
	  return localUserRampedUp;
  }

	/**
	 * @param userRampedUp the userRampedUp to set
	 */
  protected void setLocalUserRampedUp(int userRampedUp) {
	  this.localUserRampedUp = userRampedUp;
  }

	/**
	 * @return the timerID
	 */
	protected long getTimerID() {
		return timerID;
	}

	/**
	 * @param timerID the timerID to set
	 */
	protected void setTimerID(long timerID) {
		this.timerID = timerID;
	}

	/**
	 * @return the terminateID
	 */
	protected long getTerminateID() {
	  return terminateID;
  }

	/**
	 * @param terminateID the terminateID to set
	 */
	protected void setTerminateID(long terminateID) {
	  this.terminateID = terminateID;
  }
  
  /**
   * Method to check that the correct number of nukes are in correct state.
   * @return boolean
   */
  protected boolean isNukesReady() {
  	log.trace("isNukesReady()");
  	log.debug((getMinNodes() <= theContext.getNukes(NukeState.RUNNING).size()) + " = " + getMinNodes() + " <= " + theContext.getNukes(NukeState.RUNNING).size() + ".");
  	return (getMinNodes() <= theContext.getNukes(NukeState.RUNNING).size());
  }
	
  /** 
   * Method to get a suggestion of the number of items to use for distribution.
   * @param noOfUsers int with the number that we want to use.
   * @return Map with key Long and Integer value, where key is nukeid and value is amount.
   */
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
	
	protected int getTotalUsers(Map<Long, Integer> availableNukes, INukeInfo info) {
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
	protected int getDistributedNumberOfUsers() {
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
	 * Base state for the FSM logic.
	 * @author andreas.joelsson
	 */
	abstract class State {
		
		private final Logger logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.ClusterDistributorProcedure.State");
		
		/**
		 * Method called each state change.
		 */
		public void init() {
			logLocal.trace("init()");
			logLocal.error("Default init state setting aborted from state: " + getState() + "."); 
			setState(ABORTED); 
		}
		
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
		 * @param key Long
		 */
		public void removed(Long key) {
			logLocal.trace("removed(" + key + ")");
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
		
		/**
		 * Method to get the name of the state for debugging.
		 * @return String with the correct state.
		 */
		public String getClassName() {
			return this.getClass().getName();
		}
		
	}
	
	/**
	 * State handling for Aborted transactions.
	 * @author andreas.joelsson
	 * {@code * -> ABORTED}
	 */
	private class Aborted extends State {}

	/**
	 * State handling for Created transactions.
	 * @author andreas.joelsson
	 * {@code * -> ABORTED}
	 */
	private class Created extends State {}
	
	/**
	 * State handling for Completed transactions
	 * @author andreas.joelsson
	 */
	private class Completed extends State {
		private final Logger logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.ClusterDistributorProcedure.Completed");

		@Override
		public void init() {
			logLocal.trace("init()");
		}
		
		@Override
		public void updated(BaseNukeC data)  {
			logLocal.trace("updated(" + data + ")");
		}  

		@Override
		public void evicted(BaseNukeC data) {
			logLocal.trace("evicted(" + data + ")");
		}

		@Override
		public void removed(Long key) {
			logLocal.trace("removed(" + key + ")");
		}

		@Override
		public void timeout(long id) {
			logLocal.trace("timeout(" + id + ")");
		}
	}
	
	private class WaitingForNuke extends State {

		private final Logger logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.ClusterDistributorProcedure.WaitingForNuke");
		
		/**
		 * WaitingForNuke handling on init methods.
		 */
		@Override
		public void init() {
			logLocal.trace("init()");
			logLocal.info("Starting timeout for waiting for nukes every " + getWaitingForNukeUpdateSeconds() + " second.");
			startTimeout(getWaitingForNukeUpdateSeconds(), getTimerID(), ClusterDistributorProcedure.this);
		}
		
		/**
		 * WaitingForNuke handling on update methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void updated(BaseNukeC data)  {
			logLocal.trace("updated(" + data + ")");
		}  

		/**
		 * WaitingForNuke handling on evicted methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void evicted(BaseNukeC data) {
			logLocal.trace("evicted(" + data + ")");
		}

		/**
		 * WaitingForNuke handling on removed methods.
		 * @param key Long
		 */
		@Override
		public void removed(Long key) {
			logLocal.trace("removed(" + key + ")");
		}

		/**
		 * WaitingForNuke handling on timeout methods.
		 * @param id long
		 */
		@Override
		public void timeout(long id) {
			logLocal.trace("timeout(" + id + ")");
			if( id == getTimerID() ) {
				handleTimerTick();
			} else if ( id == getTerminateID() ) {
				logLocal.error("Received terminate timeout during state WAITING_FOR_NUKE.");
				setState(ABORTED); // not terminating as nothing needs to be terminated.
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
				logLocal.info("Still waiting for nukes, starting new wait timer for " + getWaitingForNukeUpdateSeconds() + " seconds.");
				startTimeout(getWaitingForNukeUpdateSeconds(), getTimerID(), ClusterDistributorProcedure.this);
			} else {
				logLocal.info("Changing state from WAITING_FOR_NUKE to RAMPING_UP.");
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
	 * RAMPING_UP -> TERMINATING
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
		 * RampingUp handling on init methods.
		 */
		@Override
		public void init() {
			logLocal.trace("init()");
			logLocal.info("Starting rampup phase with " + getUserIncrease() + " every " + getIntervalSeconds() + " seconds.");
			startTimeout(getIntervalSeconds(), getTimerID(), ClusterDistributorProcedure.this);
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
		 * @param key Long
		 */
		@Override
		public void removed(Long key) {
			logLocal.trace("removed(" + key + ")");
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
				setState(TERMINATING);
			} else {
				logLocal.fatal("Received unknown timer id: " + id + " in state RAMPING_UP.");
				throw new RuntimeException("Received unknown timer id: " + id + " in state RAMPING_UP.");
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
					logLocal.fatal("No available nodes in state " + NukeState.RUNNING + ", cannot continue.");
					throw new RuntimeException("No available nodes in state " + NukeState.RUNNING + ", cannot continue.");
				} else {
					for( Entry<Long, Integer> command : distribution.entrySet() ) {
						logLocal.debug("Sending " + command.getValue() + " commands to nuke with id: " + command.getKey() + ".");
						for( int i = 0; i < command.getValue(); i++ ) {
							registerProcedure(new CommandProcedure(command.getKey(), getCommand(), getFolder(), CommandState.EXECUTE, isRepeated()));
						}
					}
					logLocal.info("Ramping up from " + getLocalUserRampedUp() + " to " + (getLocalUserRampedUp() + usersToAdd) + ", of a total of " + getMaxUsers() + ".");
					setLocalUserRampedUp(getLocalUserRampedUp() + usersToAdd);
					startTimeout(getIntervalSeconds(), getTimerID(), ClusterDistributorProcedure.this);
				}
			} else { ///@TODO could add checks here against the distributed status instead of local, lets wait and see how it works though.
				logLocal.info("Changing state from RAMPING_UP to PEAK_DELAY.");
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
		 * PeakDelay handling on init methods.
		 */
		@Override
		public void init() {
			logLocal.trace("init()");
			logLocal.info("We have reached peak and we stay idle for " + Shared.Methods.formatTime(getPeakDelaySeconds()) + " before ramping down.");
			startTimeout(getPeakDelaySeconds(), getTimerID(), ClusterDistributorProcedure.this);
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
		 * @param key Long:
		 */
		@Override
		public void removed(Long key) {
			logLocal.trace("removed(" + key + ")");
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
				setState(TERMINATING);
			} else {
				logLocal.fatal("Received unknown timer id: " + id + " in state PEAK_DELAY.");
				throw new RuntimeException("Received unknown timer id: " + id + " in state PEAK_DELAY.");
			}
		}
		
		/**
		 * Method to handle next timer tick to create new instances of commands to execute.
		 */
		private void handleTimerTick() {
			logLocal.trace("handleTimerTick()");
			setState(RAMPING_DOWN);
		}
		
	}

	/**
	 * State handling for Ramping Down transactions.
	 * @author andreas.joelsson
	 * {@code
	 * RAMPING_DOWN -> TERMINATE
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
		 * RampingDown handling on init methods.
		 */
		@Override
		public void init() {
			logLocal.trace("init()");
			handleTimerTick();
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
		 * @param key Long
		 */
		@Override
		public void removed(Long key) {
			logLocal.trace("removed(" + key + ")");
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
				setState(TERMINATING);
			} else {
				logLocal.error("Received unknown timer id: " + id + " in state RAMPING_DOWN.");
				throw new RuntimeException("Received unknown timer id: " + id + " in state RAMPING_DOWN.");
			}
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
    public void finished(long nukeID, int state, String query, String result) {
			logLocal.trace("finished(" + nukeID + ", " + state + ", " + query + ", " + result + ")");
			if( COMPLETED == state ) {
				logLocal.info("Stop Execution command received ok from node " + nukeID + " still " + (getActiveNukeCommands().size() - 1) + " remaining.");
				if( getActiveNukeCommands().contains(nukeID) ) {
					getActiveNukeCommands().remove(nukeID);
					if( true != isTimeoutActive(getTimerID()) ) {
						logLocal.info("Starting the first timeout for RAMPING_DOWN class as we received the first command acceptance.");
						startTimeout(getRampDownUpdateSeconds(), getTimerID(), ClusterDistributorProcedure.this);
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
	
	/**
	 * State handling for Ramping Down transactions.
	 * @author andreas.joelsson
	 * {@code
	 * TERMINATE -> ABORTED
	 * TERMINATE -> COMPLETED
	 * }
	 */
	private class Terminating extends State implements ICommandCallback {
		
		private final Logger logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.ClusterDistributorProcedure.Terminating");
		
		private int terminateTimeout;
		private List<Long> activeNukeCommands;
		
		/**
		 * Constructor
		 */
		public Terminating() {
			setTerminateTimeout(30);
			setActiveNukeCommands(new ArrayList<Long>());
		}
		
		/**
		 * Terminating handling on init methods.
		 */
		@Override
		public void init() {
			logLocal.trace("init()");
			List<INukeInfo> nukes = theContext.getNukes();
			logLocal.info("Sending terminate command to " + nukes.size() + " nukes will terminate this applicatio in " + getTerminateTimeout() + " seconds.");
			for( INukeInfo info : nukes ) {
				registerProcedure(new CommandProcedure(info.getNukeID(), Shared.Commands.Execute.TERMINATE_EXECUTION, CommandState.EXECUTE, this));
				getActiveNukeCommands().add(info.getNukeID());
			}
			startTimeout(getTerminateTimeout(), getTimerID(), ClusterDistributorProcedure.this);
		}
		
		/**
		 * Terminating handling on update methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void updated(BaseNukeC data)  {
			logLocal.trace("updated(" + data + ")");
		}  

		/**
		 * Terminating handling on evicted methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void evicted(BaseNukeC data) {
			logLocal.trace("evicted(" + data + ")");
		}

		/**
		 * Terminating handling on removed methods.
		 * @param key Long
		 */
		@Override
		public void removed(Long key) {
			logLocal.trace("removed(" + key + ")");
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
				logLocal.fatal("Received terminate timeout during state TERMINATING.");
				throw new RuntimeException("Received terminate timeout during state TERMINATING.");
			} else {
				logLocal.fatal("Received unknown timer id: " + id + " in state RAMPING_DOWN.");
				throw new RuntimeException("Received terminate timeout during state TERMINATING.");
			}
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
    public void finished(long nukeID, int state, String query, String result) {
			logLocal.trace("finished(" + nukeID + ", " + state + ", " + query + ", " + result + ")");
			if( COMPLETED == state ) {
				logLocal.info("Stop Execution command received ok from node " + nukeID + " still " + (getActiveNukeCommands().size() - 1) + " remaining.");
				if( getActiveNukeCommands().contains(nukeID) ) {
					getActiveNukeCommands().remove(nukeID);
					if( true == getActiveNukeCommands().isEmpty() ) {
						logLocal.info("All nukes has terminated successful, we are done.");
						theContext.terminateTimeout(getTimerID());
						setState(COMPLETED);
					}
				} else {
					logLocal.fatal("Received status from unknown nuke with id: " + nukeID + ".");
					throw new RuntimeException("Received status from unknown nuke with id: " + nukeID + ".");
				}
			} else {
				logLocal.fatal("Received finish from nukeid " + nukeID + " but unhandled state: " + state + ".");
				throw new RuntimeException("Received finish from nukeid " + nukeID + " but unhandled state: " + state + ".");
			}
		}
		
		/**
		 * Handle timer ticks.
		 */
		private void handleTimerTick() {
			logLocal.trace("handleTimerTick()");
			logLocal.fatal("Received timeout of termination before nukes has terminated their processes, terminating.");
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

	}

}
