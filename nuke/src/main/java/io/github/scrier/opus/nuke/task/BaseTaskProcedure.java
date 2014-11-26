package io.github.scrier.opus.nuke.task;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.commander.BaseProcedureC;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.nuke.process.ProcessHandler;

public abstract class BaseTaskProcedure extends BaseProcedureC {

	private static Logger log = LogManager.getLogger(BaseTaskProcedure.class);

	private Context theContext;
	private ProcessHandler processHandler;

	public BaseTaskProcedure() {
		log.trace("BaseTaskProcedure");
		theContext = Context.INSTANCE;
		setTxID(theContext.getNextTxID());
		setProcessHandler(null);
	}

	public boolean executeProcess(String executeString, File directory, StreamGobbler gobbler) {
		boolean retValue = true;
		setProcessHandler(new ProcessHandler(executeString.split(" ")));
		if( null != directory ) {
			getProcessHandler().directory(directory);
		}
		getProcessHandler().redirectErrorStream(true);
		Process process = null;
		try {
			process = getProcessHandler().start();
			if ( null == gobbler ) {
				gobbler = new StreamGobblerToNull(process.getInputStream());
			} else {
				gobbler.setInputStream(process.getInputStream());
			}
			int retCode = process.waitFor();
			if( 0 != retCode ) {
				log.error("Receoved returncode: " + retCode);
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
	
	protected ThreadPoolExecutor getExecutor() {
		return theContext.getExecutor();
	}

	/**
	 * Method to access the information for this node.
	 * @return NukeInfo object.
	 */
	public NukeInfo getNukeInfo() {
		return theContext.getTask().getNukeInfo();
	}

}
