package io.github.scrier.opus.nuke.task.procedures;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.nuke.task.BaseTaskProcedure;

public class ExecuteTaskProcedure extends BaseTaskProcedure implements Callable<String> {
	
	private static Logger log = LogManager.getLogger(ExecuteTaskProcedure.class);
	
	private NukeCommand command;
	
	public final int RUNNING = CREATED + 1;
	
	public ExecuteTaskProcedure(NukeCommand command) {
		log.trace("ExecuteTaskProcedure(" + command + ")");
		setCommand(new NukeCommand(command));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void init() throws Exception {
	  getExecutor().submit(this);
	  getNukeInfo().setActiveCommands(getNukeInfo().getActiveCommands() + 1);
	  getNukeInfo().setRequestedCommands(getNukeInfo().getRequestedCommands() + 1);
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void shutDown() throws Exception {
		log.trace("shutDown()");
		if( true != isProcedureFinished() ) {
			throw new RuntimeException("shutDown called in a state where we arent finished.");
		} else {
		  getNukeInfo().setActiveCommands(getNukeInfo().getActiveCommands() - 1);
		  getNukeInfo().setCompletedCommands(getNukeInfo().getCompletedCommands() + 1);
		}
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int handleOnUpdated(BaseNukeC data) {
		log.trace("handleOnUpdated(" + data + ")");
		// do nothing here for now, might need to check that we dont have it still in the map?
	  return getState();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int handleOnEvicted(BaseNukeC data) {
		log.trace("handleOnEvicted(" + data + ")");
		if( data.getKey() == getCommand().getKey() ) {
			log.error("NukeCommand: " + getNukeInfo() + " was evicted.");
			setState(ABORTED);
		}
	  return getState();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int handleOnRemoved(BaseNukeC data) {
		log.trace("handleOnRemoved(" + data + ")");
		if( data.getKey() == getCommand().getKey() && true != isProcedureFinished() ) {
			log.error("NukeCommand: " + getNukeInfo() + " was removed before we were finished.");
			setState(ABORTED);
		}
	  return getState();
  }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public String call() throws Exception {
		log.trace("call()");
		setState(RUNNING);
		getCommand().setState(CommandState.WORKING);
		if( true != updateEntry(getCommand()) ) {
			log.error("Unable to update command: " + getCommand() + " in ExecuteTaskProcedure.call");
			setState(ABORTED);
  		return "Unable to update command: " + getCommand() + " in ExecuteTaskProcedure.call";
  	} 
	  String executeString = getCommand().getCommand();
	  boolean result = executeProcess(executeString, null, null);
	  log.info("Process returns: " + result + ".");
	  if( result ) {
	  	getCommand().setState(CommandState.DONE);
	  	setState(COMPLETED);
	  } else {
	  	getCommand().setState(CommandState.ABORTED);
	  	setState(ABORTED);
	  }
  	if( true != updateEntry(getCommand()) ) { // this command should trigger call to OnUpdated that should terminate this procedure.
  		log.error("Unable to update command: " + getCommand() + " in ExecuteTaskProcedure.call");
  		return "Unable to update command: " + getCommand() + " in ExecuteTaskProcedure.call";
  	} 
	  return null;
  }

	/**
	 * @return the command
	 */
  public NukeCommand getCommand() {
	  return command;
  }

	/**
	 * @param command the command to set
	 */
  public void setCommand(NukeCommand command) {
	  this.command = command;
  }

}
