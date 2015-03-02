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
package io.github.scrier.opus.duke.commander;

import java.util.ArrayList;

import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.message.MessageService;

public class MessageServiceMock extends MessageService {
	
	private ArrayList<BaseMsgC> messages;
	
	public MessageServiceMock() {
		super(new MessageIFImpl());
		setMessages(new ArrayList<BaseMsgC>());
	}

	@Override
  public void publishMessage(BaseMsgC message) {
	  messages.add(message);
  }
	
	public void clear() {
		messages.clear();
	}
	
	public int size() {
		return messages.size();
	}
	
	public boolean isEmpty() {
		return messages.isEmpty();
	}
	
	public BaseMsgC getMessage(int index) {
		if( index >= size() ) {
			return null;
		}
		return messages.get(index);
	}

	/**
	 * @return the messages
	 */
  public ArrayList<BaseMsgC> getMessages() {
	  return messages;
  }

	/**
	 * @param messages the messages to set
	 */
  private void setMessages(ArrayList<BaseMsgC> messages) {
	  this.messages = messages;
  }
  
}
