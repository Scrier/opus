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

import java.util.concurrent.TimeUnit;

import io.github.scrier.opus.common.commander.BaseProcedureC;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.exception.InvalidOperationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseDukeProcedure extends BaseProcedureC {

	private static Logger log = LogManager.getLogger(BaseDukeProcedure.class);

	protected Context theContext;

	public final int ABORTED = 0;
	public final int CREATED = 1;
	public final int COMPLETED = 9999;

	public BaseDukeProcedure() {
		log.trace("BaseProcedure()");
		this.theContext = Context.INSTANCE;
		super.setTxID(theContext.getNextTxID());;
	}

	public long getUniqueID() {
		return theContext.getUniqueID();
	}

	public void addEntry(BaseDataC data) {
		theContext.addEntry(data);
	}

	public boolean updateEntry(BaseDataC data) {
		return theContext.updateEntry(data);
	}

	public boolean removeEntry(BaseDataC data) {
		return theContext.removeEntry(data);
	}

	public boolean registerProcedure(BaseDukeProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		return theContext.registerProcedure(procedure);
	}

	public long getIdentity() {
		try {
			return theContext.getIdentity();
		} catch (InvalidOperationException e) {
			log.error("Threw InvalidOperationException when calling Context.getIdentity.", e);
		}
		return -1;
	}

	/**
	 * Method to get a specified setting connected to a key.
	 * @param key String with the key to look for.
	 * @return String
	 * @throws InvalidOperationException if not initialized correctly.
	 */
	public String getSetting(String key) throws InvalidOperationException {
		return theContext.getSetting(key);
	}

	/**
	 * Method to start a timeout in the service.
	 * @param time int with the time in seconds.
	 * @param id long with unique id to get returned.
	 * @param callback ITimeOutCallback interface to call.
	 */
	public void startTimeout(int time, long id, ITimeOutCallback callback) {
		log.trace("startTimeout(" + time + ", " + id + ", " + callback + ")");
		theContext.startTimeout(time, id, callback);
	}

	/**
	 * Method to start a timeout in the service.
	 * @param time int with the time in the specified format.
	 * @param id long with unique id to get returned.
	 * @param callback ITimeOutCallback interface to call.
	 * @param timeUnit TimeUnit format to schedule timeout in.
	 */
	public void startTimeout(int time, long id, ITimeOutCallback callback, TimeUnit timeUnit) {
		log.trace("startTimeout(" + time + ", " + id + ", " + callback + ", " + timeUnit + ")");
		theContext.startTimeout(time, id, callback, timeUnit);
	}

	/**
	 * Method to check if a specific timeout is active.
	 * @param id long with the id of the timeout to check for.
	 * @return boolean
	 */
	public boolean isTimeoutActive(long id) {
		log.trace("isTimeoutActive(" + id + ")");
		return theContext.isTimeoutActive(id);
	}

	/**
	 * @return the commander
	 */
	public DukeCommander getCommander() {
		return theContext.getCommander();
	}

}
