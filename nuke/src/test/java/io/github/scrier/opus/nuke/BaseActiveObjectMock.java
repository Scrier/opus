package io.github.scrier.opus.nuke;

import io.github.scrier.opus.common.aoc.BaseActiveObject;
import io.github.scrier.opus.common.message.BaseMsgC;

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

	@Override
  public void handleInMessage(BaseMsgC message) {
  }

}
