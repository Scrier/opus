package io.github.scrier.opus.common.commander;

import io.github.scrier.opus.common.aoc.BaseNukeC;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseProcedureC {
	
	private static Logger log = LogManager.getLogger(BaseProcedureC.class);
	
	private int state;
	
	private int txID;
	
	public final int ABORTED = 0;
	public final int CREATED = 1;
	public final int COMPLETED = 9999;
	
	public BaseProcedureC() {
		log.trace("BaseProcedureC()");
		this.state = CREATED;
		this.txID = -1;
	}
	
	public abstract void init() throws Exception;
	
	public abstract void shutDown() throws Exception;

	public abstract int handleOnUpdated(BaseNukeC value);
	
	public abstract int handleOnEvicted(BaseNukeC value);
	
	public abstract int handleOnRemoved(Long key);
	
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
		if( ABORTED != getState() && COMPLETED != getState() && this.state != state) {
			this.state = state;
			onStateChanged(this.state);
		}
	}
	
	/**
	 * Method called when a state is changed.
	 * @param state int with current state.
	 */
	public void onStateChanged(int state) {
		log.trace("onStateChanged(" + state + ")");
	}
	
	/**
	 * Method to check if the procedure has reached a finished state (COMPLETED or ABORTED).
	 * @return boolean
	 */
	public boolean isProcedureFinished() {
		return ( ABORTED == getState() ) || ( COMPLETED == getState() );
	}

	/**
	 * @return the txID
	 */
  public int getTxID() {
	  return txID;
  }
  
	/**
	 * Method to set txid
	 * @param txID int with txID to set.
	 */
  protected void setTxID(int txID) {
	  this.txID = txID;
  }

}
