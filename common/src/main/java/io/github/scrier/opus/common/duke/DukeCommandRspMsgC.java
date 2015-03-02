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

import javafx.scene.Parent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.message.SendIF;

public class DukeCommandRspMsgC extends BaseMsgC {
	
	private static Logger log = LogManager.getLogger(DukeCommandRspMsgC.class);
	
	private String response;
	
	/**
	 * Constructor
	 */
	public DukeCommandRspMsgC() {
		super(DukeMsgFactory.FACTORY_ID, DukeMsgFactory.DUKE_COMMAND_RSP);
		log.trace("DukeCommandRspMsgC()");
		setResponse("");
	}

	/**
	 * Constructor
	 */
	public DukeCommandRspMsgC(SendIF sendIF) {
		super(DukeMsgFactory.FACTORY_ID, DukeMsgFactory.DUKE_COMMAND_RSP, sendIF);
		log.trace("DukeCommandRspMsgC(" + sendIF + ")");
		setResponse("");
	}
	
	/**
	 * Copy constructor
	 * @param obj2copy DukeCommand object
	 */
	public DukeCommandRspMsgC(DukeCommandRspMsgC obj2copy) {
		super(obj2copy);
		log.trace("DukeCommandRspMsgC(" + obj2copy + ")");
		setResponse(obj2copy.getResponse());
	}
	
	/**
	 * Cast constructor
	 * @param input BaseNukeC object
	 * @throws ClassCastException if provided with a mismatching class.
	 */
	public DukeCommandRspMsgC(BaseMsgC input) throws ClassCastException {
		super(input);
		log.trace("DukeCommandRspMsgC(" + input + ")");
		if( input instanceof DukeCommandRspMsgC ) {
			DukeCommandRspMsgC obj2copy = (DukeCommandRspMsgC)input;
			setResponse(obj2copy.getResponse());
		} else {
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof DukeCommandRspMsgC[" + DukeMsgFactory.DUKE_COMMAND_RSP + "], are you using correct class?");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		log.trace("readData(" + in + ")");
		super.readData(in);
		setResponse(in.readUTF());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		super.writeData(out);
		out.writeUTF(getResponse());
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
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "DukeCommandRspMsgC{response: " + getResponse() + "} - " + super.toString();
	}
	
}
