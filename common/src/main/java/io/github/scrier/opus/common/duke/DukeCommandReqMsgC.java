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
package io.github.scrier.opus.common.duke;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.message.SendIF;

public class DukeCommandReqMsgC extends BaseMsgC {
	
	private static Logger log = LogManager.getLogger(DukeCommandReqMsgC.class);
	
	private DukeCommandEnum dukeCommand;
	
	/**
	 * Constructor
	 */
	public DukeCommandReqMsgC() {
		super(DukeMsgFactory.FACTORY_ID, DukeMsgFactory.DUKE_COMMAND_REQ);
		log.trace("DukeCommandReqMsgC()");
		setDukeCommand(DukeCommandEnum.UNDEFINED);
	}

	/**
	 * Constructor
	 * @param sendIF the SendIF to use for distribution
	 */
	public DukeCommandReqMsgC(SendIF sendIF) {
		super(DukeMsgFactory.FACTORY_ID, DukeMsgFactory.DUKE_COMMAND_REQ, sendIF);
		log.trace("DukeCommandReqMsgC(" + sendIF + ")");
		setDukeCommand(DukeCommandEnum.UNDEFINED);
	}
	
	/**
	 * Copy constructor
	 * @param obj2copy DukeCommand object
	 */
	public DukeCommandReqMsgC(DukeCommandReqMsgC obj2copy) {
		super(obj2copy);
		log.trace("DukeCommandReqMsgC(" + obj2copy + ")");
		setDukeCommand(obj2copy.getDukeCommand());
	}
	
	/**
	 * Cast constructor
	 * @param input BaseNukeC object
	 * @throws ClassCastException if provided with a mismatching class.
	 */
	public DukeCommandReqMsgC(BaseMsgC input) throws ClassCastException {
		super(input);
		log.trace("DukeCommandReqMsgC(" + input + ")");
		if( input instanceof DukeCommandReqMsgC ) {
			DukeCommandReqMsgC obj2copy = (DukeCommandReqMsgC)input;
			setDukeCommand(obj2copy.getDukeCommand());
		} else {
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof DukeCommandReqMsgC[" + DukeMsgFactory.DUKE_COMMAND_REQ + "], are you using correct class?");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		log.trace("readData(" + in + ")");
		super.readData(in);
		setDukeCommand(DukeCommandEnum.valueOf(in.readUTF()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		super.writeData(out);
		out.writeUTF(getDukeCommand().toString());
	}

	/**
	 * @return the dukeCommand
	 */
	public DukeCommandEnum getDukeCommand() {
		return dukeCommand;
	}

	/**
	 * @param dukeCommand the dukeCommand to set
	 */
	public void setDukeCommand(DukeCommandEnum dukeCommand) {
		this.dukeCommand = dukeCommand;
	}

}
