package io.github.scrier.opus.nuke.task;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.scrier.opus.common.aoc.BaseNukeC;
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
	private NukeTasks task;
	private ThreadPoolExecutor executor;
	
	private int txID;
	
	private Context() {
		initialized = false;
		txID = 0;
		executor = null;
	}
	
	public boolean init(NukeTasks task, NukeAOC parent) {
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
	 * @param task the task to set
	 */
  public void setTask(NukeTasks task) {
	  this.task = task;
  }
  
	public void addEntry(BaseNukeC data) {
		getTask().addEntry(data);
	}
	
	public boolean updateEntry(BaseNukeC data) {
		return getTask().updateEntry(data);
	}
	
	public boolean removeEntry(BaseNukeC data) {
		return getTask().removeEntry(data);
	}
	
	protected ThreadPoolExecutor getExecutor() {
		return executor;
	}
	
}
