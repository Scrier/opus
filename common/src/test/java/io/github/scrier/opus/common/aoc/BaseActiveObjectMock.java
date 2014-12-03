package io.github.scrier.opus.common.aoc;

import com.hazelcast.core.HazelcastInstance;

public class BaseActiveObjectMock extends BaseActiveObject {

	public BaseActiveObjectMock(HazelcastInstance instance) {
		super(instance);
	}

	@Override
	public void init() {
	}

	@Override
	public void shutDown() {
	}

}
