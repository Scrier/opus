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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeExecuteIndMsgC;
import io.github.scrier.opus.common.nuke.NukeExecuteReqMsgC;
import io.github.scrier.opus.common.nuke.NukeExecuteRspMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;

public class CommandProcedure extends BaseDukeProcedure {
	
	private static Logger log = LogManager.getLogger(CommandProcedure.class);

	public final int INITIALIZING = CREATED + 1;
	public final int WORKING =      CREATED + 2;
	public final int REMOVING =     CREATED + 3;
	
	private long destination;
	private String command;
	private boolean repeated;
	private String folder;
	private ICommandCallback callback;
	private CommandState currentState;
	private String response;
	private long sagaID;

	public CommandProcedure(long destination, String command) {
		this(destination, command, false, null);
	}
	
	public CommandProcedure(long destination, String command, ICommandCallback callback) {
		this(destination, command, false, callback);
	}
	
	public CommandProcedure(long destination, String command, boolean repeated) {
		this(destination, command, repeated, null);
	}
	
	public CommandProcedure(long destination, String command, boolean repeated, ICommandCallback callback) {
		this(destination, command, "", repeated, callback);
	}
	
	public CommandProcedure(long destination, String command, String folder, boolean repeated) {
		this(destination, command, folder, repeated, null);
	}
	
	public CommandProcedure(long destination, String command, String folder, boolean repeated, ICommandCallback callback) {
		log.trace("CommandProcedure(" + destination + ", \"" + command + "\", \"" + folder + "\", " + repeated + ", " + callback + ")");
		setDestination(destination);
		setCommand(command);
		setFolder(folder);
		setRepeated(repeated);
		setCallback(callback);
		setCurrentState(CommandState.UNDEFINED);
		setSagaID(getNextSagaID());
	}

	@Override
	public void init() throws Exception {
		log.trace("init()");
		NukeExecuteReqMsgC pNukeExecuteReq = new NukeExecuteReqMsgC(getSendIF());
		pNukeExecuteReq.setTxID(getTxID());
		pNukeExecuteReq.setSource(getIdentity());
		pNukeExecuteReq.setDestination(getDestination());
		pNukeExecuteReq.setCommand(getCommand());
		pNukeExecuteReq.setFolder(getFolder());
		pNukeExecuteReq.setRepeated(isRepeated());
		pNukeExecuteReq.send();
		setState(INITIALIZING);
	}

	@Override
	public void shutDown() throws Exception {
		log.trace("shutDown()");
		if( null != getCallback() ) {
			getCallback().finished(getDestination(),
					getState(), getCommand(), getResponse());
		}
	}

	@Override
	public int handleOnUpdated(BaseDataC data) {
		log.trace("handleOnUpdated(" + data + ")");
		return getState();
	}

	@Override
	public int handleOnEvicted(BaseDataC data) {
		log.trace("handleOnEvicted(" + data + ")");
		return getState();
	}

	@Override
	public int handleOnRemoved(Long key) {
		log.trace("handleOnRemoved(" + key + ")");
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
   * {@inheritDoc}
   */
  @Override
  public String toString() {
  	String retValue = "CommandProcedure{destination:" + getDestination();
  	retValue += ", command:" + getCommand();
  	retValue += ", repeated:" + isRepeated();
  	retValue += ", folder:" + getFolder();
  	retValue += ", callback:" + getCallback();
  	retValue += ", currentState:" + getCurrentState();
  	retValue += ", response:" + getResponse() + "}";
  	return retValue;
  }

  /**
   * {@inheritDoc}
   */
	@Override
  public int handleInMessage(BaseMsgC message) {
		log.trace("handleInMessage(" + message + ")");
	  switch( message.getId() ) {
	  	case NukeMsgFactory.NUKE_EXECUTE_RSP: {
	  		log.debug("Received NUKE_EXECUTE_RSP message.");
	  		NukeExecuteRspMsgC pNukeExecuteRsp = new NukeExecuteRspMsgC(message);
	  		handleMessage(pNukeExecuteRsp);
	  		break;
	  	}
	  	case NukeMsgFactory.NUKE_EXECUTE_IND: {
	  		log.debug("Received NUKE_EXECUTE_IND message.");
	  		NukeExecuteIndMsgC pNukeExecuteInd = new NukeExecuteIndMsgC(message);
	  		handleMessage(pNukeExecuteInd);
	  		break;
	  	}
	  }
	  return getState();
  }

	/**
	 * @return the destination
	 */
  public long getDestination() {
	  return destination;
  }

	/**
	 * @param destination the destination to set
	 */
  public void setDestination(long destination) {
	  this.destination = destination;
  }

	/**
	 * @return the command
	 */
  public String getCommand() {
	  return command;
  }

	/**
	 * @param command the command to set
	 */
  public void setCommand(String command) {
	  this.command = command;
  }

	/**
	 * @return the repeated
	 */
  public boolean isRepeated() {
	  return repeated;
  }

	/**
	 * @param repeated the repeated to set
	 */
  public void setRepeated(boolean repeated) {
	  this.repeated = repeated;
  }

	/**
	 * @return the folder
	 */
  public String getFolder() {
	  return folder;
  }

	/**
	 * @param folder the folder to set
	 */
  public void setFolder(String folder) {
	  this.folder = folder;
  }

	/**
	 * @return the currentState
	 */
  public CommandState getCurrentState() {
	  return currentState;
  }

	/**
	 * @param currentState the currentState to set
	 */
  public void setCurrentState(CommandState currentState) {
	  this.currentState = currentState;
  }

	/**
	 * @return the response
	 */
  public String getResponse() {
	  return response;
  }

	/**
	 * @param response the response to set
	 */
  public void setResponse(String response) {
	  this.response = response;
  }
  
	/**
	 * @return the sagaID
	 */
  public long getSagaID() {
	  return sagaID;
  }

	/**
	 * @param sagaID the sagaID to set
	 */
  public void setSagaID(long sagaID) {
	  this.sagaID = sagaID;
  }
  
  /**
   * Method to handle the NukeExecuteRspMsgC message.
   * @param message NukeExecuteRspMsgC instance
   */
  protected void handleMessage(NukeExecuteRspMsgC message) {
  	log.trace(" handleMessage(" + message + ")");
  	if( getTxID() == message.getTxID() ) {
  		if( INITIALIZING != getState() ) {
  			log.error("Received NukeExecuteRspMsgC in wrong state: " + getState() + ", expected: " + INITIALIZING + ".");
  			setState(ABORTED);
  		} else {
	  		log.info("Received: " + message);
	  		setResponse(message.getResponse());
	  		setState(WORKING);
  		}
  	}
  }
  
  /**
   * Method to handle the NukeExecuteIndMsgC message.
   * @param message NukeExecuteIndMsgC instance
   */
  protected void handleMessage(NukeExecuteIndMsgC message) {
  	log.trace(" handleMessage(" + message + ")");
  	if( getDestination() == message.getSource() ) {
  		log.info("Received: " + message);
  		if( WORKING != getState() ) {
  			log.error("Received NukeExecuteIndMsgC when not in state WORKING.");
  			setState(ABORTED);
  		} else {
  			log.info("[" + getTxID() + "] Changed status to: " + message.getStatus() + ".");
  			switch( message.getStatus() ) {
  				case ABORTED: {
  					log.info("Task reports aborted state with response: " + message.getResponse() + ".");
  					setResponse(message.getResponse());
  					setState(ABORTED);
  					break;
  				}
  				case DONE: {
  					log.info("Task reports done state with response: " + message.getResponse() + ".");
  					setResponse(message.getResponse());
  					setState(COMPLETED);
  					break;
  				}
  				default: {
  					// do nothing.
  				}
  			}
  			setCurrentState(message.getStatus());
  		}
  	}
  }

}
