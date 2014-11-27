package io.github.scrier.opus.nuke.task.procedures;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.nuke.task.BaseTaskProcedure;

public class RepeatedExecuteTaskProcedure extends BaseTaskProcedure implements Callable<String> {

	private static Logger log = LogManager.getLogger(RepeatedExecuteTaskProcedure.class);
	
	private NukeCommand command;
	private boolean repeated;
	private int completedCommands;
	
	public final int RUNNING = CREATED + 1;
	
	public RepeatedExecuteTaskProcedure(NukeCommand command) {
		log.trace("RepeatedExecuteTaskProcedure(" + command + ")");
		setCommand(command);
		setCompletedCommands(0);
		setRepeated(command.isRepeated());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public void init() throws Exception {
		if( !getCommand().isRepeated() ) {
			throw new RuntimeException("Started a RepeatedExecuteTaskProcedure with command that isn't repeated.");
		} else {
		  getExecutor().submit(this);
		  getNukeInfo().setActiveCommands(getNukeInfo().getActiveCommands() + 1);
		  getNukeInfo().setRequestedCommands(getNukeInfo().getRequestedCommands() + 1);
		}
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
		if( data.getKey() != getCommand().getKey() ) {
			log.debug("Update not for us.");
		} else {
			switch ( data.getId() ) {
				case NukeFactory.NUKE_COMMAND: {
					NukeCommand nukeCommand = new NukeCommand(data);
					handleUpdate(nukeCommand);
					break;
				}
				default: {
					throw new RuntimeException("Unhandled id: " + data.getId() + " received in RepeatedExecuteTaskProcedure.handleOnUpdated(" + data + ").");
				}
			}
		}
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
	  do {
		  boolean result = executeProcess(executeString, null, null);
		  log.info("Process returns: " + result + ".");
		  if( !isRepeated() && result ) {
		  	getCommand().setState(CommandState.DONE);
		  	setState(COMPLETED);
		  } else if( true != result ){
		  	getCommand().setState(CommandState.ABORTED);
		  	setState(ABORTED);
		  }
		  incCompletedCommands();
	  } while ( RUNNING == getState() && isRepeated() );
  	if( true != updateEntry(getCommand()) ) { // this command should trigger call to OnUpdated that should terminate this procedure.
  		log.error("Unable to update command: " + getCommand() + " in ExecuteTaskProcedure.call");
  		return "Unable to update command: " + getCommand() + " in ExecuteTaskProcedure.call";
  	} 
	  return null;
  }
	
	/**
	 * Method to handle updates to the NukeCommand associated with this task procedure.
	 * @param nukeCommand NukeCommand to handle.
	 */
	private void handleUpdate(NukeCommand nukeCommand) {
		log.trace("handleUpdate(" + nukeCommand + ")");
		if( CommandState.STOP == nukeCommand.getState() ) {
			log.info("Received command to stop execution from " + nukeCommand.getComponent() + ".");
			setRepeated(false);
		} else if ( CommandState.TERMINATE == nukeCommand.getState() ) {
			log.info("Received command to terminate execution from " + nukeCommand.getComponent() + ".");
			setRepeated(false);
			terminateProcess();
		}
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

	/**
	 * @return the repeated
	 */
	private boolean isRepeated() {
		return repeated;
	}

	/**
	 * @param repeated the repeated to set
	 */
	private void setRepeated(boolean repeated) {
		this.repeated = repeated;
	}

	/**
	 * @return the completedCommands
	 */
  protected int getCompletedCommands() {
	  return completedCommands;
  }
  
	/**
	 * Method to increase completed commands with 1.
	 */
  private void incCompletedCommands() {
	  ++completedCommands;
  }

	/**
	 * @param completedCommands the completedCommands to set
	 */
  private void setCompletedCommands(int completedCommands) {
	  this.completedCommands = completedCommands;
  }

}
