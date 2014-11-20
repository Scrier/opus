package io.github.scrier.opus.duke.commander;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseActiveObject;
import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.exception.InvalidOperationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.IdGenerator;

public enum Context {
	INSTANCE;
	
	private static Logger log = LogManager.getLogger(Context.class);
	
	private DukeCommander commander;
	private BaseActiveObject baseAoC;
	private final List<Long> executeItems;
	
	private Map<Long, INukeInfo> nukes;
	private IdGenerator uniqueGenerator;
	
	private ScheduledExecutorService timeoutService;
	
	private boolean doOnce;
	private int txID;
	
	private Context() {
		this.doOnce = true;
		setCommander(null);
		setBaseAoC(null);
		setTxID(0);
		setNukes(null);
		setUniqueGenerator(null);
		this.executeItems = new ArrayList<Long>();
	}
	
	public void init(DukeCommander commander, BaseActiveObject baseAoC) {
		log.trace("init(" + commander + ")");
		if( doOnce ) {
			setCommander(commander);
			setBaseAoC(baseAoC);
			setNukes(new HashMap<Long, INukeInfo>());
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
   * Method to get a specified setting connected to a key.
   * @param key String with the key to look for.
   * @return String
   * @throws InvalidOperationException if not initialized correctly.
   */
  public String getSetting(String key) throws InvalidOperationException {
  	return getBaseAoC().getSettings().get(key);
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
   * Method to get a unique ID for timers and other items in the system.
   * @return long with a unique id.
   */
  public long getUniqueID() {
  	if( null == getUniqueGenerator() ) {
  		setUniqueGenerator(getBaseAoC().getInstance().getIdGenerator(Shared.Hazelcast.COMMON_UNIQUE_ID));
  	}
  	return getUniqueGenerator().newId();
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
  	log.trace("addNuke(" + identity + ", " + info + ")");
		if (true == getNukesMap().containsKey(identity)) {
			return false;
		} else {
			return (null == getNukesMap().put(identity, info));
		}
	}
  
  public boolean removeNuke(Long identity, INukeInfo info) {
  	log.trace("removeNuke(" + identity + ", " + info + ")");
  	return getNukesMap().remove(identity, info);
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
	 * Method to start a timeout in the service.
	 * @param time int with the time in seconds.
	 * @param id long with unique id to get returned.
	 * @param callback ITimeOutCallback interface to call.
	 */
  public void startTimeout(int time, long id, ITimeOutCallback callback) {
  	log.trace("startTimeout(" + time + ", " + id + ", " + callback + ")");
  	startTimeout(time, id, callback, TimeUnit.SECONDS);
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
  	if( null == this.timeoutService ) {
  		timeoutService = Executors.newSingleThreadScheduledExecutor();
  	}
  	final long timeOutID = id;
  	final ITimeOutCallback itemToCallback = callback;
  	getExecuteItems().add(timeOutID);
  	timeoutService.schedule(new Runnable() {
  		public void run() {
  			if( getExecuteItems().contains(timeOutID) ) {
  				getExecuteItems().remove(timeOutID);
  				itemToCallback.timeOutTriggered(timeOutID);
  			}
  		}
  	}, time, timeUnit);
  }
  
  /**
   * Method to terminate all active timeouts in the service.
   */
  public void terminateTimeouts() {
  	log.trace("terminateTimeouts()");
  	if( null != this.timeoutService ) {
  		getExecuteItems().clear();
  	}
  }
  
	/**
	 * Method to terminate a specific timeout.
	 * @param id long with the id of the timeout to terminate.
	 * @return boolean
	 */
  public boolean terminateTimeout(long id) {
  	log.trace("terminateTimeout(" + id + ")");
  	return getExecuteItems().remove(id);
  }
  
	/**
	 * Method to check if a specific timeout is active.
	 * @param id long with the id of the timeout to check for.
	 * @return boolean
	 */
  public boolean isTimeoutActive(long id) {
  	log.trace("isTimeoutActive(" + id + ")");
  	return getExecuteItems().contains(id);
  }

	/**
	 * @return the nukes
	 */
  private Map<Long, INukeInfo> getNukesMap() {
	  return nukes;
  }

	/**
	 * @param nukes the nukes to set
	 */
  private void setNukes(Map<Long, INukeInfo> nukes) {
	  this.nukes = nukes;
  }
  
	/**
	 * @return the executeItems
	 */
  private List<Long> getExecuteItems() {
	  return executeItems;
  }

	/**
	 * @return the uniqueGenerator
	 */
	private IdGenerator getUniqueGenerator() {
		return uniqueGenerator;
	}

	/**
	 * @param uniqueGenerator the uniqueGenerator to set
	 */
	private void setUniqueGenerator(IdGenerator uniqueGenerator) {
		this.uniqueGenerator = uniqueGenerator;
	}

}
