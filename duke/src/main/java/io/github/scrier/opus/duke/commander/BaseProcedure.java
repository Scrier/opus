package io.github.scrier.opus.duke.commander;

import io.github.scrier.opus.common.node.NukeInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseProcedure {
	
	private static Logger log = LogManager.getLogger(BaseProcedure.class);
	
	private int state;
	private Context theContext;
	
	public final int ABORTED = 0;
	public final int CREATED = 1;
	public final int COMPLETED = 9999;
	
	public BaseProcedure() {
		setState(CREATED);
		theContext = Context.INSTANCE;
	}
	
	public abstract void init() throws Exception;
	
	public abstract void shutDown() throws Exception;

	public abstract int handleOnUpdated(NukeInfo info);
	
	public abstract int handleOnEvicted(NukeInfo info);
	
	public boolean registerProcedure(BaseProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		return theContext.registerProcedure(procedure);
	}
	
	public boolean registerProcedure(BaseProcedure procedure, Long componentID) {
		log.trace("registerProcedure(" + procedure + ", " + componentID + ")");
		return theContext.registerProcedure(procedure, componentID);
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		this.state = state;
	}

}
