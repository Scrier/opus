package io.github.scrier.opus.nuke.task.procedures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.nuke.task.BaseTaskProcedure;

public class QueryTaskProcedure extends BaseTaskProcedure {

	private static Logger log = LogManager.getLogger(QueryTaskProcedure.class);
	
	private NukeCommand command;
	
	boolean stopped;
	boolean terminated;
	
	public QueryTaskProcedure(NukeCommand command) {
		log.trace("QueryTaskProcedure(" + command + ")");
		setCommand(command);
		setStopped(false);
		setTerminated(false);
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
  public int handleOnRemoved(Long key) {
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
  
  /**
   * @param stopped the stopped to set
   */
  public void setStopped(boolean stopped) {
  	this.stopped = stopped;
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public boolean isStopped() {
	  return this.stopped;
  }
	
  /**
   * @param terminated the terminated to set
   */
  public void setTerminated(boolean terminated) {
  	this.terminated = terminated;
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public boolean isTerminated() {
	  return this.terminated;
  }

}
