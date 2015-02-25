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

import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.duke.DukeCommandReqMsgC;
import io.github.scrier.opus.common.duke.DukeCommandEnum;
import io.github.scrier.opus.common.duke.DukeCommandRspMsgC;
import io.github.scrier.opus.common.duke.DukeInfo;
import io.github.scrier.opus.common.duke.DukeMsgFactory;
import io.github.scrier.opus.common.message.BaseMsgC;

/**
 * Method to handle termination of another duke if duplicates are found.
 * @author andreas.joelsson
 */
public class TerminateDukeProcedure extends BaseDukeProcedure implements ITimeOutCallback {
	
	private static Logger log = LogManager.getLogger(TerminateDukeProcedure.class);
	
	private long id;
	private IProcedureWait callback;
	private DukeInfo duke;
	
	private final int statusTimeout = 5;		// Time in seconds that duke is allowed to respond in for a status message.
	private final int stopTimeout = 5;			// Time in seconds that duke is allowed to respond in for a stop message.
	private final int terminateTimeout = 5;	// Time in seconds that duke is allowed to respond in for a terminate message.
	private final int removeTimeout = 5;		// Time in seconds that duke is allowed to respond in for a remove of the other duke.
	
	protected final long STATUS_TIMEOUT_ID    = 456L;
	protected final long STOP_TIMEOUT_ID      = 457L;
	protected final long TERMINATE_TIMEOUT_ID = 458L;
	protected final long REMOVE_TIMEOUT_ID    = 459L;
	
	public final int WAITING_FOR_STATUS    = CREATED + 1;
	public final int WAITING_FOR_STOP      = CREATED + 2;
	public final int WAITING_FOR_TERMINATE = CREATED + 3;
	public final int WAITING_FOR_REMOVE    = CREATED + 4;
	
	private State[] states;

	/**
	 * Constructor
	 * @param identity
	 * @param callback
	 */
	public TerminateDukeProcedure(long identity, IProcedureWait callback, DukeInfo otherDuke) {
		log.trace("TerminateDukeProcedure(" + identity + ", " + callback + ", " +  otherDuke + ")");
		this.id = identity;
		this.callback = callback;
		this.duke = otherDuke;
		this.states = new State[WAITING_FOR_TERMINATE + 1];
		this.states[ABORTED] = new Aborted();
		this.states[CREATED] = new Created();
		this.states[WAITING_FOR_STATUS] = new WaitingForStatus();
		this.states[WAITING_FOR_STOP] = new WaitingForStop();
		this.states[WAITING_FOR_TERMINATE] = new WaitingForTerminate();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		DukeCommandReqMsgC command = new DukeCommandReqMsgC(getSendIF());
		command.setDukeCommand(DukeCommandEnum.STATUS);
		command.setSource(getIdentity());
		command.setDestination(duke.getKey());
		command.setTxID(getTxID());
		command.send();
		startTimeout(statusTimeout, STATUS_TIMEOUT_ID, this);
		setState(WAITING_FOR_STATUS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() throws Exception {
		callback.procedureFinished(id, getState());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnUpdated(BaseDataC value) {
	// do nothing
		return getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnEvicted(BaseDataC value) {
	// do nothing
		return getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnRemoved(Long key) {
		log.trace("handleOnRemoved(" + key + ")");
		if( duke.getKey() == key ) {
			log.info("Duke is removed, we are done.");
			setState(COMPLETED);
		}
		return getState();
	}

  /**
   * {@inheritDoc}
   */
	@Override
  public int handleInMessage(BaseMsgC message) {
		log.trace("handleInMessage(" + message + ")");
		switch( message.getId() ) {
			case DukeMsgFactory.DUKE_COMMAND_RSP: {
				DukeCommandRspMsgC command = new DukeCommandRspMsgC(message);
				handleMessage(command);
				break;
			}
		}
		return getState();
  }
	
	/**
	 * Method to handle the DukeCommand message
	 * @param message DukeCommandMsgC message.
	 */
	protected void handleMessage(DukeCommandRspMsgC message) {
		log.trace("handleMessage(" + message + ")");
		try {
			log.debug("states[" + states[getState()].getClass().getSimpleName() + "].handleMessage(" + message + ");");
			states[getState()].handleMessage(message);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed().handleMessage(message);
			} else {
				log.error("Received out of bound exception in state: " + getState() + ".", e);
				setState(ABORTED);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public void timeOutTriggered(long id) {
	  log.trace("timeOutTriggered(" + id + ")");
		try {
			log.debug("states[" + states[getState()].getClass().getSimpleName() + "].handleTimeOut(" + id + ");");
			states[getState()].handleTimeOut(id);
		} catch ( ArrayIndexOutOfBoundsException e ) {
			if( COMPLETED == getState() ) {
				new Completed().handleTimeOut(id);
			} else {
				log.error("Received out of bound exception in state: " + getState() + ".", e);
				setState(ABORTED);
			}
		}
  }
	
	/**
	 * Exposed method to subclasses to start timeouts.
	 * @param time int with the time in seconds.
	 * @param id that the timeout should have.
	 */
	public void startTimeout(int time, long id) {
		log.trace("startTimeout(" + time + ", " + id + ")");
		startTimeout(time,  id, this);
	}
	
	/**
	 * Method to initialize the terminate command.
	 */
	private void sendTerminateCommand() {
		log.trace("sendTerminateCommand()");
		DukeCommandReqMsgC pDukeCommandReq = new DukeCommandReqMsgC(getSendIF());
		pDukeCommandReq.setSource(getIdentity());
		pDukeCommandReq.setDestination(duke.getKey());
		pDukeCommandReq.setTxID(getTxID());
		pDukeCommandReq.setDukeCommand(DukeCommandEnum.TERMINATE);
		pDukeCommandReq.send();
		setState(WAITING_FOR_TERMINATE);
		startTimeout(terminateTimeout, TERMINATE_TIMEOUT_ID, this);
	}
	
	/**
	 * Base state for the FSM logic.
	 */
	public class State {
		
		protected Logger logLocal;
		
		public State() {
			logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.TerminateDukeProcedure.State");
		}
		
		/**
		 * Method to handle the DukeCommandMsgC in a specified state.
		 * @param message DukeCommandMsgC message to handle
		 */
		public void handleMessage(DukeCommandRspMsgC message) {
			logLocal.trace("handleMessage(" + message + ")");
			logLocal.error("Called default state when in state " + getState() + ", cannot continue.");
			setState(ABORTED);
		}
		
		/**
		 * Method to handle a timeout in a given state.
		 * @param id long identifying the unique id for the timeout.
		 */
		public void handleTimeOut(long id) {
			logLocal.trace("handleTimeOut(" + id + ")");
			logLocal.error("Called default state when in state " + getState() + ", cannot continue.");
			setState(ABORTED);
		}
		
	}
	
	/**
	 * Empty state for not handled information.
	 */
	public class Aborted extends State {
		
		public Aborted() {
			logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.TerminateDukeProcedure.Aborted");
		}
		
	}
	
	/**
	 * Empty state for not handled information.
	 */
	public class Created extends State {
		
		public Created() {
			logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.TerminateDukeProcedure.Created");
		}
		
	}
	
	/**
	 * State handling the Waiting For Status message.
	 */
	public class WaitingForStatus extends State {
		
		public WaitingForStatus() {
			logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.TerminateDukeProcedure.WaitingForStatus");
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleMessage(DukeCommandRspMsgC message) {
			logLocal.trace("handleMessage(" + message + ")");
			if( getTxID() == message.getTxID() ) {
				log.info("Old duke is reporting the following information: \"" + message.getResponse() + "\", sending stop command.");
				terminateTimeout(STATUS_TIMEOUT_ID);
				DukeCommandReqMsgC pDukeCommand = new DukeCommandReqMsgC(getSendIF());
				pDukeCommand.setSource(getIdentity());
				pDukeCommand.setDestination(duke.getKey());
				pDukeCommand.setTxID(getTxID());
				pDukeCommand.setDukeCommand(DukeCommandEnum.STOP);
				pDukeCommand.send();
				startTimeout(stopTimeout, STOP_TIMEOUT_ID);
				setState(WAITING_FOR_STOP);
			}
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleTimeOut(long id) {
			logLocal.trace("handleTimeOut(" + id + ")");
			if( STATUS_TIMEOUT_ID == id ) {
				log.info("Timed out while waiting for the status feedback from duke with id: " + duke.getKey() + ", sending terminate command.");
				sendTerminateCommand();
			}
		}
		
	}
	
	/**
	 * State handling the Waiting For Status message.
	 */
	public class WaitingForStop extends State {
		
		public WaitingForStop() {
			logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.TerminateDukeProcedure.WaitingForStop");
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleMessage(DukeCommandRspMsgC message) {
			logLocal.trace("handleMessage(" + message + ")");
			if( getTxID() == message.getTxID() ) {
				log.info("Old duke is reporting the following information from stop command: \"" + message.getResponse() + "\", waiting for removal from map.");
				terminateTimeout(STOP_TIMEOUT_ID);
				setState(WAITING_FOR_REMOVE);
				startTimeout(removeTimeout, REMOVE_TIMEOUT_ID);
			}
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleTimeOut(long id) {
			logLocal.trace("handleTimeOut(" + id + ")");
			if( STOP_TIMEOUT_ID == id ) {
				log.info("Timed out while waiting for the stop feedback from duke with id: " + duke.getKey() + ", sending terminate command.");
				sendTerminateCommand();
			}
		}
		
	}
	
	/**
	 * State handling the Waiting For Status message.
	 */
	public class WaitingForTerminate extends State {
		
		public WaitingForTerminate() {
			logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.TerminateDukeProcedure.WaitingForTerminate");
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleMessage(DukeCommandRspMsgC message) {
			logLocal.trace("handleMessage(" + message + ")");
			if( getTxID() == message.getTxID() ) {
				log.info("Old duke is reporting the following information from terminate command: \"" + message.getResponse() + "\", waiting for removal from map.");
				terminateTimeout(TERMINATE_TIMEOUT_ID);
				setState(WAITING_FOR_REMOVE);
				startTimeout(removeTimeout, REMOVE_TIMEOUT_ID);
			}
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handleTimeOut(long id) {
			logLocal.trace("handleTimeOut(" + id + ")");
			if( TERMINATE_TIMEOUT_ID == id ) {
				log.info("Timed out while waiting for the terminutae feedback from duke with id: " + duke.getKey() + ", cannot continue.");
				setState(ABORTED);
			}
		}
		
	}
	
	/**
	 * Empty state for not handled information.
	 */
	public class Completed extends State {
		
		public Completed() {
			logLocal = LogManager.getLogger("io.github.scrier.opus.duke.commander.TerminateDukeProcedure.Completed");
		}
		
	}
	
}
