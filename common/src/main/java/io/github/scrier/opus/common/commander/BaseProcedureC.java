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
package io.github.scrier.opus.common.commander;

import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.message.BaseMsgC;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseProcedureC {
	
	private static Logger log = LogManager.getLogger(BaseProcedureC.class);
	
	private int state;
	
	private int txID;
	
	public final int ABORTED = 0;
	public final int CREATED = 1;
	public final int COMPLETED = 9999;
	
	public BaseProcedureC() {
		log.trace("BaseProcedureC()");
		this.state = CREATED;
		this.txID = -1;
	}
	
	/**
	 * Method to initialize the procedure. Will be called directly after registering as a first step, do your initialization of the procedure here.
	 * {@code
	 * init() {
	 *   // initialize and throw potential exceptions that could happen when initializate this.
	 * }
	 * }
	 * @throws Exception thrown if init method fails to initialize.
	 */
	public abstract void init() throws Exception;
	
	/**
	 * Method to shutdown the procedures. Will be called as a last step when a procedure comes to either COMPLETED or ABORTED state. 
	 * {@code
	 * shutDown() {
	 *   // shutdown and throw potential exceptions that could happen when cleaning up.
	 * }
	 * }
	 * @throws Exception thrown if shutdown fails cleaning up.
	 */
	public abstract void shutDown() throws Exception;

	/**
	 * Method to handle updates on the data map.
	 * @param value BaseDataC with the value of the object updated.
	 * @return int with the current state of the procedure.
	 */
	public abstract int handleOnUpdated(BaseDataC value);
	
	/**
	 * Method to handle if a data class gets evicted from the map due to shortage of memory or similar.
	 * @param value BaseDataC with the evicted information.
	 * @return int with the current state of the procedure.
	 */
	public abstract int handleOnEvicted(BaseDataC value);
	
	/**
	 * Method to handle a remove event of a data class. 
	 * @param key long with the key value of the map.
	 * @return int with the current state of the procedure.
	 */
	public abstract int handleOnRemoved(Long key);
	
	/**
	 * Method to handle a message to the procedure.
	 * @param message BaseMsgC to be handled for the procedure.
	 * @return int with the current state of the procedure.
	 */
	public abstract int handleInMessage(BaseMsgC message);
	
	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		// We cannot change a complete of aborted procedure.
		if( ABORTED != getState() && COMPLETED != getState() && this.state != state) {
			int previousState = this.state;
			this.state = state;
			onStateChanged(this.state, previousState);
		}
	}
	
	/**
	 * Method called when a state is changed.
	 * @param newState int with current state.
	 * @param previousState int with previous state.
	 */
	public void onStateChanged(int newState, int previousState) {
		log.trace("onStateChanged(" + newState + ", " + previousState + ")");
	}
	
	/**
	 * Method to check if the procedure has reached a finished state (COMPLETED or ABORTED).
	 * @return boolean
	 */
	public boolean isProcedureFinished() {
		return ( ABORTED == getState() ) || ( COMPLETED == getState() );
	}

	/**
	 * @return the txID
	 */
  public int getTxID() {
	  return txID;
  }
  
	/**
	 * Method to set txid
	 * @param txID int with txID to set.
	 */
  protected void setTxID(int txID) {
	  this.txID = txID;
  }
  
}
