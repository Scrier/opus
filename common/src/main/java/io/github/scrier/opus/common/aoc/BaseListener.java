package io.github.scrier.opus.common.aoc;

import java.util.Collection;

import io.github.scrier.opus.common.Shared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public abstract class BaseListener implements EntryListener<Long, BaseNukeC> {
	
	private static Logger log = LogManager.getLogger(BaseListener.class);
	
	private HazelcastInstance instance;
	private IMap<Long, BaseNukeC> sharedMap;
	
	public BaseListener(HazelcastInstance instance, String distributedMap) {
		setInstance(instance);
		sharedMap = instance.getMap(distributedMap);
		sharedMap.addEntryListener(this, true);
	}
	
	public abstract void preEntry();
	
	public abstract void entryAdded(Long component, BaseNukeC data);
	
	public abstract void entryEvicted(Long component, BaseNukeC data);
	
	public abstract void entryRemoved(Long component, BaseNukeC data);
	
	public abstract void entryUpdated(Long component, BaseNukeC data);
	
	public abstract void postEntry();
	
	/**
	 * Method to add a new entry to the map.
	 * @param data BaseNukeC to add to the map.
	 * @return long with the unique ID that this data has.
	 */
	public long addEntry(BaseNukeC data) {
		long value = getInstance().getIdGenerator(Shared.Hazelcast.COMMON_NODE_ID).newId();
		sharedMap.put(value, data);
		return value;
	}
	
	public void addEntry(BaseNukeC data, Long component) {
		sharedMap.put(component, data);
	}
	
	public boolean updateEntry(BaseNukeC data, Long component) {
		boolean retValue = true;
		if( sharedMap.containsKey(component) ) {
			sharedMap.put(component, data);
		} else {
			retValue = false;
		}
		return retValue;
	}
	
	public boolean removeEntry(Long component) {
		return null != sharedMap.remove(component);
	}
	
	public Collection<BaseNukeC> getEntries() {
		return sharedMap.values();
	}
	
	@Override
	public void entryAdded(EntryEvent<Long, BaseNukeC> added) {
		log.trace("entryAdded(" + added + ")");
		preEntry();
		entryAdded(added.getKey(), added.getValue());
		postEntry();
	}

	@Override
	public void entryEvicted(EntryEvent<Long, BaseNukeC> evicted) {
		log.trace("entryEvicted(" + evicted + ")");
		preEntry();
		entryEvicted(evicted.getKey(), evicted.getValue());
		postEntry();
	}

	@Override
	public void entryRemoved(EntryEvent<Long, BaseNukeC> removed) {
		log.trace("entryRemoved(" + removed + ")");
		preEntry();
		entryRemoved(removed.getKey(), removed.getValue());
		postEntry();
	}

	@Override
	public void entryUpdated(EntryEvent<Long, BaseNukeC> updated) {
		log.trace("entryUpdated(" + updated + ")");
		preEntry();
		entryUpdated(updated.getKey(), updated.getValue());
		postEntry();
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
