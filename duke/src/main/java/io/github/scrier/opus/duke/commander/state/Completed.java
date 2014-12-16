package io.github.scrier.opus.duke.commander.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;

/**
 * State handling for Completed transactions
 * @author andreas.joelsson
 */
public class Completed extends State {
	
	private static Logger log = LogManager.getLogger(Completed.class);

	public Completed(ClusterDistributorProcedure parent) {
	  super(parent);
  }

	@Override
	public void init() {
		log.trace("init()");
	}
	
	@Override
	public void updated(BaseNukeC data)  {
		log.trace("updated(" + data + ")");
	}  

	@Override
	public void evicted(BaseNukeC data) {
		log.trace("evicted(" + data + ")");
	}

	@Override
	public void removed(Long key) {
		log.trace("removed(" + key + ")");
	}

	@Override
	public void timeout(long id) {
		log.trace("timeout(" + id + ")");
	}

}
