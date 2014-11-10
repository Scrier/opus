package io.github.scrier.opus.duke.commander;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.exception.InvalidOperationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseProcedure {
	
	private static Logger log = LogManager.getLogger(BaseProcedure.class);
	
	private int state;
	private Context theContext;
	
	private int txID;
	
	public final int ABORTED = 0;
	public final int CREATED = 1;
	public final int COMPLETED = 9999;
	
	public BaseProcedure() {
		log.trace("BaseProcedure()");
		this.state = CREATED;
		this.theContext = Context.INSTANCE;
		this.txID = theContext.getNextTxID();
	}
	
	public abstract void init() throws Exception;
	
	public abstract void shutDown() throws Exception;

	public abstract int handleOnUpdated(BaseNukeC data);
	
	public abstract int handleOnEvicted(BaseNukeC data);
	
	public abstract int handleOnRemoved(BaseNukeC data);
	
	public long addEntry(BaseNukeC data) {
		return theContext.addEntry(data);
	}
	
	public void addEntry(BaseNukeC data, Long component) {
		theContext.addEntry(data, component);
	}
	
	public boolean updateEntry(BaseNukeC data, Long component) {
		return theContext.updateEntry(data, component);
	}
	
	public boolean removeEntry(Long component) {
		return theContext.removeEntry(component);
	}
	
	public boolean registerProcedure(BaseProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		return theContext.registerProcedure(procedure);
	}
	
	public long getIdentity() {
		try {
	    return theContext.getIdentity();
    } catch (InvalidOperationException e) {
	    log.error("Threw InvalidOperationException when calling Context.getIdentity.", e);
    }
		return -1;
	}
	
	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		// We cannot change a complete of aborted procedure.
		if( ABORTED != getState() && COMPLETED != getState() ) {
			this.state = state;
		}
	}

	/**
	 * @return the txID
	 */
  protected int getTxID() {
	  return txID;
  }

}
