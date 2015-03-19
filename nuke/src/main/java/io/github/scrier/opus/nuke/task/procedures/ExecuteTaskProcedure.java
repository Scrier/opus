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

import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeExecuteReqMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;
import io.github.scrier.opus.common.nuke.NukeStopAllReqMsgC;
import io.github.scrier.opus.common.nuke.NukeTerminateAllReqMsgC;
import io.github.scrier.opus.nuke.task.BaseTaskProcedure;

public class ExecuteTaskProcedure extends BaseTaskProcedure implements Callable<String> {
	
	private static Logger log = LogManager.getLogger(ExecuteTaskProcedure.class);
	
	public final int RUNNING = CREATED + 1;
	
	public ExecuteTaskProcedure(NukeExecuteReqMsgC message) {
		super(message);
		log.trace("ExecuteTaskProcedure(" + message + ")");
		setProcessID(Constants.HC_UNDEFINED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void init() throws Exception {
		log.trace("init()");
		log.info("init to id: " + getIdentity() + ".");
		setProcessID(getUniqueID());
		sendResponse();
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
			log.fatal("shutDown called in a state where we arent finished.");
			throw new RuntimeException("shutDown called in a state where we arent finished.");
		} else {
		  getNukeInfo().setActiveCommands(getNukeInfo().getActiveCommands() - 1);
		  getNukeInfo().setCompletedCommands(getNukeInfo().getCompletedCommands() + 1);
		}
		cleanUp();
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
			case NukeMsgFactory.NUKE_STOP_ALL_REQ: {
				NukeStopAllReqMsgC pNukeStopReq = new NukeStopAllReqMsgC(message);
				handleMessage(pNukeStopReq);
				break;
			}
			case NukeMsgFactory.NUKE_TERMINATE_ALL_REQ: {
				NukeTerminateAllReqMsgC pNukeTerminateReq = new NukeTerminateAllReqMsgC(message);
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
	  boolean result = false;
  	if( getFolder().isEmpty() ) {
  		result = executeProcess(executeString, null, null);
  	} else {
  		result = executeProcess(executeString, new File(getFolder()), null);
  	}
	  log.debug("[" + getTxID() + "] Process returns: " + result + ".");
	  if( result ) {
	  	sendCommandStateUpdate(CommandState.DONE);
	  	setState(COMPLETED);
	  } else {
	  	sendCommandStateUpdate(CommandState.ABORTED, getErrorMessage());
	  	setState(ABORTED);
	  }
	  return null;
  }

}
