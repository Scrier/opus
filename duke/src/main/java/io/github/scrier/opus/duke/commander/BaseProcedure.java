package io.github.scrier.opus.duke.commander;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.exception.InvalidOperationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseProcedure {
	
	private static Logger log = LogManager.getLogger(BaseProcedure.class);
	
	private int state;
	protected Context theContext;
	
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
	
	public long getUniqueID() {
		return theContext.getUniqueID();
	}
	
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
			switch (getState()) {
				case ABORTED: { onAborted(); break; }
				case COMPLETED: { onCompleted(); break; }
				default: { break; }
			}
		}
	}
	
	/**
	 * Method is called when the state is changed to ABORTED. Method is called once and only once.
	 */
	public void onAborted() {
		log.trace("onAborted()");
	}

	/**
	 * Method is called when state is changed to COMPLETED. Method is called once and only once.
	 */
	public void onCompleted() {
		log.trace("onCompleted()");
	}
	
  /**
   * Method to get a specified setting connected to a key.
   * @param key String with the key to look for.
   * @return String
   * @throws InvalidOperationException if not initialized correctly.
   */
	public String getSetting(String key) throws InvalidOperationException {
		return theContext.getSetting(key);
	}
	
	/**
	 * Method to start a timeout in the service.
	 * @param time int with the time in seconds.
	 * @param id long with unique id to get returned.
	 * @param callback ITimeOutCallback interface to call.
	 */
  public void startTimeout(int time, long id, ITimeOutCallback callback) {
  	log.trace("startTimeout(" + time + ", " + id + ", " + callback + ")");
  	theContext.startTimeout(time, id, callback);
  }
  
	/**
	 * Method to start a timeout in the service.
	 * @param time int with the time in the specified format.
	 * @param id long with unique id to get returned.
	 * @param callback ITimeOutCallback interface to call.
	 * @param timeunit format to schedule timeout in.
	 */
  public void startTimeout(int time, long id, ITimeOutCallback callback, TimeUnit timeUnit) {
  	log.trace("startTimeout(" + time + ", " + id + ", " + callback + ", " + timeUnit + ")");
  	theContext.startTimeout(time, id, callback, timeUnit);
  }
	
	/**
	 * @return the commander
	 */
	public DukeCommander getCommander() {
		return theContext.getCommander();
	}
	
	/**
	 * @return the txID
	 */
  protected int getTxID() {
	  return txID;
  }

}
