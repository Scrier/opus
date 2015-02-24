package io.github.scrier.opus.duke.commander;

import io.github.scrier.opus.common.aoc.BaseActiveObject;
import io.github.scrier.opus.common.message.BaseMsgC;

import com.hazelcast.core.HazelcastInstance;

public class BaseActiveObjectMock extends BaseActiveObject {
	
	public BaseMsgC LastMessage;
	public int InitCalls;
	public int ShutDownCalls;

	public BaseActiveObjectMock(HazelcastInstance instance) {
		super(instance);
		LastMessage = null;
		InitCalls = 0;
		ShutDownCalls = 0;
	}

	@Override
	public void init() {
		InitCalls++;
	}

	@Override
	public void shutDown() {
		ShutDownCalls++;
	}

	@Override
  public void handleInMessage(BaseMsgC message) {
	  LastMessage = message;
  }

}
