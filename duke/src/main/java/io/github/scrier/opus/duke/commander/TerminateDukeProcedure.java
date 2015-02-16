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
import io.github.scrier.opus.common.duke.DukeCommandMsgC;
import io.github.scrier.opus.common.duke.DukeCommandEnum;
import io.github.scrier.opus.common.duke.DukeInfo;
import io.github.scrier.opus.common.duke.DukeMsgFactory;
import io.github.scrier.opus.common.message.BaseMsgC;

public class TerminateDukeProcedure extends BaseDukeProcedure {
	
	private static Logger log = LogManager.getLogger(TerminateDukeProcedure.class);
	
	private long id;
	private IProcedureWait callback;
	private DukeInfo duke;
	
	public final int WAITING_FOR_STATUS = CREATED + 1;
	
	private State[] states = { new Aborted(), new Created(), new WaitingForStatus() };

	/**
	 * Constructor
	 * @param identity
	 * @param callback
	 */
	public TerminateDukeProcedure(long identity, IProcedureWait callback, DukeInfo otherDuke) {
		this.id = identity;
		this.callback = callback;
		this.duke = otherDuke;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		DukeCommandMsgC command = new DukeCommandMsgC(getSendIF());
		command.setDukeCommand(DukeCommandEnum.STATUS);
		command.setSource(getIdentity());
		command.setDestination(duke.getKey());
		command.setTxID(getTxID());
		command.send();
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
		// do nothing
		return getState();
	}

  /**
   * {@inheritDoc}
   */
	@Override
  public int handleMessage(BaseMsgC message) {
		log.trace("handleMessage(" + message + ")");
		if( DukeMsgFactory.FACTORY_ID == message.getFactoryId() ) {
		  switch( message.getId() ) {
		  	case DukeMsgFactory.DUKE_COMMAND: {
		  		DukeCommandMsgC command = new DukeCommandMsgC(message);
		  		handleMessage(command);
		  		break;
		  	}
		  }
		}
	  return getState();
  }
	
	/**
	 * Method to handle the DukeCommand message
	 * @param message DukeCommandMsgC message.
	 */
	protected void handleMessage(DukeCommandMsgC message) {
		log.trace("handleMessage(" + message + ")");
		states[getState()].handleMessage(message);
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
		public void handleMessage(DukeCommandMsgC message) {
			logLocal.trace("handleMessage(" + message + ")");
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
		public void handleMessage(DukeCommandMsgC message) {
			logLocal.trace("handleMessage(" + message + ")");
			
		}
		
	}
	
}
