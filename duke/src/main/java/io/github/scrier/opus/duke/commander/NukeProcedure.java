package io.github.scrier.opus.duke.commander;

import io.github.scrier.opus.common.nuke.NukeInfo;

public class NukeProcedure extends BaseProcedure {
	
	private NukeInfo local;
	
	private final int WORKING = CREATED + 1;
	
	public NukeProcedure(NukeInfo info) {
		local = new NukeInfo(info);
	}
	
	@Override
	public void init() throws Exception {
		setState(WORKING);
	}

	@Override
	public void shutDown() throws Exception {
		local = null;
	}

	@Override
	public int handleOnUpdated(NukeInfo info) {
		// TODO Auto-generated method stub
		return getState();
	}

	@Override
	public int handleOnEvicted(NukeInfo info) {
		// TODO Auto-generated method stub
		return getState();
	}

}
