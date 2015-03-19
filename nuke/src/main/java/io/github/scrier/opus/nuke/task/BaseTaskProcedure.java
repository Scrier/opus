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
package io.github.scrier.opus.nuke.task;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeExecuteIndMsgC;
import io.github.scrier.opus.common.nuke.NukeExecuteReqMsgC;
import io.github.scrier.opus.common.nuke.NukeExecuteRspMsgC;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.common.nuke.NukeStopAllReqMsgC;
import io.github.scrier.opus.common.nuke.NukeStopAllRspMsgC;
import io.github.scrier.opus.common.nuke.NukeTerminateAllReqMsgC;
import io.github.scrier.opus.common.nuke.NukeTerminateAllRspMsgC;
import io.github.scrier.opus.nuke.process.ProcessHandler;

public abstract class BaseTaskProcedure extends BaseNukeProcedure {

	private static Logger log = LogManager.getLogger(BaseTaskProcedure.class);

	private ProcessHandler processHandler;
	private Process process;
	private String command;
	private String folder;
	private int msgTxID;
	private CommandState currentCommandState;
	private String errorMessage;
	private long source;
	private long sagaID;
	private long processID;
	private boolean repeated;
//	static final AtomicLong NEXT_ID = new AtomicLong(0);
//  protected final long id = NEXT_ID.getAndIncrement();

	public BaseTaskProcedure() {
		log.trace("BaseTaskProcedure");
		setProcessHandler(null);
		setProcess(null);
		setCommand("");
		setFolder("");
		setMsgTxID(-1);
		setSource(Constants.HC_UNDEFINED);
		setCurrentCommandState(CommandState.UNDEFINED);
		setSagaID(Constants.HC_UNDEFINED);
		setProcessID(Constants.HC_UNDEFINED);
	}
	
	public BaseTaskProcedure(NukeExecuteReqMsgC message) {
		log.trace("BaseTaskProcedure");
		setProcessHandler(null);
		setProcess(null);
		setCommand(message.getCommand());
		setFolder(message.getFolder());
		setMsgTxID(message.getTxID());
		setSource(message.getSource());
		setSagaID(message.getSagaID());
		setCurrentCommandState(CommandState.UNDEFINED);
	}
	
	public void cleanUp() {
		log.trace("cleanUp()");
		log.debug("Process: " + getProcess() + ", processHandler: " + getProcessHandler());
		setRepeated(false);
		if( null != getProcess() ) {
			while( getProcess().isAlive() ) {
				log.info("Terminating child process.");
				getProcess().destroy();
				try {
	        log.info("With return code: " + getProcess().waitFor(100, TimeUnit.MILLISECONDS) + ".");
        } catch (InterruptedException e) {
	        log.fatal("Received InterruptedException from waitFor killing process.", e);
        }
			}
		}
	}

	/**
	 * Method to execute a process.
	 * @param executeString String to process.
	 * @param directory File optional of where to execute command.
	 * @param gobbler StreamGobbler optional for handling process output. 
	 * @return boolean
	 */
	public synchronized boolean executeProcess(String executeString, File directory, StreamGobbler gobbler) {
		log.trace("executeProcess(" + executeString + ", " + directory + ", " + gobbler + ")");
		boolean retValue = true;
		setProcessHandler(new ProcessHandler(executeString.split(" ")));
		if( null != directory ) {
			getProcessHandler().directory(directory);
		}
		getProcessHandler().redirectErrorStream(true);
		setProcess(null);
		try {
			setProcess(getProcessHandler().start());
			if ( null == gobbler ) {
				log.debug("No gobbler defined, creating new one!");
				gobbler = new StreamGobblerToNull(getProcess().getInputStream());
			} else {
				log.debug("Setting input stream to gobbler: " + gobbler);
				gobbler.setInputStream(getProcess().getInputStream());
			}
			gobbler.start();
			int retCode = getProcess().waitFor();
			if( getProcess().isAlive() ) {
				log.error("Process still alive, although ret code returned.");
			}
			log.info("Received returncode: " + retCode);
			if( 0 != retCode ) {
				log.error("Received returncode: " + retCode);
				setErrorMessage("Command: " + getCommand() + ", on node " + getIdentity() + ", received return code: " + retCode + ".");
				retValue = false;
			}
		} catch ( IOException e ) {
			log.error("IOException when starting process.", e);
			setErrorMessage("Command: " + getCommand() + ", on node " + getIdentity() + ", received IOException: " + e.getMessage() + ".");
			retValue = false;
		} catch ( InterruptedException e ) {
			log.error("InterruptedException received when waiting for process.", e);
			setErrorMessage("Command: " + getCommand() + ", on node " + getIdentity() + ", received InterruptedException: " + e.getMessage() + ".");
			retValue = false;
		}
		return retValue;
	}

	/**
	 * @return ProcessHandler
	 */
	private ProcessHandler getProcessHandler() {
		return processHandler;
	}

	/**
	 * @param processHandler the ProcessHandler to set
	 */
	private void setProcessHandler(ProcessHandler processHandler) {
		this.processHandler = processHandler;
	}
	
	/**
	 * @return the process
	 */
	private Process getProcess() {
		return process;
	}

	/**
	 * @param process the process to set
	 */
	private void setProcess(Process process) {
		this.process = process;
	}

	/**
	 * Method to get the process started by executor.
	 * @return ThreadPoolExecutor to get threads from.
	 */
	protected ThreadPoolExecutor getExecutor() {
		return getContext().getExecutor();
	}
	
	protected void terminateProcess() {
		log.trace("terminateProcess()");
		if( null != getProcess() ) {
			getProcess().destroy();
		}
	}

	/**
	 * Method to access the information for this node.
	 * @return NukeInfo object.
	 */
	public NukeInfo getNukeInfo() {
		return getContext().getTask().getNukeInfo();
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
	 * @return the msgTxID
	 */
  public int getMsgTxID() {
	  return msgTxID;
  }

	/**
	 * @param msgTxID the msgTxID to set
	 */
  public void setMsgTxID(int msgTxID) {
	  this.msgTxID = msgTxID;
  }
  
  /**
   * Method to send a command indication update.
   * @param newState The state to change to.
   */
  protected void sendCommandStateUpdate(CommandState newState) {
  	sendCommandStateUpdate(newState, "");
  }
  
  /**
   * Method to send a command indication update.
   * @param newState The state to change to.
   * @param extraInformation message with reasoning behind a state change.
   */
  protected synchronized void sendCommandStateUpdate(CommandState newState, String extraInformation) {
  	log.trace("sendCommandStateUpdate(" + newState + ", \"" + extraInformation + "\")");
  	if( newState != getCurrentCommandState() ) {
  		setCurrentCommandState(newState);
  		NukeExecuteIndMsgC pNukeExecuteInd = new NukeExecuteIndMsgC(getSendIF());
  		pNukeExecuteInd.setSource(getIdentity());
  		pNukeExecuteInd.setDestination(Constants.MSG_TO_ALL);
  		pNukeExecuteInd.setTxID(getTxID());
  		pNukeExecuteInd.setProcessID(getProcessID());
  		pNukeExecuteInd.setStatus(newState);
  		pNukeExecuteInd.send();
  	}
  }
  
  /**
   * Method to handle the NukeStopReqMsgC message.
   * @param message NukeStopReqMsgC instance.
   */
  protected void handleMessage(NukeStopAllReqMsgC message) {
  	log.trace("handleMessage(" + message + ")");
  	if( getProcessID() != message.getProcessID() ) {
  		log.debug("NukeStopReqMsgC: " + message + ", not for us, expected: " + getProcessID() + " but was: " + message.getProcessID() + ".");
  	} else {
	  	NukeStopAllRspMsgC pNukeStopRsp = new NukeStopAllRspMsgC(getSendIF());
	  	pNukeStopRsp.setSource(getIdentity());
	  	pNukeStopRsp.setDestination(message.getSource());
	  	pNukeStopRsp.setTxID(message.getTxID());
	  	pNukeStopRsp.setSagaID(message.getSagaID());
	  	pNukeStopRsp.setProcessID(getProcessID());
	  	if( true != getProcess().isAlive() ) {
	  		pNukeStopRsp.setStatus("Process with id: " + getProcessID() + " is not alive.");
	  		pNukeStopRsp.setSuccess(false);
	  	} else {
	  		setRepeated(false);
	  		pNukeStopRsp.setStatus("Stopped repeat feature and waiting for soft stop.");
	  		pNukeStopRsp.setSuccess(true);
	  	}
	  	pNukeStopRsp.send();
  	}
  }
  
  /**
   * Method to handle the NukeTerminateReqMsgC message
   * @param message NukeTerminateReqMsgC instance.
   */
  protected void handleMessage(NukeTerminateAllReqMsgC message) {
  	log.trace("handleMessage(" + message + ")");
  	if( getProcessID() != message.getProcessID() ) {
  		log.debug("NukeTerminateReqMsgC: " + message + ", not for us, expected: " + getProcessID() + " but was: " + message.getProcessID() + ".");
  	} else {
  		log.info("Received command to terminate execution from: " + message.getSource() + ".");
  		NukeTerminateAllRspMsgC pNukeTerminateRsp = new NukeTerminateAllRspMsgC(getSendIF());
  		pNukeTerminateRsp.setSource(getIdentity());
  		pNukeTerminateRsp.setDestination(message.getSource());
  		pNukeTerminateRsp.setTxID(message.getTxID());
  		pNukeTerminateRsp.setSagaID(message.getSagaID());
  		pNukeTerminateRsp.setSuccess(true);
  		if( true != getProcess().isAlive() ) {
  			pNukeTerminateRsp.setStatus("Process " + getProcessID() + " is not alive, nothing to do.");
  		} else {
  			terminateProcess();
  			pNukeTerminateRsp.setStatus("Process " + getProcessID() + " has been given command to destroy process.");
  		}
  		pNukeTerminateRsp.send();
  	}
  }

	/**
	 * @return the currentCommandState
	 */
  private CommandState getCurrentCommandState() {
	  return currentCommandState;
  }

	/**
	 * @param currentCommandState the currentCommandState to set
	 */
  private void setCurrentCommandState(CommandState currentCommandState) {
	  this.currentCommandState = currentCommandState;
  }

	/**
	 * @return the errorMessage
	 */
  public String getErrorMessage() {
	  return errorMessage;
  }

	/**
	 * @param errorMessage the errorMessage to set
	 */
  public void setErrorMessage(String errorMessage) {
	  this.errorMessage = errorMessage;
  }

	/**
	 * @return the source
	 */
  public long getSource() {
	  return source;
  }

	/**
	 * @param source the source to set
	 */
  public void setSource(long source) {
	  this.source = source;
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
	 * @return the repeated
	 */
	protected boolean isRepeated() {
		return repeated;
	}

	/**
	 * @param repeated the repeated to set
	 */
	protected void setRepeated(boolean repeated) {
		this.repeated = repeated;
	}
  
	/**
	 * Method to send response to the requesting part.
	 */
	protected void sendResponse() {
		log.trace("sendResponse()");
		if( Constants.HC_UNDEFINED == getProcessID() ) {
			log.fatal("Process id is undefined. Cannot continue.");
			throw new RuntimeException("Process id in method BaseTaskProcedure.sendResponse is undefined. Cannot continue.");
		} else {
			NukeExecuteRspMsgC pNukeExecuteRsp = new NukeExecuteRspMsgC(getSendIF());
			pNukeExecuteRsp.setSource(getIdentity());
			pNukeExecuteRsp.setDestination(getSource());
			pNukeExecuteRsp.setTxID(getTxID());
			pNukeExecuteRsp.setProcessID(getProcessID());
			pNukeExecuteRsp.setSagaID(getSagaID());
			pNukeExecuteRsp.send();
		}
	}
	
}
