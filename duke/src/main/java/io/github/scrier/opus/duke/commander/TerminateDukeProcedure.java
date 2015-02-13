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

import io.github.scrier.opus.common.aoc.BaseDataC;
import io.github.scrier.opus.common.duke.DukeCommand;
import io.github.scrier.opus.common.duke.DukeCommandEnum;
import io.github.scrier.opus.common.duke.DukeInfo;

public class TerminateDukeProcedure extends BaseDukeProcedure {
	
	private long id;
	private IProcedureWait callback;
	private DukeInfo duke;

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
		DukeCommand command = new DukeCommand();
		command.setDukeCommand(DukeCommandEnum.STATUS);
		command.setKey(duke.getKey());
		command.setTxID(getTxID());
		theContext.addEntry(command);
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
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnEvicted(BaseDataC value) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnRemoved(Long key) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
