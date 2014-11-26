package io.github.scrier.opus.nuke.task.procedures;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.nuke.task.BaseTaskProcedure;

public class ExecuteTaskProcedure extends BaseTaskProcedure implements Callable<String> {
	
	private static Logger log = LogManager.getLogger(ExecuteTaskProcedure.class);
	
	private NukeCommand command;
	
	public final int PROC_RUNNING = CREATED + 1;
	public final int PROC_DONE    = CREATED + 2;
	
	public ExecuteTaskProcedure(NukeCommand command) {
		log.trace("ExecuteTaskProcedure(" + command + ")");
		setCommand(command);
	}

	@Override
  public void init() throws Exception {
	  getExecutor().submit(this);
	  getNukeInfo().setActiveCommands(getNukeInfo().getActiveCommands() + 1);
	  getNukeInfo().setRequestedCommands(getNukeInfo().getRequestedCommands() + 1);
  }

	@Override
  public void shutDown() throws Exception {
	  getNukeInfo().setActiveCommands(getNukeInfo().getActiveCommands() - 1);
	  getNukeInfo().setCompletedCommands(getNukeInfo().getCompletedCommands() + 1);
  }

	@Override
  public int handleOnUpdated(BaseNukeC data) {
	  // TODO Auto-generated method stub
	  return getState();
  }

	@Override
  public int handleOnEvicted(BaseNukeC data) {
	  // TODO Auto-generated method stub
	  return getState();
  }

	@Override
  public int handleOnRemoved(BaseNukeC data) {
	  // TODO Auto-generated method stub
	  return getState();
  }
	
	@Override
  public String call() throws Exception {
		setState(PROC_RUNNING);
	  String executeString = getCommand().getCommand();
	  log.info("Process returns: " + executeProcess(executeString, null, null));
	  setState(PROC_DONE);
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
