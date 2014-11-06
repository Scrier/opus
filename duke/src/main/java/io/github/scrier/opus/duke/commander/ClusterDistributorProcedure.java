package io.github.scrier.opus.duke.commander;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;

public class ClusterDistributorProcedure extends BaseProcedure {
	
	private static Logger log = LogManager.getLogger(ClusterDistributorProcedure.class);

	public ClusterDistributorProcedure() {

	}

	@Override
	public void init() throws Exception {
		log.trace("init()");
	}

	@Override
	public void shutDown() throws Exception {
		log.trace("shutDown()");
	}

	@Override
	public int handleOnUpdated(BaseNukeC data) {
		log.trace("handleOnUpdated(" + data + ")");
		return getState();
	}

	@Override
	public int handleOnEvicted(BaseNukeC data) {
		log.trace("handleOnEvicted(" + data + ")");
		return getState();
	}

	@Override
	public int handleOnRemoved(BaseNukeC data) {
		log.trace("handleOnRemoved(" + data + ")");
		return getState();
	}

}
