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

public class NukeStopRspMsgC extends BaseMsgC {
	
	private static Logger log = LogManager.getLogger(NukeStopRspMsgC.class);
	
	private long processID;
	private boolean success;
	private String status;
	
	/**
	 * Constructor
	 */
	public NukeStopRspMsgC() {
		super(NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_STOP_RSP);
		log.trace("NukeStopRspMsgC()");
		setProcessID(Constants.HC_UNDEFINED);
		setSuccess(false);
		setStatus("");
	}

	/**
	 * Constructor
	 */
	public NukeStopRspMsgC(SendIF sendIF) {
		super(NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_STOP_RSP, sendIF);
		log.trace("NukeStopRspMsgC(" + sendIF + ")");
		setProcessID(Constants.HC_UNDEFINED);
		setSuccess(false);
		setStatus("");
	}
	
	/**
	 * Copy constructor
	 * @param obj2copy DukeCommand object
	 */
	public NukeStopRspMsgC(NukeStopRspMsgC obj2copy) {
		super(obj2copy);
		log.trace("NukeStopRspMsgC(" + obj2copy + ")");
		setProcessID(obj2copy.getProcessID());
		setSuccess(obj2copy.isSuccess());
		setStatus(obj2copy.getStatus());
	}
	
	/**
	 * Cast constructor
	 * @param input BaseNukeC object
	 * @throws ClassCastException if provided with a mismatching class.
	 */
	public NukeStopRspMsgC(BaseMsgC input) throws ClassCastException {
		super(input);
		log.trace("NukeStopRspMsgC(" + input + ")");
		if( input instanceof NukeStopRspMsgC ) {
			NukeStopRspMsgC obj2copy = (NukeStopRspMsgC)input;
			setProcessID(obj2copy.getProcessID());
			setSuccess(obj2copy.isSuccess());
			setStatus(obj2copy.getStatus());
		} else {
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof NukeStopRspMsgC[" + NukeMsgFactory.NUKE_STOP_RSP + "], are you using correct class?");
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
		setSuccess(in.readBoolean());
		setStatus(in.readUTF());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		super.writeData(out);
		out.writeLong(getProcessID());
		out.writeBoolean(isSuccess());
		out.writeUTF(getStatus());
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
	 * @return the success
	 */
  public boolean isSuccess() {
	  return success;
  }

	/**
	 * @param success the success to set
	 */
  public void setSuccess(boolean success) {
	  this.success = success;
  }
  
	/**
	 * @return the status
	 */
  public String getStatus() {
	  return status;
  }

	/**
	 * @param status the status to set
	 */
  public void setStatus(String status) {
	  this.status = status;
  }
  
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String retValue = "NukeStopRspMsgC{processID: " + getProcessID();
		retValue += ", success: " + isSuccess();
		retValue += ", status: " + getStatus() + "} - " + super.toString();
		return retValue;
	}
	
}
