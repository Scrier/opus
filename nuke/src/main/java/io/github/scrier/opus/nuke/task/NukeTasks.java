package io.github.scrier.opus.nuke.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseListener;
import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.nuke.NukeInfo;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapEvent;

public class NukeTasks extends BaseListener {
	
	private static Logger log = LogManager.getLogger(NukeTasks.class);
	
	private Context theContext;
	private Long identity;
	
	public NukeTasks(HazelcastInstance instance) throws InvalidOperationException {
	  super(instance, Shared.Hazelcast.BASE_NUKE_MAP);
	  theContext = Context.INSTANCE;
	  setIdentity(theContext.getIdentity());
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void preEntry() {
	  // TODO Auto-generated method stub
	  
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void entryAdded(Long component, BaseNukeC data) {
	  // TODO Auto-generated method stub
	  
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void entryEvicted(Long component, BaseNukeC data) {
	  // TODO Auto-generated method stub
	  
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void entryRemoved(Long component, BaseNukeC data) {
	  // TODO Auto-generated method stub
	  
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void entryUpdated(Long component, BaseNukeC data) {
	  // TODO Auto-generated method stub
	  
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void postEntry() {
	  // TODO Auto-generated method stub
	  
  }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public void mapCleared(MapEvent event) {
	  // TODO Auto-generated method stub
	  
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void mapEvicted(MapEvent event) {
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
