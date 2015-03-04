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
package io.github.scrier.opus.common.aoc;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.message.MessageIF;
import io.github.scrier.opus.common.message.MessageService;
import io.github.scrier.opus.common.message.SendIF;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public abstract class BaseActiveObject implements MessageIF {
	
	public static Logger log = LogManager.getLogger(BaseActiveObject.class);

	private boolean correctInitPerformed;
	private long identity;
	private HazelcastInstance instance;
	private IMap<String, String> settings;
	private MessageService msgService;
	
	/**
	 * Constructor
	 * @param instance the HazelcastInstance to use.
	 */
	public BaseActiveObject(HazelcastInstance instance) {
		setInstance(instance);
		setIdentity(-1L);
		setCorrectInitPerformed(false);
		setSettings(null);
		setMsgService(null);
	}

	/**
	 * @return the identity
	 * @throws InvalidOperationException if not is correctly initialized.
	 */
	public long getIdentity() throws InvalidOperationException {
		if( !isCorrectInitPerformed() ) {
			log.error("getIdentity called before preInit method, intialisation is wrong.");
			throw new InvalidOperationException("getIdentity called before preInit method, intialisation is wrong.");
		}
		return identity;
	}

	/**
	 * @param identity the identity to set
	 */
	private void setIdentity(long identity) {
		this.identity = identity;
	}

	/**
	 * @return the instance
	 */
	@Override
	public HazelcastInstance getInstance() {
		return instance;
	}

	/**
	 * @param instance the instance to set
	 */
	private void setInstance(HazelcastInstance instance) {
		this.instance = instance;
	}

	/**
	 * @return the settings
	 * @throws InvalidOperationException if not correctly initialized
	 */
	public IMap<String, String> getSettings() throws InvalidOperationException {
		if( !isCorrectInitPerformed() ) {
			log.error("getSettings called before preInit method, intialisation is wrong.");
			throw new InvalidOperationException("getIdentity called before preInit method, intialisation is wrong.");
		}
		return settings;
	}

	/**
	 * @param settings the settings to set
	 */
	private void setSettings(IMap<String, String> settings) {
		this.settings = settings;
	}

	/**
	 * Method called for the initialization of member classes.
	 */
	public void preInit() {
		log.trace("preInit()");
		setCorrectInitPerformed(true);
		setIdentity(getInstance().getIdGenerator(Shared.Hazelcast.COMMON_MAP_UNIQUE_ID).newId());
		settings = getInstance().getMap(Shared.Hazelcast.SETTINGS_MAP);
		setMsgService(new MessageService(this));
		init();
	}
	
	/**
	 * Returns a unique identifier for a saga id.
	 * @return long
	 */
	public long getNextSagaID() {
		return getInstance().getIdGenerator(Shared.Hazelcast.COMMON_SAGA_ID).newId();
	}
	
	/**
	 * Method to initialize your active object, called before any information is available.
	 */
	public abstract void init();
	
	/**
	 * Method to implement cleanup after the active object is terminated.
	 */
	public abstract void shutDown();
	
	@SuppressWarnings("unused")
	private BaseActiveObject() {} 
	
	/**
	 * @return the correctInitPerformed
	 */
	private boolean isCorrectInitPerformed() {
		return correctInitPerformed;
	}

	/**
	 * @param correctInitPerformed the correctInitPerformed to set
	 */
	private void setCorrectInitPerformed(boolean correctInitPerformed) {
		this.correctInitPerformed = correctInitPerformed;
	}

	/**
	 * @return the msgService
	 */
  public SendIF getSendIF() {
	  return msgService;
  }

	/**
	 * @param msgService the msgService to set
	 */
  public void setMsgService(MessageService msgService) {
	  this.msgService = msgService;
  }
  
  /**
   * Register on a factory id of messages.
   * @param factoryID int
   * @return boolean if successfull.
   */
  public boolean registerOnFactory(int factoryID) {
  	return this.msgService.registerOnFactory(factoryID);
  }
  
  /**
   * Un register on a factory id of messages.
   * @param factoryID int of the factory id to subscibe to
   * @return boolean if successfull.
   */
  public boolean unRegisterOnFactory(int factoryID) {
  	return this.msgService.unRegisterOnFactory(factoryID);
  }
	
}
