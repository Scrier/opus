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
package io.github.scrier.opus.common.nuke;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.message.SendIF;

public class NukeTerminateAllReqMsgC extends BaseMsgC {
	
	private static Logger log = LogManager.getLogger(NukeTerminateAllReqMsgC.class);
	
	/**
	 * Constructor
	 */
	public NukeTerminateAllReqMsgC() {
		super(NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_TERMINATE_ALL_REQ);
		log.trace("NukeTerminateAllReqMsgC()");
	}

	/**
	 * Constructor
	 */
	public NukeTerminateAllReqMsgC(SendIF sendIF) {
		super(NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_TERMINATE_ALL_REQ, sendIF);
		log.trace("NukeTerminateAllReqMsgC(" + sendIF + ")");
	}
	
	/**
	 * Copy constructor
	 * @param obj2copy DukeCommand object
	 */
	public NukeTerminateAllReqMsgC(NukeTerminateAllReqMsgC obj2copy) {
		super(obj2copy);
		log.trace("NukeTerminateAllReqMsgC(" + obj2copy + ")");
	}
	
	/**
	 * Cast constructor
	 * @param input BaseNukeC object
	 * @throws ClassCastException if provided with a mismatching class.
	 */
	public NukeTerminateAllReqMsgC(BaseMsgC input) throws ClassCastException {
		super(input);
		log.trace("NukeTerminateAllReqMsgC(" + input + ")");
		if( input instanceof NukeTerminateAllReqMsgC ) {
		} else {
			// no parameters to work with.
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof NukeTerminateAllReqMsgC[" + NukeMsgFactory.NUKE_TERMINATE_ALL_REQ + "], are you using correct class?");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		log.trace("readData(" + in + ")");
		super.readData(in);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		super.writeData(out);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "NukeTerminateAllReqMsgC - " + super.toString();
	}

}
