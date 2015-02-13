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

import io.github.scrier.opus.common.aoc.BaseDataC;

public class DukeCommand extends BaseDataC {
	
	private static Logger log = LogManager.getLogger(DukeCommand.class);
	
	private DukeCommandEnum dukeCommand;
	private String response;

	/**
	 * Constructor
	 */
	public DukeCommand() {
		super(DukeFactory.FACTORY_ID, DukeFactory.DUKE_INFO);
		log.trace("DukeCommand()");
		setDukeCommand(DukeCommandEnum.UNDEFINED);
		setResponse("");
	}
	
	/**
	 * Copy constructor
	 * @param obj2copy DukeCommand object
	 */
	public DukeCommand(DukeCommand obj2copy) {
		super(obj2copy);
		log.trace("DukeCommand(" + obj2copy + ")");
		setDukeCommand(obj2copy.getDukeCommand());
		setResponse(obj2copy.getResponse());
	}
	
	/**
	 * Cast constructor
	 * @param input BaseNukeC object
	 * @throws ClassCastException if provided with a mismatching class.
	 */
	public DukeCommand(BaseDataC input) throws ClassCastException {
		super(input);
		log.trace("DukeCommand(" + input + ")");
		if( input instanceof DukeCommand ) {
			DukeCommand obj2copy = (DukeCommand)input;
			setDukeCommand(obj2copy.getDukeCommand());
			setResponse(obj2copy.getResponse());
		} else {
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof DukeCommand[" + DukeFactory.DUKE_INFO + "], are you using correct class?");
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
		setResponse(in.readUTF());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		super.writeData(out);
		out.writeUTF(getDukeCommand().toString());
		out.writeUTF(getResponse());
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

	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(String response) {
		this.response = response;
	}
	
}
