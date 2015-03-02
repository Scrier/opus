/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Andreas Joelsson (andreas.joelsson@gmail.com)
 */
package io.github.scrier.opus.duke.commander;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseActiveObject;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.duke.DukeInfo;
import io.github.scrier.opus.common.duke.DukeState;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.message.SendIF;
import io.github.scrier.opus.common.nuke.NukeState;

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
	
	private DukeState clientState;
	private DukeInfo clientInfo;
	
	private Context() {
		this.doOnce = true;
		setCommander(null);
		setBaseAoC(null);
		setTxID(0);
		setNukes(null);
		setUniqueGenerator(null);
		this.executeItems = new ArrayList<Long>();
		this.clientState = DukeState.UNDEFINED;
		this.clientInfo = null;
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
		if( null != this.clientInfo ) {
			getCommander().removeEntry(this.clientInfo);
		}
		this.clientInfo = null;
		setCommander(null);
		setBaseAoC(null);
		setTxID(0);
		setNukes(null);
	}
	
	public boolean registerProcedure(BaseDukeProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		return (null == getCommander()) ? false : getCommander().registerProcedure(procedure);
	}
	
	public long getIdentity() throws InvalidOperationException {
		return getBaseAoC().getIdentity();
	}
	
	/**
	 * Method to add a new entry to the map.
	 * @param data BaseNukeC to add to the map.
	 */
	public void addEntry(BaseDataC data) {
		getCommander().addEntry(data);
	}
	
	public boolean updateEntry(BaseDataC data) {
		return getCommander().updateEntry(data);
	}
	
	public boolean removeEntry(BaseDataC component) {
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
   * @param identity Long
   * @param info INukeInfo
   * @return boolean
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
   * @return List list returned
   */
  public List<INukeInfo> getNukes() {
  	return new ArrayList<INukeInfo>(getNukesMap().values());
  }
  
  /**
   * Method to get a Collection with nukes in the state(s) specified.
   * @param states one to many NukeState to look for.
   * @return List that is in the states specified.
   * {@code
   * List<INukeInfo> singleState = getNukes(NukeState.RUNNING);
   * List<INukeInfo> multiState = getNukes(NukeState.INTITIALIZED, NukeState.RUNNING);
   * }
   */
  public List<INukeInfo> getNukes(NukeState... states) {
  	log.trace("getNukes(" + states + ")");
  	List<INukeInfo> retValue = new ArrayList<INukeInfo>();
  	for( INukeInfo info : getNukes() ) {
  		for( NukeState state : states ) {
  			if( info.getInfoState() == state ) {
  				retValue.add(info);
  				break;
  			}
  		}
  	}
  	return retValue;
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
	 * @param timeUnit TuneUnit format to schedule timeout in.
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
  		public synchronized void run() {
  			if( getExecuteItems().contains(timeOutID) ) {
  				getExecuteItems().remove(timeOutID);
  				itemToCallback.timeOutTriggered(timeOutID);
  				getCommander().handlePostEntry();
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
   * Method to get the send interface for messages.
   * @return SendIF  to use for message sends.
   */
  public SendIF getSendIF() {
  	return baseAoC.getSendIF();
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

	/**
	 * @return the clientState
	 */
  public DukeState getClientState() {
	  return clientState;
  }

	/**
	 * @param clientState the clientState to set
	 */
  public void setClientState(DukeState clientState) {
  	if( DukeState.UNDEFINED == clientState ) {
  		log.error("Received a request to set client state back to UNDEFINED, cannot continue.");
  		getBaseAoC().shutDown();
  	} else if ( null == this.clientInfo || DukeState.UNDEFINED == this.clientState ) {
  		log.info("We publish the duke state " + clientState + " to the info map.");
  		this.clientInfo = new DukeInfo();
  		try {
	      clientInfo.setDukeID(getIdentity());
	      clientInfo.setKey(getIdentity());
      } catch (InvalidOperationException e) {
	      log.fatal("Received a InvalidOperationException when trying to fetch Identity for the client.", e);
	      getBaseAoC().shutDown();
      }
  		clientInfo.setState(clientState);
  		getCommander().addEntry(clientInfo);
  		this.clientState = clientState;
  	} else if( this.clientState != clientState ) {
  		log.info("Changing duke state from " + this.clientState + " to " + clientState + ".");
  		clientInfo.setState(clientState);
  		getCommander().updateEntry(this.clientInfo);
  		this.clientState = clientState;
  	}
  }

}
