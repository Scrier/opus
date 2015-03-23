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

import io.github.scrier.opus.common.Constants;
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
	private long processID;
	private long sagaID;

	/**
	 * Constructor
	 * @param destination long with the id of the nuke to call.
	 * @param command String with the command to execute.
	 */
	public CommandProcedure(long destination, String command) {
		this(destination, command, false, null);
	}
	
	/**
	 * Constructor
	 * @param destination long with the id of the nuke to call.
	 * @param command String with the command to execute.
	 * @param callback interface to callback the result of the handling.
	 */
	public CommandProcedure(long destination, String command, ICommandCallback callback) {
		this(destination, command, false, callback);
	}
	
	/**
	 * Constructor
	 * @param destination long with the id of the nuke to call.
	 * @param command String with the command to execute.
	 * @param repeated boolean if the command should be repeated or not.
	 */
	public CommandProcedure(long destination, String command, boolean repeated) {
		this(destination, command, repeated, null);
	}
	
	/**
	 * Constructor
	 * @param destination long with the id of the nuke to call.
	 * @param command String with the command to execute.
	 * @param repeated boolean if the command should be repeated or not.
	 * @param callback interface to callback the result of the handling.
	 */
	public CommandProcedure(long destination, String command, boolean repeated, ICommandCallback callback) {
		this(destination, command, "", repeated, callback);
	}
	
	/**
	 * Constructor
	 * @param destination long with the id of the nuke to call.
	 * @param command String with the command to execute.
	 * @param folder String with the folder to execute the command from.
	 * @param repeated boolean if the command should be repeated or not.
	 */
	public CommandProcedure(long destination, String command, String folder, boolean repeated) {
		this(destination, command, folder, repeated, null);
	}
	
	/**
	 * Constructor
	 * @param destination long with the id of the nuke to call.
	 * @param command String with the command to execute.
	 * @param folder String with the folder to execute the command from.
	 * @param repeated boolean if the command should be repeated or not.
	 * @param callback interface to callback the result of the handling.
	 */
	public CommandProcedure(long destination, String command, String folder, boolean repeated, ICommandCallback callback) {
		log.trace("CommandProcedure(" + destination + ", \"" + command + "\", \"" + folder + "\", " + repeated + ", " + callback + ")");
		setDestination(destination);
		setCommand(command);
		setFolder(folder);
		setRepeated(repeated);
		setCallback(callback);
		setProcessID(Constants.HC_UNDEFINED);
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
			getCallback().finished(getDestination(), getProcessID(),
					getState(), getCommand(), "");
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
  	retValue += ", processID: " + getProcessID() + "}";
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
	  		log.debug("[" + getTxID() + "] Received NUKE_EXECUTE_RSP message.");
	  		NukeExecuteRspMsgC pNukeExecuteRsp = new NukeExecuteRspMsgC(message);
	  		handleMessage(pNukeExecuteRsp);
	  		break;
	  	}
	  	case NukeMsgFactory.NUKE_EXECUTE_IND: {
	  		log.debug("[" + getTxID() + "] Received NUKE_EXECUTE_IND message.");
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
	 * @return the processID
	 */
  public long getProcessID() {
	  return processID;
  }

	/**
	 * @param processID the processID to set
	 */
  public void setProcessID(long processID) {
	  this.processID = processID;
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
  	if( getTxID() != message.getTxID() ) {
  		log.debug("[" + getTxID() + "] Wrong txid. expected: " + getTxID() + ", but was: " + message.getTxID() + ".");
  	} else {
  		if( INITIALIZING != getState() ) {
  			log.error("[" + getTxID() + "] Received NukeExecuteRspMsgC in wrong state: " + getState() + ", expected: " + INITIALIZING + ".");
  			setState(ABORTED);
  		} else {
	  		log.debug("[" + getTxID() + "] Received: " + message + ", updating processID to: " + message.getProcessID() + ".");
	  		setProcessID(message.getProcessID());
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
  		if( getProcessID() != message.getProcessID() ) {
  			log.debug("[" + getTxID() + "] Message not for us, expected: " + getProcessID() + ", received: " + message.getProcessID() + ".");
  		} else {
  			log.debug("[" + getTxID() + "] Received: " + message);
  			if( WORKING != getState() ) {
  				log.error("[" + getTxID() + "] Received NukeExecuteIndMsgC when not in state WORKING.");
  				setState(ABORTED);
  			} else {
  				log.debug("[" + getTxID() + "] Changed status to: " + message.getStatus() + ".");
  				switch( message.getStatus() ) {
  					case ABORTED: {
  						log.error("[" + getTxID() + "] Task reports aborted state.");
  						setState(ABORTED);
  						break;
  					}
  					case DONE: {
  						log.debug("[" + getTxID() + "] Task reports done state.");
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

}
