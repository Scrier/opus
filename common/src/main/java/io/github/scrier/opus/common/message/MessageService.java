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
package io.github.scrier.opus.common.message;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class MessageService implements MessageListener<IdentifiedDataSerializable>, SendIF {
	
	private static Logger log = LogManager.getLogger(MessageService.class);
	
	private MessageIF messageIF;
	private Map<Integer, ITopic<IdentifiedDataSerializable>> topicsMap;
	
	/**
	 * Constructor
	 * @param messageIF send interface that will publish messages to the system.
	 */
	public MessageService(MessageIF messageIF) {
		this.messageIF = messageIF;
		this.topicsMap = new HashMap<Integer, ITopic<IdentifiedDataSerializable>>();
	}
	
	/**
	 * Method to register on a factory of messages.
	 * @param factoryID int with the factory id. 
	 * @return boolean if successfult or not.
	 */
	public boolean registerOnFactory(int factoryID) {
	 log.trace("registerOnFactory(" + factoryID + ")");
		boolean retValue = true;
		if( true ==  topicsMap.containsKey(factoryID) ) {
			log.error("Already registered on factory with id: " + factoryID + ".");
			retValue = false;
		} else {
			log.debug("Registering on factory id: " + factoryID + ".");
			String topic = Integer.toString(factoryID);
			ITopic<IdentifiedDataSerializable> top = messageIF.getInstance().getTopic(topic);
			top.addMessageListener(this);
			topicsMap.put(factoryID, top);
		}
		return retValue;
	}
	
	/**
	 * Method to unregister on a factory of messages.
	 * @param factoryID int with the factory id.
	 * @return boolean if successful or not.
	 */
	public boolean unRegisterOnFactory(int factoryID) {
		log.trace("unRegisterOnFactory(" + factoryID + ")");
		boolean retValue = true;
		if( false == topicsMap.containsKey(factoryID) ) {
			log.error("Not registered on factory with id: " + factoryID + ".");
			retValue = false;
		} else {
			log.debug("Unregistering on factory id: " + factoryID + ".");
			ITopic<IdentifiedDataSerializable> top = topicsMap.get(factoryID);
			top.removeMessageListener(Integer.toString(factoryID));
			topicsMap.remove(factoryID);
		}
		return retValue;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public void onMessage(Message<IdentifiedDataSerializable> inMessage) {
		log.trace("onMessge(" + inMessage + ")");
		BaseMsgC msg = (BaseMsgC)inMessage.getMessageObject();
		messageIF.handleInMessage(msg);
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void publishMessage(BaseMsgC message) {
	  if( false == topicsMap.containsKey(message.getFactoryId()) ) {
	  	log.error("No topic is registered for message " + message + " with factory id: " + message.getFactoryId() + ".");
	  } else {
	  	topicsMap.get(message.getFactoryId()).publish(message);
	  }
  }

}
