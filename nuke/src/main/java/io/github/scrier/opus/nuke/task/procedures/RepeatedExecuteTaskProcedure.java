/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Andreas Joelsson (andreas.joelsson@gmail.com)
 */
package io.github.scrier.opus.nuke.task.procedures;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.nuke.task.BaseTaskProcedure;
import io.github.scrier.opus.nuke.task.Context;
import io.github.scrier.opus.nuke.task.StreamGobbler;
import io.github.scrier.opus.nuke.task.StreamGobblerToFile;
import io.github.scrier.opus.nuke.task.StreamGobblerToLog4j;

public class RepeatedExecuteTaskProcedure extends BaseTaskProcedure implements Callable<String> {

	private static Logger log = LogManager.getLogger(RepeatedExecuteTaskProcedure.class);
	
	private NukeCommand command;
	private boolean repeated;
	private int completedCommands;
	private Context theContext = Context.INSTANCE;
	
	boolean stopped;
	boolean terminated;
	
	public final int RUNNING = CREATED + 1;
	
	public RepeatedExecuteTaskProcedure(NukeCommand command) {
		log.trace("RepeatedExecuteTaskProcedure(" + command + ")");
		setCommand(command);
		setCompletedCommands(0);
		setRepeated(command.isRepeated());
		setStopped(false);
		setTerminated(false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public void init() throws Exception {
		log.info("init()");
		if( !getCommand().isRepeated() ) {
			log.fatal("[" + getTxID() + "] Started a RepeatedExecuteTaskProcedure with command that isn't repeated.");
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
			log.info("[" + getTxID() + "] shutDown of procedure after " + getCompletedCommands() + " commands.");
		  getNukeInfo().setActiveCommands(getNukeInfo().getActiveCommands() - 1);
		  getNukeInfo().setCompletedCommands(getNukeInfo().getCompletedCommands() + 1);
		}
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int handleOnUpdated(BaseDataC data) {
		log.trace("handleOnUpdated(" + data + ")");
		if( data.getKey() == getCommand().getKey() ) {
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
  public int handleOnEvicted(BaseDataC data) {
		log.trace("handleOnEvicted(" + data + ")");
		if( data.getKey() == getCommand().getKey() ) {
			log.error("[" + getTxID() + "] NukeCommand: " + getNukeInfo() + " was evicted.");
			setState(ABORTED);
		}
	  return getState();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int handleOnRemoved(Long key) {
		log.trace("handleOnRemoved(" + key + ")");
		if( key == getCommand().getKey() && true != isProcedureFinished() ) {
			log.error("[" + getTxID() + "] NukeCommand: " + getNukeInfo() + " was removed before we were finished.");
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
			log.error("[" + getTxID() + "] Unable to update command: " + getCommand() + " in ExecuteTaskProcedure.call");
			setState(ABORTED);
  		return "Unable to update command: " + getCommand() + " in ExecuteTaskProcedure.call";
  	} 
	  String executeString = getCommand().getCommand();
	  do {
	  	File folder = null;
	  	StreamGobbler gobbler = null;
	  	if( true != getCommand().getFolder().isEmpty() ) {
	  		folder = new File(getCommand().getFolder());
	  	}
	  	if( true == theContext.containsSetting(Shared.Settings.EXECUTE_GOBBLER_LEVEL) ) {
	  		log.debug("Creating gobbler StreamGobblerToLog4j");
	  		gobbler = new StreamGobblerToLog4j(theContext.getSetting(Shared.Settings.EXECUTE_GOBBLER_LEVEL), getCommand().getTxID());
	  	}
	  	else if( true == theContext.containsSetting(Shared.Settings.EXECUTE_GOBBLER_DIR) ) {
	  		log.debug("Creating gobbler StreamGobblerToFile");
	  		File target = new File(theContext.getSetting(Shared.Settings.EXECUTE_GOBBLER_DIR) + "/" + "process-" + getCommand().getTxID() + ".log");
	  		target.createNewFile();
	  		gobbler = new StreamGobblerToFile(target);
	  	}
	  	boolean result = executeProcess(executeString, folder, gobbler);
		  log.debug("[" + getTxID() + "] Process returns: " + result + ".");
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
  		log.error("[" + getTxID() + "] Unable to update command: " + getCommand() + " in ExecuteTaskProcedure.call");
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
			log.info("[" + getTxID() + "] Received command to stop execution from " + nukeCommand.getComponent() + ".");
			setRepeated(false);
			setStopped(true);
		} else if ( CommandState.TERMINATE == nukeCommand.getState() ) {
			log.info("[" + getTxID() + "] Received command to terminate execution from " + nukeCommand.getComponent() + ".");
			setRepeated(false);
			terminateProcess();
			setStopped(false);    // not necessary as terminated has precedence.
			setTerminated(true);
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
