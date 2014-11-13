package io.github.scrier.opus.duke.commander;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private Map<Long, INukeInfo> Nukes;
	
	private boolean doOnce;
	private int txID;
	
	private Context() {
		this.doOnce = true;
		setCommander(null);
		setBaseAoC(null);
		setTxID(0);
		setNukes(new HashMap<Long, INukeInfo>());
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
		this.doOnce = true;
		setCommander(null);
		setBaseAoC(null);
		setTxID(0);
		setNukes(null);
	}
	
	public boolean registerProcedure(BaseProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		return (null == getCommander()) ? false : getCommander().registerProcedure(procedure);
	}
	
	public long getIdentity() throws InvalidOperationException {
		return getBaseAoC().getIdentity();
	}
	
	/**
	 * Method to add a new entry to the map.
	 * @param data BaseNukeC to add to the map.
	 * @return long with the unique ID that this data has.
	 */
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
  	log.trace("getNextTxID()");
  	if( txID + 1 == Integer.MAX_VALUE ) {
  		txID = 0;
  	} else {
  		txID++;
  	}
	  return txID;
  }

	/**
	 * @param txID the txID to set
	 */
  private void setTxID(int txID) {
	  this.txID = txID;
  }
  
  /**
   * Method to add nuke to the map of existing monitored items.
   * @param identity
   * @param info
   * @return
   */
  public boolean addNuke(Long identity, INukeInfo info) {
		if (true == getNukesMap().containsKey(identity)) {
			return false;
		} else {
			return (null == getNukesMap().put(identity, info));
		}
	}
  
  /**
   * Return a list with all nuke nodes and their available info.
   * @return List<INukeInfo>
   */
  public Collection<INukeInfo> getNukes() {
  	return getNukesMap().values();
  }
  
  /**
   * Returns the nuke object related to an identity.
   * @param identity long with the unique id key.
   * @return INukeInfo or null.
   */
  public INukeInfo getNuke(Long identity) {
  	return getNukesMap().get(identity);
  }

	/**
	 * @return the nukes
	 */
  private Map<Long, INukeInfo> getNukesMap() {
	  return Nukes;
  }

	/**
	 * @param nukes the nukes to set
	 */
  private void setNukes(Map<Long, INukeInfo> nukes) {
	  Nukes = nukes;
  }

}
