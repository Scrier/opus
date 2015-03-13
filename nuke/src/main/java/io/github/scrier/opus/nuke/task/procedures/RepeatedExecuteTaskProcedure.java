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
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeExecuteReqMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;
import io.github.scrier.opus.common.nuke.NukeStopReqMsgC;
import io.github.scrier.opus.common.nuke.NukeTerminateReqMsgC;
import io.github.scrier.opus.nuke.task.BaseTaskProcedure;
import io.github.scrier.opus.nuke.task.StreamGobbler;
import io.github.scrier.opus.nuke.task.StreamGobblerToFile;
import io.github.scrier.opus.nuke.task.StreamGobblerToLog4j;

public class RepeatedExecuteTaskProcedure extends BaseTaskProcedure implements Callable<String> {

	private static Logger log = LogManager.getLogger(RepeatedExecuteTaskProcedure.class);
	
	private int completedCommands;
	
	public final int RUNNING = CREATED + 1;
	
	public RepeatedExecuteTaskProcedure(NukeExecuteReqMsgC message) {
		super(message);
		log.trace("RepeatedExecuteTaskProcedure(" + message + ")");
		setRepeated(message.isRepeated());
		setCompletedCommands(0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public void init() throws Exception {
		log.info("init()");
		setProcessID(getUniqueID());
		sendResponse();
		if( !isRepeated() ) {
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
	  return getState();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int handleOnEvicted(BaseDataC data) {
		log.trace("handleOnEvicted(" + data + ")");
	  return getState();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int handleOnRemoved(Long key) {
		log.trace("handleOnRemoved(" + key + ")");
	  return getState();
  }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public int handleInMessage(BaseMsgC message) {
		log.trace("handleInMessage(" + message + ")");
		switch( message.getId() ) {
			case NukeMsgFactory.NUKE_STOP_REQ: {
				NukeStopReqMsgC pNukeStopReq = new NukeStopReqMsgC(message);
				handleMessage(pNukeStopReq);
				break;
			}
			case NukeMsgFactory.NUKE_TERMINATE_REQ: {
				NukeTerminateReqMsgC pNukeTerminateReq = new NukeTerminateReqMsgC(message);
				handleMessage(pNukeTerminateReq);
				break;
			}
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
		sendCommandStateUpdate(CommandState.WORKING);
	  String executeString = getCommand();
	  do {
	  	File folder = null;
	  	StreamGobbler gobbler = null;
	  	if( true != getFolder().isEmpty() ) {
	  		folder = new File(getFolder());
	  	}
	  	if( true == getContext().containsSetting(Shared.Settings.EXECUTE_GOBBLER_LEVEL) ) {
	  		log.debug("Creating gobbler StreamGobblerToLog4j");
	  		gobbler = new StreamGobblerToLog4j(getContext().getSetting(Shared.Settings.EXECUTE_GOBBLER_LEVEL), getMsgTxID());
	  	}
	  	else if( true == getContext().containsSetting(Shared.Settings.EXECUTE_GOBBLER_DIR) ) {
	  		log.debug("Creating gobbler StreamGobblerToFile");
	  		File target = new File(getContext().getSetting(Shared.Settings.EXECUTE_GOBBLER_DIR) + "/" + "process-" + getMsgTxID() + ".log");
	  		target.createNewFile();
	  		gobbler = new StreamGobblerToFile(target);
	  	}
	  	boolean result = executeProcess(executeString, folder, gobbler);
		  log.debug("[" + getTxID() + "] Process returns: " + result + ".");
		  if( !isRepeated() && result ) {
		  	sendCommandStateUpdate(CommandState.DONE);
		  	setState(COMPLETED);
		  } else if( true != result ){
		  	sendCommandStateUpdate(CommandState.ABORTED, getErrorMessage());
		  	setState(ABORTED);
		  }
		  incCompletedCommands();
	  } while ( RUNNING == getState() && isRepeated() );
	  return null;
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
