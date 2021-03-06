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
package io.github.scrier.opus.common.data;

import java.util.Collection;

import io.github.scrier.opus.common.Shared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public abstract class DataListener implements EntryListener<Long, BaseDataC> {
	
	private static Logger log = LogManager.getLogger(DataListener.class);
	
	private HazelcastInstance instance;
	private IMap<Long, BaseDataC> sharedMap;
	
	public DataListener(HazelcastInstance instance, String distributedMap) {
		setInstance(instance);
		sharedMap = instance.getMap(distributedMap);
		sharedMap.addEntryListener(this, true);
	}
	
	public abstract void init();
	
	public abstract void shutDown();
	
	public abstract void preEntry();
	
	public abstract void entryAdded(Long key, BaseDataC value);
	
	public abstract void entryEvicted(Long key, BaseDataC value);
	
	public abstract void entryRemoved(Long key);
	
	public abstract void entryUpdated(Long key, BaseDataC value);
	
	public abstract void postEntry();
	
	/**
	 * Method to add a new entry to the map.
	 * @param data BaseNukeC to add to the map.
	 */
	public void addEntry(BaseDataC data) {
		log.trace("addEntry(" + data + ")");
		if( 0 > data.getKey() ) {
			data.setKey(getInstance().getIdGenerator(Shared.Hazelcast.COMMON_MAP_UNIQUE_ID).newId());
		}
		sharedMap.put(data.getKey(), data);
	}
	
	public boolean updateEntry(BaseDataC data) {
		log.trace("updateEntry(" + data + ")");
		boolean retValue = true;
		if( sharedMap.containsKey(data.getKey()) ) {
			sharedMap.put(data.getKey(), data);
		} else {
			retValue = false;
		}
		return retValue;
	}
	
	public boolean removeEntry(BaseDataC data) {
		log.trace("removeEntry(" + data + ")");
		return null != sharedMap.remove(data.getKey());
	}
	
	public Collection<BaseDataC> getEntries() {
		log.trace("getEntries()");
		return sharedMap.values();
	}
	
	@Override
	public synchronized void entryAdded(EntryEvent<Long, BaseDataC> added) {
		log.trace("entryAdded(" + added + ")");
		if( added.getKey() != added.getValue().getKey() ) {
			log.fatal("Received a mismatch mapkey and BasenukeC key in entryAdded!!");
			throw new RuntimeException("Received a mismatch mapkey[" + added.getKey() + "] and BasenukeC[" + added.getValue() + "] key in entryAdded!!");
		} else {
			preEntry();
			entryAdded(added.getKey(), added.getValue());
			postEntry();
		}
	}

	@Override
	public synchronized void entryEvicted(EntryEvent<Long, BaseDataC> evicted) {
		log.trace("entryEvicted(" + evicted + ")");
		preEntry();
		entryEvicted(evicted.getKey(), evicted.getValue());
		postEntry();
	}

	@Override
	public synchronized void entryRemoved(EntryEvent<Long, BaseDataC> removed) {
		log.trace("entryRemoved(" + removed + ")");
		preEntry();
		entryRemoved(removed.getKey());
		postEntry();
	}

	@Override
	public synchronized void entryUpdated(EntryEvent<Long, BaseDataC> updated) {
		log.trace("entryUpdated(" + updated + ")");
		if( updated.getKey() != updated.getValue().getKey() ) {
			log.fatal("Received a mismatch mapkey and BasenukeC key in entryUpdated!!");
			throw new RuntimeException("Received a mismatch mapkey[" + updated.getKey() + "] and BasenukeC[" + updated.getValue() + "] key in entryUpdated!!");
		} else {
			preEntry();
			entryUpdated(updated.getKey(), updated.getValue());
			postEntry();
		}
	}

	/**
	 * @return the instance
	 */
  protected HazelcastInstance getInstance() {
	  return instance;
  }

	/**
	 * @param instance the instance to set
	 */
  private void setInstance(HazelcastInstance instance) {
	  this.instance = instance;
  }
	
}
