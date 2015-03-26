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

import static org.junit.Assert.assertEquals;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeExecuteIndMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;

public class CommonCheck {

	/**
	 * Method to check the NukeExecuteInd Message
	 * @param msg BaseMsg to check
	 * @param expectedStatus expected status
	 * @param containsResponse contains response
	 */
	public static void assertNukeExecuteIndMsgC(BaseMsgC msg, CommandState expectedStatus, long expectedProcess) {
		assertCorrectBaseMessage(msg, NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_EXECUTE_IND);
		NukeExecuteIndMsgC check = new NukeExecuteIndMsgC(msg);
		assertEquals(expectedStatus, check.getStatus());
		assertEquals(expectedProcess, check.getProcessID());
	}
	
	/**
	 * Method to validate a base message for converting later on.
	 * @param msg BaseMsgC instance to check
	 * @param factoryID expected factoryID.
	 * @param messageID expected messageID.
	 */
	public static void assertCorrectBaseMessage(BaseMsgC msg, long factoryID, long messageID) {
		assertEquals(factoryID, msg.getFactoryId());
		assertEquals(messageID, msg.getId());
	}
	
}
