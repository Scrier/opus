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

import io.github.scrier.opus.common.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class NukeMsgFactory implements DataSerializableFactory {

	private static Logger log = LogManager.getLogger(NukeMsgFactory.class);

	public static final int FACTORY_ID = 830814;

	public static final int NUKE_EXECUTE_REQ =   Constants.NUKE_MSG_START + 1;
	public static final int NUKE_EXECUTE_RSP =   Constants.NUKE_MSG_START + 2;
	public static final int NUKE_EXECUTE_IND =   Constants.NUKE_MSG_START + 3;
	public static final int NUKE_STOP_ALL_REQ =      Constants.NUKE_MSG_START + 4;
	public static final int NUKE_STOP_ALL_RSP =      Constants.NUKE_MSG_START + 5;
	public static final int NUKE_TERMINATE_ALL_REQ = Constants.NUKE_MSG_START + 6;
	public static final int NUKE_TERMINATE_ALL_RSP = Constants.NUKE_MSG_START + 7;
	
	/**
	 * Constructor
	 */
	public NukeMsgFactory() {
		log.trace("NukeMsgFactory()");
	}
	
	@Override
	public IdentifiedDataSerializable create(int dataID) {
		log.trace("create(" + dataID + ")");
		IdentifiedDataSerializable retValue = null;
		switch(dataID) {
			case NUKE_EXECUTE_REQ:
				retValue = new NukeExecuteReqMsgC();
				break;
			case NUKE_EXECUTE_RSP:
				retValue = new NukeExecuteRspMsgC();
				break;
			case NUKE_EXECUTE_IND:
				retValue = new NukeExecuteIndMsgC();
				break;
			case NUKE_STOP_ALL_REQ:
				retValue = new NukeStopAllReqMsgC();
				break;
			case NUKE_STOP_ALL_RSP:
				retValue = new NukeStopAllRspMsgC();
				break;
			case NUKE_TERMINATE_ALL_REQ:
				retValue = new NukeTerminateAllReqMsgC();
				break;
			case NUKE_TERMINATE_ALL_RSP:
				retValue = new NukeTerminateAllRspMsgC();
				break;
		}
		return retValue;
	}

}
