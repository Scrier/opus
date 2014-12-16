package io.github.scrier.opus.duke.commander.state;

import io.github.scrier.opus.common.aoc.BaseNukeC;
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
		return this.getClass().getName();
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
	 * Propagated method from parent
	 * @return long with the timer id.
	 */
	protected long getTimerID() {
		return parent.getTimerID();
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
	
}
