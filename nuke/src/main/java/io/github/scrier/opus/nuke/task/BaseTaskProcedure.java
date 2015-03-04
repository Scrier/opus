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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeExecuteIndMsgC;
import io.github.scrier.opus.common.nuke.NukeExecuteReqMsgC;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.nuke.process.ProcessHandler;

public abstract class BaseTaskProcedure extends BaseNukeProcedure {

	private static Logger log = LogManager.getLogger(BaseTaskProcedure.class);

	private ProcessHandler processHandler;
	private Process process;
	private String command;
	private String folder;
	private int msgTxID;
	private CommandState currentCommandState;

	public BaseTaskProcedure() {
		log.trace("BaseTaskProcedure");
		setProcessHandler(null);
		setProcess(null);
		setCommand("");
		setFolder("");
		setMsgTxID(-1);
		setCurrentCommandState(CommandState.UNDEFINED);
	}
	
	public BaseTaskProcedure(NukeExecuteReqMsgC message) {
		log.trace("BaseTaskProcedure");
		setProcessHandler(null);
		setProcess(null);
		setCommand(message.getCommand());
		setFolder(message.getFolder());
		setMsgTxID(message.getTxID());
		setCurrentCommandState(CommandState.UNDEFINED);
	}

	/**
	 * Method to execute a process.
	 * @param executeString String to process.
	 * @param directory File optional of where to execute command.
	 * @param gobbler StreamGobbler optional for handling process output. 
	 * @return boolean
	 */
	public boolean executeProcess(String executeString, File directory, StreamGobbler gobbler) {
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
			if( 0 != retCode ) {
				log.error("Received returncode: " + retCode);
				retValue = false;
			}
		} catch ( IOException e ) {
			log.error("IOException when starting process.", e);
			retValue = false;
		} catch ( InterruptedException e ) {
			log.error("InterruptedException received when waiting for process.", e);
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
   * 
   * @param newState
   */
  protected void sendCommandStateUpdate(CommandState newState) {
  	if( newState != getCurrentCommandState() ) {
  		NukeExecuteIndMsgC pNukeExecuteInd = new NukeExecuteIndMsgC(getSendIF());
  		pNukeExecuteInd.setSource(getIdentity());
  		pNukeExecuteInd.setDestination(Constants.MSG_TO_ALL);
  		pNukeExecuteInd.setTxID(getTxID());
  		pNukeExecuteInd.setStatus(newState);
  		pNukeExecuteInd.send();
  		setCurrentCommandState(newState);
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
	
}
