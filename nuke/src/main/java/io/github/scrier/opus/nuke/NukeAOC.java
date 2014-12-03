package io.github.scrier.opus.nuke;

import com.hazelcast.core.HazelcastInstance;

import io.github.scrier.opus.common.aoc.BaseActiveObject;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.nuke.task.Context;
import io.github.scrier.opus.nuke.task.NukeTasks;

public class NukeAOC extends BaseActiveObject {

	private NukeTasks nukeTasks;
	
	public NukeAOC(HazelcastInstance instance) throws InvalidOperationException {
		super(instance);
		nukeTasks = new NukeTasks(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		Context.INSTANCE.init(nukeTasks, this);
    nukeTasks.init();  
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() {
		Context.INSTANCE.shutDown();
		nukeTasks.shutDown();
	}

}
