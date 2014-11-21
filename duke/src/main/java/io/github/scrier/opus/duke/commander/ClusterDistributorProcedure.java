package io.github.scrier.opus.duke.commander;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseNukeC;

public class ClusterDistributorProcedure extends BaseProcedure implements ITimeOutCallback {

	private static Logger log = LogManager.getLogger(ClusterDistributorProcedure.class);

	public final int RAMPING_UP   = CREATED + 1;
	public final int PEAK_DELAY   = CREATED + 2;
	public final int RAMPING_DOWN = CREATED + 3;

	private int minNodes;
	private int maxUsers;
	private int intervalSeconds;
	private int userIncrease;
	private int peakDelaySeconds;
	private int terminateSeconds;
	private String command;
	private String folder;

	private int localUserRampedUp;
	
	private long timerID;
	private long terminateID;

	private State[] states = { new Aborted(), new Created(), new RampingUp(), 
														 new PeakDelay(), new RampingDown() };

	public ClusterDistributorProcedure() {
		setMinNodes(0);
		setMaxUsers(0);
		setIntervalSeconds(0);
		setUserIncrease(0);
		setPeakDelaySeconds(0);
		setTerminateSeconds(0);
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
		if( 1 != getCommander().getProcedures(ClusterDistributorProcedure.class).size() ) {
			log.error("We have more than one cluster distributor running, aborting.");
			setState(ABORTED);
		} else {
			setMinNodes(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_MINIMUM_NODES)));
			setMaxUsers(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_MAX_USERS)));
			setIntervalSeconds(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_INTERVAL)));
			setUserIncrease(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_USER_INCREASE)));
			setPeakDelaySeconds(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_PEAK_DELAY)));
			setTerminateSeconds(Integer.parseInt(getSetting(Shared.Settings.EXECUTE_TERMINATE)));
			setCommand(getSetting(Shared.Settings.EXECUTE_COMMAND));
			setFolder(getSetting(Shared.Settings.EXECUTE_FOLDER));
			setTimerID(getUniqueID());
			setTerminateID(getUniqueID());
			log.info("Starting timeout for execution to go off in " + Shared.Methods.formatTime(getTerminateSeconds()) + ".");
			startTimeout(getTerminateSeconds(), getTerminateID(), this);
			log.info("Starting rampup phase with " + getUserIncrease() + " every " + getIntervalSeconds() + " seconds.");
			startTimeout(getIntervalSeconds(), getTimerID(), this);
			setState(RAMPING_UP);
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

	@Override
	public void timeOutTriggered(long id) {
		log.trace("timeOutTriggered(" + id + ")");
		try {
			states[getState()].timeout(id);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed().timeout(id);
			} else {
				log.error("Received out of bound exception in state: " + getState() + ".", e);
			}
		}
	}
	
	/**
	 * Base state for the FSM logic.
	 * @author andreas.joelsson
	 */
	abstract class State {

		/**
		 * Base handling on update methods.
		 * @param data BaseNukeC
		 */
		public void updated(BaseNukeC data)  {
			log.trace("updated(" + data + ")");
			log.error("Default update state setting aborted from state: " + getState() + "."); 
			setState(ABORTED); 
		}  

		/**
		 * Base handling on evicted mmethods.
		 * @param data BaseNukeC
		 */
		public void evicted(BaseNukeC data) {
			log.trace("evicted(" + data + ")");
			log.error("Default update state setting aborted from state: " + getState() + ".");
			setState(ABORTED); 
		}

		/**
		 * Base handling on removed mmethods.
		 * @param data BaseNukeC
		 */
		public void removed(BaseNukeC data) {
			log.trace("removed(" + data + ")");
			log.error("Default update state setting aborted from state: " + getState() + ".");
			setState(ABORTED); 
		}

		/**
		 * Base handling on timeout mmethods.
		 * @param id long
		 */
		public void timeout(long id) {
			log.trace("timeout(" + id + ")");
			log.error("Default update state setting aborted from state: " + getState() + ".");
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

	/**
	 * State handling for RampingUp transactions.
	 * @author andreas.joelsson
	 * {@code
	 * RAMPING_UP -> ABORTED
	 * RAMPING_UP -> PEAK_DELAY
	 * }
	 */
	private class RampingUp extends State {
		
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
		 * @param data BaseNukeC
		 */
		@Override
		public void removed(BaseNukeC data) {
			log.trace("removed(" + data + ")");
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
				log.error("Received terminate timeout during state RAMP_UP.");
				setState(ABORTED);
			} else {
				log.error("Received unknown timer id: " + id + " in state RAMP_UP.");
				setState(ABORTED);
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
				
			} else {
				
			}
		}
		
		private int getDistributedNumberOfUsers() {
			log.trace("getDistributedNumberOfUsers()");
			int retValue = 0;
			for( INukeInfo info : theContext.getNukes() ) {
				retValue += info.getNoOfUsers();
			}
			return retValue;
		}
		
		private int getDistributedNumberOfRequestedUsers() {
			log.trace("getDistributedNumberOfRequestedUsers()");
			int retValue = 0;
			for( INukeInfo info : theContext.getNukes() ) {
				retValue += info.getRequestedNoOfUsers();
			}
			return retValue;
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
			log.trace("updated(" + data + ")");
		}  

		/**
		 * PeakDelay handling on evicted methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void evicted(BaseNukeC data) {
			log.trace("evicted(" + data + ")");
		}

		/**
		 * PeakDelay handling on removed methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void removed(BaseNukeC data) {
			log.trace("removed(" + data + ")");
		}

		/**
		 * PeakDelay handling on timeout methods.
		 * @param id long
		 */
		@Override
		public void timeout(long id) {
			log.trace("timeout(" + id + ")");
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
	private class RampingDown extends State {
		
		/**
		 * Constructor
		 */
		public RampingDown() {
			
		}
		
		/**
		 * RampingDown handling on update methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void updated(BaseNukeC data)  {
			log.trace("updated(" + data + ")");
		}  

		/**
		 * RampingDown handling on evicted methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void evicted(BaseNukeC data) {
			log.trace("evicted(" + data + ")");
		}

		/**
		 * RampingDown handling on removed methods.
		 * @param data BaseNukeC
		 */
		@Override
		public void removed(BaseNukeC data) {
			log.trace("removed(" + data + ")");
		}

		/**
		 * RampingDown handling on timeout methods.
		 * @param id long
		 */
		@Override
		public void timeout(long id) {
			log.trace("timeout(" + id + ")");
		}
		
	}

}
