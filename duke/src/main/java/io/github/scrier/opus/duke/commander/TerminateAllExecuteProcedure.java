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
package io.github.scrier.opus.duke.commander;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;
import io.github.scrier.opus.common.nuke.NukeTerminateAllReqMsgC;
import io.github.scrier.opus.common.nuke.NukeTerminateAllRspMsgC;

public class TerminateAllExecuteProcedure extends BaseDukeProcedure {
	
	private static Logger log = LogManager.getLogger(TerminateAllExecuteProcedure.class);
	
	public final int WAITING_FOR_TERMINATE_RSP = CREATED + 1;

	private ICommandCallback callback;
	private long nukeID;
	private String result;
	
	/**
	 * Constructor
	 * @param id long with the id of the nuke.
	 * @param callback callback interface with result of the execution.
	 */
	public TerminateAllExecuteProcedure(long id, ICommandCallback callback) {
		log.trace("TerminateAllExecuteProcedure(" + id + ", " + callback + ")");
		setNukeID(id);
		setCallback(callback);
	}

	@Override
  public void init() throws Exception {
	  log.trace("init()");
	  log.info("[" + getTxID() + "] Sending terminate command to nuke with id: " + getNukeID() + ".");
	  NukeTerminateAllReqMsgC pNukeTerminateAllReq = new NukeTerminateAllReqMsgC(getSendIF());
	  pNukeTerminateAllReq.setSource(getIdentity());
	  pNukeTerminateAllReq.setDestination(getNukeID());
	  pNukeTerminateAllReq.setTxID(getTxID());
	  pNukeTerminateAllReq.send();
	  setState(WAITING_FOR_TERMINATE_RSP);
  }

	@Override
  public void shutDown() throws Exception {
		log.trace("shutDown()");
		getCallback().finished(getNukeID(), Constants.HC_UNDEFINED, getState(), null, getResult());
  }

	@Override
  public int handleOnUpdated(BaseDataC value) {
		log.trace("handleOnUpdated(" + value + ")");
		return getState();
  }

	@Override
  public int handleOnEvicted(BaseDataC value) {
		log.trace("handleOnEvicted(" + value + ")");
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
		assert Constants.HC_UNDEFINED != message.getSource() : "Source should not be undefined: " + message.getSource() + ".";
		assert Constants.HC_UNDEFINED != message.getDestination() : "Destination should not be undefined: " + message.getDestination() + ".";
		switch( message.getId() ) {
			case NukeMsgFactory.NUKE_TERMINATE_ALL_RSP: {
				NukeTerminateAllRspMsgC pNukeTerminateAllRsp = new NukeTerminateAllRspMsgC(message);
				handleMessage(pNukeTerminateAllRsp);
				break;
			}
			default: {
				// do nothing
				log.debug("Default switch for message with id: " + message.getId() + ".");
			}
		}
		return getState();
  }

	/**
	 * @return the callback
	 */
  public ICommandCallback getCallback() {
	  return callback;
  }

	/**
	 * @param callback the callback to set
	 */
  public void setCallback(ICommandCallback callback) {
	  this.callback = callback;
  }

	/**
	 * @return the nukeID
	 */
  public long getNukeID() {
	  return nukeID;
  }

	/**
	 * @param nukeID the nukeID to set
	 */
  public void setNukeID(long nukeID) {
	  this.nukeID = nukeID;
  }

	/**
	 * @return the result
	 */
  public String getResult() {
	  return result;
  }

	/**
	 * @param result the result to set
	 */
  public void setResult(String result) {
	  this.result = result;
  }
  
  /**
   * Method to handle the NukeTerminateAllRspMsgC message.
   * @param message
   */
  protected void handleMessage(NukeTerminateAllRspMsgC message) {
  	log.trace("handleMessage(" + message + ")"); 
  	if( getTxID() == message.getTxID() ) {
  		if( WAITING_FOR_TERMINATE_RSP != getState() ) {
  			setResult("Received NukeTerminateAllRspMsgC in wrong state, expected: " + WAITING_FOR_TERMINATE_RSP + ", but was: " + getState() + ".");
  			log.error("[" + getTxID() + "] " + getResult());
  			setState(ABORTED);
  		} else if ( true != message.isSuccess() ) {
  			setResult(message.getStatus());
  			log.error("[" + getTxID() + "] Terminate all failed with status: \"" + getResult() + "\".");
  			setState(ABORTED);
  		} else {
  			log.info("[" + getTxID() + "] Terminate command have successfully been finished to node " + message.getSource() + ".");
  			setResult(message.getStatus());
  			setState(COMPLETED);
  		}
  	}
  }
	
}
