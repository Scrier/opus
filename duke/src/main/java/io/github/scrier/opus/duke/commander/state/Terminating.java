package io.github.scrier.opus.duke.commander.state;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;
import io.github.scrier.opus.duke.commander.CommandProcedure;
import io.github.scrier.opus.duke.commander.Context;
import io.github.scrier.opus.duke.commander.ICommandCallback;
import io.github.scrier.opus.duke.commander.INukeInfo;

/**
 * State handling for Ramping Down transactions.
 * @author andreas.joelsson
 * {@code
 * TERMINATE -> ABORTED
 * TERMINATE -> COMPLETED
 * }
 */
public class Terminating extends State implements ICommandCallback {

	private static Logger log = LogManager.getLogger(Terminating.class);

	private int terminateTimeout;
	private List<Long> activeNukeCommands;
	private Context theContext = Context.INSTANCE;
	
	public Terminating(ClusterDistributorProcedure parent) {
	  super(parent);
		setTerminateTimeout(30);
		setActiveNukeCommands(new ArrayList<Long>());
  }
	
	/**
	 * Terminating handling on init methods.
	 */
	@Override
	public void init() {
		log.trace("init()");
		List<INukeInfo> nukes = theContext.getNukes();
		log.info("Sending terminate command to " + nukes.size() + " nukes will terminate this applicatio in " + getTerminateTimeout() + " seconds.");
		for( INukeInfo info : nukes ) {
			registerProcedure(new CommandProcedure(info.getNukeID(), Shared.Commands.Execute.TERMINATE_EXECUTION, CommandState.EXECUTE, this));
			getActiveNukeCommands().add(info.getNukeID());
		}
		startTimeout(getTerminateTimeout(), getTimerID());
	}
	
	/**
	 * Terminating handling on update methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void updated(BaseNukeC data)  {
		log.trace("updated(" + data + ")");
	}  

	/**
	 * Terminating handling on evicted methods.
	 * @param data BaseNukeC
	 */
	@Override
	public void evicted(BaseNukeC data) {
		log.trace("evicted(" + data + ")");
	}

	/**
	 * Terminating handling on removed methods.
	 * @param key Long
	 */
	@Override
	public void removed(Long key) {
		log.trace("removed(" + key + ")");
	}

	/**
	 * RampingDown handling on timeout methods.
	 * @param id long
	 */
	@Override
	public void timeout(long id) {
		log.trace("timeout(" + id + ")");
		if( id == getTimerID() ) {
			handleTimerTick();
		} else if ( id == getTerminateID() ) {
			log.fatal("Received terminate timeout during state TERMINATING.");
			throw new RuntimeException("Received terminate timeout during state TERMINATING.");
		} else {
			log.fatal("Received unknown timer id: " + id + " in state RAMPING_DOWN.");
			throw new RuntimeException("Received terminate timeout during state TERMINATING.");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public void finished(long nukeID, int state, String query, String result) {
		log.trace("finished(" + nukeID + ", " + state + ", " + query + ", " + result + ")");
		if( COMPLETED == state ) {
			log.info("Stop Execution command received ok from node " + nukeID + " still " + (getActiveNukeCommands().size() - 1) + " remaining.");
			if( getActiveNukeCommands().contains(nukeID) ) {
				getActiveNukeCommands().remove(nukeID);
				if( true == getActiveNukeCommands().isEmpty() ) {
					log.info("All nukes has terminated successful, we are done.");
					theContext.terminateTimeout(getTimerID());
					setState(COMPLETED);
				}
			} else {
				log.fatal("Received status from unknown nuke with id: " + nukeID + ".");
				throw new RuntimeException("Received status from unknown nuke with id: " + nukeID + ".");
			}
		} else {
			log.fatal("Received finish from nukeid " + nukeID + " but unhandled state: " + state + ".");
			throw new RuntimeException("Received finish from nukeid " + nukeID + " but unhandled state: " + state + ".");
		}
	}
	
	/**
	 * Handle timer ticks.
	 */
	private void handleTimerTick() {
		log.trace("handleTimerTick()");
		log.fatal("Received timeout of termination before nukes has terminated their processes, terminating.");
		throw new RuntimeException("Received timeout of termination before nukes has terminated their processes, terminating.");
	}

	/**
	 * @return the terminateTimeout
	 */
  public int getTerminateTimeout() {
    return terminateTimeout;
  }

	/**
	 * @param terminateTimeout the terminateTimeout to set
	 */
  public void setTerminateTimeout(int terminateTimeout) {
    this.terminateTimeout = terminateTimeout;
  }

	/**
	 * @return the activeNukeCommands
	 */
  public List<Long> getActiveNukeCommands() {
    return activeNukeCommands;
  }

	/**
	 * @param activeNukeCommands the activeNukeCommands to set
	 */
  public void setActiveNukeCommands(List<Long> activeNukeCommands) {
    this.activeNukeCommands = activeNukeCommands;
  }

}
