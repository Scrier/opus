package io.github.scrier.opus.duke.commander;

import java.util.Collection;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseActiveObject;
import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.exception.InvalidOperationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum Context {
	INSTANCE;
	
	private static Logger log = LogManager.getLogger(Context.class);

	
	private DukeCommander commander;
	private BaseActiveObject baseAoC;
	
	private boolean doOnce;
	private int txID;
	
	private Context() {
		this.doOnce = true;
		setCommander(null);
		setBaseAoC(null);
		setTxID(0);
	}
	
	public void init(DukeCommander commander, BaseActiveObject baseAoC) {
		log.trace("init(" + commander + ")");
		if( doOnce ) {
			setCommander(commander);
			setBaseAoC(baseAoC);
		} else {
			log.error("init alread called.");
		}
	}
	
	public void shutDown() {
		
	}
	
	public boolean registerProcedure(BaseProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		return (null == getCommander()) ? false : getCommander().registerProcedure(procedure);
	}
	
	public long getIdentity() throws InvalidOperationException {
		return getBaseAoC().getIdentity();
	}
	
	public long addEntry(BaseNukeC data) {
		return getCommander().addEntry(data);
	}
	
	public void addEntry(BaseNukeC data, Long component) {
		getCommander().addEntry(data, component);
	}
	
	public boolean updateEntry(BaseNukeC data, Long component) {
		return getCommander().updateEntry(data, component);
	}
	
	public boolean removeEntry(Long component) {
		return getCommander().removeEntry(component);
	}
	
	/**
	 * @return the commander
	 */
	public DukeCommander getCommander() {
		return commander;
	}

	/**
	 * @param commander the commander to set
	 */
	private void setCommander(DukeCommander commander) {
		this.commander = commander;
	}

	/**
	 * @return the baseAoC
	 */
  public BaseActiveObject getBaseAoC() {
	  return baseAoC;
  }

	/**
	 * @param baseAoC the baseAoC to set
	 */
  private void setBaseAoC(BaseActiveObject baseAoC) {
	  this.baseAoC = baseAoC;
  }

	/**
	 * @return the txID
	 */
  public int getNextTxID() {
  	if( txID + 1 == Integer.MAX_VALUE ) {
  		txID = 0;
  	} else {
  		txID ++;
  	}
	  return txID;
  }

	/**
	 * @param txID the txID to set
	 */
  private void setTxID(int txID) {
	  this.txID = txID;
  }

}
