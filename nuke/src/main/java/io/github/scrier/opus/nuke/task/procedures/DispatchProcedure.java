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
package io.github.scrier.opus.nuke.task.procedures;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.NukeExecuteReqMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;
import io.github.scrier.opus.nuke.task.BaseNukeProcedure;

public class DispatchProcedure extends BaseNukeProcedure {
	
	private static Logger log = LogManager.getLogger(DispatchProcedure.class);
	
	public DispatchProcedure() {
		log.trace("DispatchProcedure()");
	}

	@Override
  public void init() throws Exception {
	  log.trace("init()");
  }

	@Override
  public void shutDown() throws Exception {
		log.trace("shutDown()");
  }

	@Override
  public int handleOnUpdated(BaseDataC value) {
		log.trace("handleOnUpdated(" + value + ")");
	  return getState();
  }

	@Override
  public int handleOnEvicted(BaseDataC value) {
		log.trace("handleOnRemoved(" + value + ")");
	  return getState();
  }

	@Override
  public int handleOnRemoved(Long key) {
		log.trace("handleOnRemoved(" + key + ")");
	  return getState();
  }

	@Override
  public int handleInMessage(BaseMsgC message) {
		log.trace("handleInMessage(" + message + ")");
		switch( message.getId() ) {
			case NukeMsgFactory.NUKE_EXECUTE_REQ: {
				NukeExecuteReqMsgC pNukeExecuteReq = new NukeExecuteReqMsgC(message);
				handleMessage(pNukeExecuteReq);
				break;
			}
		}
	  return getState();
  }
	
	/**
	 * Method to handle the NukeExecuteReqMsgC message.
	 * @param msg NukeExecuteReqMsgC instance.
	 */
	protected void handleMessage(NukeExecuteReqMsgC msg) {
		log.trace("handleMessage(" + msg + ")");
		if( msg.isRepeated() ) {
			registerProcedure(new RepeatedExecuteTaskProcedure(msg));
		} else {
			registerProcedure(new ExecuteTaskProcedure(msg));
		}
	}
}
