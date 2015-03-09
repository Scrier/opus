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

public class NukeStopReqMsgC extends BaseMsgC {
	
	private static Logger log = LogManager.getLogger(NukeStopReqMsgC.class);
	
	private long processID;
	
	/**
	 * Constructor
	 */
	public NukeStopReqMsgC() {
		super(NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_STOP_REQ);
		log.trace("NukeStopReqMsgC()");
		setProcessID(Constants.HC_UNDEFINED);
	}

	/**
	 * Constructor
	 */
	public NukeStopReqMsgC(SendIF sendIF) {
		super(NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_STOP_REQ, sendIF);
		log.trace("NukeStopReqMsgC(" + sendIF + ")");
		setProcessID(Constants.HC_UNDEFINED);
	}
	
	/**
	 * Copy constructor
	 * @param obj2copy DukeCommand object
	 */
	public NukeStopReqMsgC(NukeStopReqMsgC obj2copy) {
		super(obj2copy);
		log.trace("NukeStopReqMsgC(" + obj2copy + ")");
		setProcessID(obj2copy.getProcessID());
	}
	
	/**
	 * Cast constructor
	 * @param input BaseNukeC object
	 * @throws ClassCastException if provided with a mismatching class.
	 */
	public NukeStopReqMsgC(BaseMsgC input) throws ClassCastException {
		super(input);
		log.trace("NukeStopReqMsgC(" + input + ")");
		if( input instanceof NukeStopReqMsgC ) {
			NukeStopReqMsgC obj2copy = (NukeStopReqMsgC)input;
			setProcessID(obj2copy.getProcessID());
		} else {
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof NukeStopReqMsgC[" + NukeMsgFactory.NUKE_STOP_REQ + "], are you using correct class?");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		log.trace("readData(" + in + ")");
		super.readData(in);
		setProcessID(in.readLong());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		super.writeData(out);
		out.writeLong(getProcessID());
	}

	/**
	 * @return the processID
	 */
  public long getProcessID() {
	  return processID;
  }

	/**
	 * @param processID the processID to set
	 */
  public void setProcessID(long processID) {
	  this.processID = processID;
  }
  
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "NukeStopReqMsgC{processID: " + getProcessID() + "} - " + super.toString();
	}

}
