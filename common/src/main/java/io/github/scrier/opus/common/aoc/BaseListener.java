package io.github.scrier.opus.common.aoc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;

public abstract class BaseListener implements EntryListener<Long, BaseNukeC> {
	
	private static Logger log = LogManager.getLogger(BaseListener.class);
	
	public abstract void entryAdded(Long component, BaseNukeC data);
	
	public abstract void entryEvicted(Long component, BaseNukeC data);
	
	public abstract void entryRemoved(Long component, BaseNukeC data);
	
	public abstract void entryUpdated(Long component, BaseNukeC data);
	
	@Override
	public void entryAdded(EntryEvent<Long, BaseNukeC> added) {
		log.trace("entryAdded(" + added + ")");
		entryAdded(added.getKey(), added.getValue());
	}

	@Override
	public void entryEvicted(EntryEvent<Long, BaseNukeC> evicted) {
		log.trace("entryEvicted(" + evicted + ")");
		entryEvicted(evicted.getKey(), evicted.getValue());
	}

	@Override
	public void entryRemoved(EntryEvent<Long, BaseNukeC> removed) {
		log.trace("entryRemoved(" + removed + ")");
		entryRemoved(removed.getKey(), removed.getValue());
	}

	@Override
	public void entryUpdated(EntryEvent<Long, BaseNukeC> updated) {
		log.trace("entryUpdated(" + updated + ")");
		entryUpdated(updated.getKey(), updated.getValue());
	}
}
