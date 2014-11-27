package io.github.scrier.opus.nuke.task;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.commander.BaseProcedureC;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.nuke.process.ProcessHandler;

public abstract class BaseTaskProcedure extends BaseProcedureC {

	private static Logger log = LogManager.getLogger(BaseTaskProcedure.class);

	private Context theContext;
	private ProcessHandler processHandler;
	private Process process;

	public BaseTaskProcedure() {
		log.trace("BaseTaskProcedure");
		theContext = Context.INSTANCE;
		setTxID(theContext.getNextTxID());
		setProcessHandler(null);
		setProcess(null);
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
				gobbler = new StreamGobblerToNull(getProcess().getInputStream());
			} else {
				gobbler.setInputStream(getProcess().getInputStream());
			}
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
	 * @return the execute
	 */
	private ProcessHandler getProcessHandler() {
		return processHandler;
	}

	/**
	 * @param execute the execute to set
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
	 * @return
	 */
	protected ThreadPoolExecutor getExecutor() {
		return theContext.getExecutor();
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
		return theContext.getTask().getNukeInfo();
	}
	
	public boolean updateEntry(BaseNukeC data) {
		return theContext.updateEntry(data);
	}

}
