package io.github.scrier.opus.nuke.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.nuke.NukeInfo;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

public class NukeTasks implements EntryListener<Long, NukeInfo> {
	
	private static Logger log = LogManager.getLogger(NukeTasks.class);
	
	private Context theContext;
	private Long identity;
	
	public NukeTasks() throws InvalidOperationException {
		theContext = Context.INSTANCE;
		setIdentity(theContext.getIdentity());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void entryAdded(EntryEvent<Long, NukeInfo> added) {
		log.trace("entryAdded(" +  added + ")");
		if( getIdentity() == added.getKey() ) { // Message is about us.

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void entryEvicted(EntryEvent<Long, NukeInfo> evicted) {
		log.trace("entryEvicted(" +  evicted + ")");
		if( getIdentity() == evicted.getKey() ) { // Message is about us.
			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void entryRemoved(EntryEvent<Long, NukeInfo> removed) {
		log.trace("entryRemoved(" +  removed + ")");
		if( getIdentity() == removed.getKey() ) { // Message is about us.
			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void entryUpdated(EntryEvent<Long, NukeInfo> updated) {
		log.trace("entryUpdated(" +  updated + ")");
		if( getIdentity() == updated.getKey() ) { // Message is about us.
			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mapCleared(MapEvent cleared) {
		log.trace("mapCleared(" +  cleared + ")");
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mapEvicted(MapEvent evicted) {
		log.trace("mapEvicted(" +  evicted + ")");
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the identity
	 */
	protected Long getIdentity() {
		return identity;
	}

	/**
	 * @param identity the identity to set
	 */
	private void setIdentity(Long identity) {
		this.identity = identity;
	}

}
