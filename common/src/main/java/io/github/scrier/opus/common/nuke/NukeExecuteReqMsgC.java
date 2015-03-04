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

import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.message.SendIF;

public class NukeExecuteReqMsgC extends BaseMsgC {
	
	private static Logger log = LogManager.getLogger(NukeExecuteReqMsgC.class);
	
	private String command;
	private String folder;
	private boolean repeated;
	
	/**
	 * Constructor
	 */
	public NukeExecuteReqMsgC() {
		super(NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_EXECUTE_REQ);
		log.trace("NukeExecuteReqMsgC()");
		setCommand("");
		setFolder("");
		setRepeated(false);
	}

	/**
	 * Constructor
	 */
	public NukeExecuteReqMsgC(SendIF sendIF) {
		super(NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_EXECUTE_REQ, sendIF);
		log.trace("NukeExecuteReqMsgC(" + sendIF + ")");
		setCommand("");
		setFolder("");
		setRepeated(false);
	}
	
	/**
	 * Copy constructor
	 * @param obj2copy DukeCommand object
	 */
	public NukeExecuteReqMsgC(NukeExecuteReqMsgC obj2copy) {
		super(obj2copy);
		log.trace("NukeExecuteReqMsgC(" + obj2copy + ")");
		setCommand(obj2copy.getCommand());
		setFolder(obj2copy.getFolder());
		setRepeated(obj2copy.isRepeated());
	}
	
	/**
	 * Cast constructor
	 * @param input BaseNukeC object
	 * @throws ClassCastException if provided with a mismatching class.
	 */
	public NukeExecuteReqMsgC(BaseMsgC input) throws ClassCastException {
		super(input);
		log.trace("NukeExecuteReqMsgC(" + input + ")");
		if( input instanceof NukeExecuteReqMsgC ) {
			NukeExecuteReqMsgC obj2copy = (NukeExecuteReqMsgC)input;
			setCommand(obj2copy.getCommand());
			setFolder(obj2copy.getFolder());
			setRepeated(obj2copy.isRepeated());
		} else {
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof NukeExecuteReqMsgC[" + NukeMsgFactory.NUKE_EXECUTE_REQ + "], are you using correct class?");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		log.trace("readData(" + in + ")");
		super.readData(in);
		setCommand(in.readUTF());
		setFolder(in.readUTF());
		setRepeated(in.readBoolean());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		super.writeData(out);
		out.writeUTF(getCommand());
		out.writeUTF(getFolder());
		out.writeBoolean(isRepeated());
	}

	/**
	 * @return the command
	 */
  public String getCommand() {
	  return command;
  }

	/**
	 * @param command the command to set
	 */
  public void setCommand(String command) {
	  this.command = command;
  }

	/**
	 * @return the folder
	 */
  public String getFolder() {
	  return folder;
  }

	/**
	 * @param folder the folder to set
	 */
  public void setFolder(String folder) {
	  this.folder = folder;
  }

	/**
	 * @return the repeated
	 */
  public boolean isRepeated() {
	  return repeated;
  }

	/**
	 * @param repeated the repeated to set
	 */
  public void setRepeated(boolean repeated) {
	  this.repeated = repeated;
  }
  
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String retValue = "NukeExecuteReqMsgC{command: " + getCommand(); 
		retValue += ", folder: " + getFolder();
		retValue += ", repeated: " + isRepeated() + "} - " + super.toString();
		return retValue;
	}

}
