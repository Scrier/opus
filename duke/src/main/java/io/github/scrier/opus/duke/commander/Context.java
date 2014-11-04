package io.github.scrier.opus.duke.commander;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum Context {
	INSTANCE;
	
	private static Logger log = LogManager.getLogger(Context.class);
	
	private boolean doOnce;
	private DukeCommander commander;
	
	private Context() {
		this.doOnce = true;
		this.setCommander(null);
	}
	
	public void init(DukeCommander commander) {
		log.trace("init(" + commander + ")");
		if( doOnce ) {
			this.setCommander(commander);
		} else {
			log.error("init alread called.");
		}
	}
	
	public boolean registerProcedure(BaseProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		return (null == getCommander()) ? false : getCommander().registerProcedure(procedure);
	}
	
	public boolean registerProcedure(BaseProcedure procedure, Long componentID) {
		log.trace("registerProcedure(" + procedure + ", " + componentID + ")");
		return (null == getCommander()) ? false : getCommander().registerProcedure(procedure, componentID);
	}

	/**
	 * @return the commander
	 */
	public DukeCommander getCommander() {
		return commander;
	}

	/**
	 * @param commander the commander to set
	 */
	private void setCommander(DukeCommander commander) {
		this.commander = commander;
	}

}
