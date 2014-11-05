package io.github.scrier.opus.nuke.task;

import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.nuke.NukeAOC;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;

public enum Context {
	INSTANCE;
	
	private static Logger log = LogManager.getLogger(Context.class);
	
	private HazelcastInstance instance;
	private boolean initialized;
	private NukeAOC parent;
	
	private Context() {
		initialized = false;
	}
	
	public boolean init(NukeAOC parent) {
		boolean retValue = true;
		if( true == initialized ) {
			log.error("Already, initialized, you have an invalid call to init.");
			retValue = false;
		} else {
			initialized = true;
			setParent(parent);
			setInstance(parent.getInstance());
		}
		return retValue;
	}

	public void shutDown() {
		
	}
	
	public Long getIdentity() throws InvalidOperationException {
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
	 * @return the parent
	 */
	private NukeAOC getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	private void setParent(NukeAOC parent) {
		this.parent = parent;
	}
	
}
