package io.github.scrier.opus.nuke.task.procedures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.nuke.task.BaseTaskProcedure;

public class QueryTaskProcedure extends BaseTaskProcedure {

	private static Logger log = LogManager.getLogger(QueryTaskProcedure.class);
	
	private NukeCommand command;
	
	public QueryTaskProcedure(NukeCommand command) {
		log.trace("QueryTaskProcedure(" + command + ")");
		setCommand(command);
	}
	
	@Override
  public void init() throws Exception {
	  // TODO Auto-generated method stub
	  
  }

	@Override
  public void shutDown() throws Exception {
	  // TODO Auto-generated method stub
	  
  }

	@Override
  public int handleOnUpdated(BaseNukeC data) {
	  // TODO Auto-generated method stub
	  return 0;
  }

	@Override
  public int handleOnEvicted(BaseNukeC data) {
	  // TODO Auto-generated method stub
	  return 0;
  }

	@Override
  public int handleOnRemoved(BaseNukeC data) {
	  // TODO Auto-generated method stub
	  return 0;
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
