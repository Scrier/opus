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
package io.github.scrier.opus.nuke.task;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseActiveObject;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.message.SendIF;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;

public enum Context {
	INSTANCE;
	
	private static Logger log = LogManager.getLogger(Context.class);
	
	private HazelcastInstance instance;
	private boolean initialized;
	private BaseActiveObject parent;
	private NukeTasks task;
	private ThreadPoolExecutor executor;
	
	private int txID;
	
	private Context() {
		initialized = false;
		txID = 0;
		executor = null;
		task = null;
		parent = null;
		instance = null;
	}
	
	public boolean init(NukeTasks task, BaseActiveObject parent) {
		boolean retValue = true;
		if( true == initialized ) {
			log.error("Already, initialized, you have an invalid call to init.");
			retValue = false;
		} else {
			initialized = true;
			setParent(parent);
			setTask(task);
			setInstance(parent.getInstance());
			executor = new ThreadPoolExecutor(10000, 10000, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		}
		return retValue;
	}

	public void shutDown() {
		if( null != executor ) {
			executor.shutdownNow();
			log.info("Shutting down threads, waiting for terminateion.");
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.fatal("Received InterruptedException in shutDown.", e);
			}
		}
		initialized = false;
		txID = 0;
		executor = null;
		task = null;
		parent = null;
		instance = null;
	}
	
	public long getIdentity() throws InvalidOperationException {
		return getParent().getIdentity();
	}
	
	/**
	 * @return the instance
	 */
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
	 * Method to get a cluster unique id key for use.
	 * @return long
	 */
	public long getUniqueID() {
		return getInstance().getIdGenerator(Shared.Hazelcast.COMMON_UNIQUE_ID).newId();
	}

	/**
	 * @return the parent
	 */
	private BaseActiveObject getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	private void setParent(BaseActiveObject parent) {
		this.parent = parent;
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
	 * @return the task
	 */
  public NukeTasks getTask() {
	  return task;
  }
  
  /**
   * Method to get a specified setting connected to a key.
   * @param key String with the key to look for.
   * @return String
   * @throws InvalidOperationException if not initialized correctly.
   */
  public String getSetting(String key) throws InvalidOperationException {
  	return getParent().getSettings().get(key);
  }
  
  /**
   * Method to check if a specified setting exists.
   * @param key String with the key to look for.
   * @return boolean
   * @throws InvalidOperationException if not initialized correctly.
   */
  public boolean containsSetting(String key) throws InvalidOperationException {
  	return getParent().getSettings().containsKey(key);
  }

	/**
	 * @param task the task to set
	 */
  public void setTask(NukeTasks task) {
	  this.task = task;
  }
  
	public void addEntry(BaseDataC data) {
		getTask().addEntry(data);
	}
	
	public boolean updateEntry(BaseDataC data) {
		return getTask().updateEntry(data);
	}
	
	public boolean removeEntry(BaseDataC data) {
		return getTask().removeEntry(data);
	}
	
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}
	
	public SendIF getSendIF() {
		return parent.getSendIF();
	}
	
}
