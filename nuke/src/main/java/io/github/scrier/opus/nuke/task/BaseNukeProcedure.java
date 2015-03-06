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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import io.github.scrier.opus.common.commander.BaseProcedureC;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.message.SendIF;

public abstract class BaseNukeProcedure extends BaseProcedureC {

	public static Logger log = LogManager.getLogger(BaseNukeProcedure.class);

	private Context theContext;
	private long identity;

	/**
	 * Constructor
	 */
	public BaseNukeProcedure() {
		log.trace("BaseNukeProcedure()");
		theContext = Context.INSTANCE;
		setTxID(theContext.getNextTxID());
		try {
	    setIdentity(theContext.getIdentity());
    } catch (InvalidOperationException e) {
	    log.fatal("BaseNukeProcedure threw InvalidOperationException: ", e);
	    theContext.shutDown();
    }
	}

	/**
	 * Method to add an entry to the data map.
	 * @param data BaseDataC instance.
	 */
	protected void addEntry(BaseDataC data) {
		theContext.addEntry(data);
	}

	/**
	 * Method to update an existing entry with information.
	 * @param data BaseDataC instance
	 * @return boolean if an update was successful.
	 */
	public boolean updateEntry(BaseDataC data) {
		return theContext.updateEntry(data);
	}

	/**
	 * Method to add a new procedure to the task handler.
	 * @param procedure BaseNukeProcedure instance
	 */
	public void registerProcedure(BaseNukeProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		theContext.getTask().registerProcedure(procedure);
	}
	
	/**
	 * Method to access the context class.
	 * @return Context instance.
	 */
	public Context getContext() {
		return this.theContext;
	}
	
	/**
	 * Method to get a cluster unique id to use.
	 * @return long
	 */
	public long getUniqueID() {
		return this.theContext.getUniqueID();
	}
	
	/**
	 * Method to get the send interface for use with messages.
	 * @return SendIF to use for sending messgages.
	 */
	public SendIF getSendIF() {
		return theContext.getSendIF();
	}

	/**
	 * @return the identity
	 */
  public long getIdentity() {
	  return identity;
  }

	/**
	 * @param identity the identity to set
	 */
  private void setIdentity(long identity) {
	  this.identity = identity;
  }
	
}
