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

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.hazelcast.partition.client.GetPartitionsRequest;

import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.NukeExecuteReqMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;
import io.github.scrier.opus.common.nuke.NukeStopAllReqMsgC;
import io.github.scrier.opus.common.nuke.NukeStopAllRspMsgC;
import io.github.scrier.opus.common.nuke.NukeTerminateAllReqMsgC;
import io.github.scrier.opus.common.nuke.NukeTerminateAllRspMsgC;
import io.github.scrier.opus.nuke.task.BaseNukeProcedure;
import io.github.scrier.opus.nuke.task.BaseTaskProcedure;

public class DispatchProcedure extends BaseNukeProcedure {
	
	private static Logger log = LogManager.getLogger(DispatchProcedure.class);
	
	public DispatchProcedure() {
		log.trace("DispatchProcedure()");
	}

	@Override
  public void init() throws Exception {
	  log.trace("init()");
  }

	@Override
  public void shutDown() throws Exception {
		log.trace("shutDown()");
  }

	@Override
  public int handleOnUpdated(BaseDataC value) {
		log.trace("handleOnUpdated(" + value + ")");
	  return getState();
  }

	@Override
  public int handleOnEvicted(BaseDataC value) {
		log.trace("handleOnRemoved(" + value + ")");
	  return getState();
  }

	@Override
  public int handleOnRemoved(Long key) {
		log.trace("handleOnRemoved(" + key + ")");
	  return getState();
  }

	@Override
  public int handleInMessage(BaseMsgC message) {
		log.trace("handleInMessage(" + message + ")");
		switch( message.getId() ) {
			case NukeMsgFactory.NUKE_EXECUTE_REQ: {
				NukeExecuteReqMsgC pNukeExecuteReq = new NukeExecuteReqMsgC(message);
				handleMessage(pNukeExecuteReq);
				break;
			}
			case NukeMsgFactory.NUKE_STOP_ALL_REQ: {
				NukeStopAllReqMsgC pNukeStopAllReq = new NukeStopAllReqMsgC(message);
				handleMessage(pNukeStopAllReq);
				break;
			}
			case NukeMsgFactory.NUKE_TERMINATE_ALL_REQ: {
				NukeTerminateAllReqMsgC pNukeTerminateAllReq = new NukeTerminateAllReqMsgC(message);
				handleMessage(pNukeTerminateAllReq);
				break;
			}
		}
	  return getState();
  }
	
	/**
	 * Method to handle the NukeExecuteReqMsgC message.
	 * @param msg NukeExecuteReqMsgC instance.
	 */
	protected void handleMessage(NukeExecuteReqMsgC msg) {
		log.trace("handleMessage(" + msg + ")");
		if( msg.isRepeated() ) {
			registerProcedure(new RepeatedExecuteTaskProcedure(msg));
		} else {
			registerProcedure(new ExecuteTaskProcedure(msg));
		}
	}
	
	/**
	 * Method to handle the NukeStopAllReqMsgC message.
	 * @param msg NukeStopAllReqMsgC instance.
	 */
	protected void handleMessage(NukeStopAllReqMsgC msg) {
		log.trace("handleMessage(" + msg + ")");
		List<BaseNukeProcedure> executeTasks = getContext().getTask().getProcedures(ExecuteTaskProcedure.class, RepeatedExecuteTaskProcedure.class);
		String error = "";
		int success = 0;
		log.info("Received message to stop all tasks, stopping " + executeTasks.size() + " procedures.");
		for( BaseNukeProcedure baseProc : executeTasks ) {
			BaseTaskProcedure procedure = (BaseTaskProcedure)baseProc;
			log.debug("Stopping procedure " + procedure + ".");
			if( true == procedure.stopProcess() ) {
				success++;
			} else if ( error.isEmpty() ) {
				error += procedure.getErrorMessage();
			} else {
				error += ", " + procedure.getErrorMessage();
			}
		}
		String message = "Stopping " + executeTasks.size() + " procedures with " + (executeTasks.size() - success) + " errors.";
		if( true != error.isEmpty() ) {
			message += " Errors: " + error;
		}
		log.info("Sending response for the stop all request.");
		NukeStopAllRspMsgC pNukeStopAllRsp = new NukeStopAllRspMsgC(getSendIF());
		pNukeStopAllRsp.setSource(getIdentity());
		pNukeStopAllRsp.setDestination(msg.getSource());
		pNukeStopAllRsp.setTxID(msg.getTxID());
		pNukeStopAllRsp.setSuccess(executeTasks.size() == success);
		pNukeStopAllRsp.setStatus(message);
		pNukeStopAllRsp.send();
	}
	
	/**
	 * Method to handle the NukeTerminateAllReqMsgC message.
	 * @param msg NukeTerminateAllReqMsgC instance.
	 */
	protected void handleMessage(NukeTerminateAllReqMsgC msg) {
		log.trace("handleMessage(" + msg + ")");
		List<BaseNukeProcedure> executeTasks = getContext().getTask().getProcedures(ExecuteTaskProcedure.class, RepeatedExecuteTaskProcedure.class);
		String error = "";
		int success = 0;
		log.info("Received message to terminate all tasks, terminating " + executeTasks.size() + " procedures.");
		for( BaseNukeProcedure baseProc : executeTasks ) {
			BaseTaskProcedure procedure = (BaseTaskProcedure)baseProc;
			log.debug("Terminating procedure " + procedure + ".");
			if( true == procedure.terminateProcess() ) {
				success++;
			} else if ( error.isEmpty() ) {
				error += procedure.getErrorMessage();
			} else {
				error += ", " + procedure.getErrorMessage();
			}
		}
		String message = "Terminating " + executeTasks.size() + " procedures with " + (executeTasks.size() - success) + " errors.";
		if( true != error.isEmpty() ) {
			message += " Errors: " + error;
		}
		log.info("Sending response for the terminate all request.");
		NukeTerminateAllRspMsgC pNukeTerminateAllRsp = new NukeTerminateAllRspMsgC(getSendIF());
		pNukeTerminateAllRsp.setSource(getIdentity());
		pNukeTerminateAllRsp.setDestination(msg.getSource());
		pNukeTerminateAllRsp.setTxID(msg.getTxID());
		pNukeTerminateAllRsp.setSuccess(executeTasks.size() == success);
		pNukeTerminateAllRsp.setStatus(message);
		pNukeTerminateAllRsp.send();
	}
	
}
