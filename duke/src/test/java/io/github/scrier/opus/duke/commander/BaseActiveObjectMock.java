package io.github.scrier.opus.duke.commander;

import io.github.scrier.opus.common.aoc.BaseActiveObject;
import io.github.scrier.opus.common.message.BaseMsgC;

import com.hazelcast.core.HazelcastInstance;

public class BaseActiveObjectMock extends BaseActiveObject {
	
	public BaseMsgC LastMessage;

	public BaseActiveObjectMock(HazelcastInstance instance) {
		super(instance);
		LastMessage = null;
	}

	@Override
	public void init() {
	}

	@Override
	public void shutDown() {
	}

	@Override
  public void handleInMessage(BaseMsgC message) {
	  LastMessage = message;
  }

}
