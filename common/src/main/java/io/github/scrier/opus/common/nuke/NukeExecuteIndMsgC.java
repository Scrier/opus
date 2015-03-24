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

public class NukeExecuteIndMsgC extends BaseMsgC { 

	private static Logger log = LogManager.getLogger(NukeExecuteIndMsgC.class);
	
	private CommandState status;
	private long processID;
	
	/**
	 * Constructor
	 */
	public NukeExecuteIndMsgC() {
		super(NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_EXECUTE_IND);
		log.trace("NukeExecuteIndMsgC()");
		setStatus(CommandState.UNDEFINED);
		setProcessID(Constants.HC_UNDEFINED);
	}

	/**
	 * Constructor
	 * @param sendIF the SendIF to use for distribution
	 */
	public NukeExecuteIndMsgC(SendIF sendIF) {
		super(NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_EXECUTE_IND, sendIF);
		log.trace("NukeExecuteIndMsgC(" + sendIF + ")");
		setStatus(CommandState.UNDEFINED);
		setProcessID(Constants.HC_UNDEFINED);
	}
	
	/**
	 * Copy constructor
	 * @param obj2copy DukeCommand object
	 */
	public NukeExecuteIndMsgC(NukeExecuteIndMsgC obj2copy) {
		super(obj2copy);
		log.trace("NukeExecuteIndMsgC(" + obj2copy + ")");
		setStatus(obj2copy.getStatus());
		setProcessID(obj2copy.getProcessID());
	}
	
	/**
	 * Cast constructor
	 * @param input BaseNukeC object
	 * @throws ClassCastException if provided with a mismatching class.
	 */
	public NukeExecuteIndMsgC(BaseMsgC input) throws ClassCastException {
		super(input);
		log.trace("NukeExecuteIndMsgC(" + input + ")");
		if( input instanceof NukeExecuteIndMsgC ) {
			NukeExecuteIndMsgC obj2copy = (NukeExecuteIndMsgC)input;
			setStatus(obj2copy.getStatus());
			setProcessID(obj2copy.getProcessID());
		} else {
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof NukeExecuteIndMsgC[" + NukeMsgFactory.NUKE_EXECUTE_IND + "], are you using correct class?");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		log.trace("readData(" + in + ")");
		super.readData(in);
		setStatus(CommandState.valueOf(in.readUTF()));
		setProcessID(in.readLong());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		super.writeData(out);
		out.writeUTF(getStatus().toString());
		out.writeLong(getProcessID());
	}

	/**
	 * @return the status
	 */
	public CommandState getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(CommandState status) {
		this.status = status;
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
		String retValue = "NukeExecuteIndMsgC{status: " + getStatus();
		retValue += ", processID: " + getProcessID() + "} - " + super.toString();
		return retValue;
	}
	
}
