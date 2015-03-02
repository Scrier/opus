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

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class BaseMsgC implements IdentifiedDataSerializable {

	private static Logger log = LogManager.getLogger(BaseMsgC.class);

	private long source;
	private long destination;
	private int sagaID;
	private int txID;
	private int factoryID;
	private int messageID;
	private SendIF sendIF;

	/**
	 * Constructor
	 * @param factoryID The factory id where the message class is defined.
	 * @param messageID The specified message id that is used from the class.
	 */
	public BaseMsgC(int factoryID, int messageID) {
		this(factoryID, messageID, null);
	}

	/**
	 * Constructor
	 * @param factoryID The factory id where the message class is defined.
	 * @param messageID The specified message id that is used from the class.
	 * @param sendIF The send interface to call when sending messages.
	 */
	public BaseMsgC(int factoryID, int messageID, SendIF sendIF) {
		log.trace("BaseMsgC(" + factoryID + ", " + messageID + ", " + sendIF + ")");
		this.source = -1L;
		this.destination = -1L;
		this.sagaID = -1;
		this.txID = -1;
		this.factoryID = factoryID;
		this.messageID = messageID;
		this.sendIF = sendIF;
	}

	/**
	 * Copy Constructor
	 * @param obj2copy BaseMsgC to copy.
	 */
	public BaseMsgC(BaseMsgC obj2copy) {
		log.trace("BaseMsgC(" + obj2copy + ")");
		this.source = obj2copy.source;
		this.destination = obj2copy.destination;
		this.sagaID = obj2copy.sagaID;
		this.txID = obj2copy.txID;
		this.factoryID = obj2copy.factoryID;
		this.messageID = obj2copy.messageID;
		this.sendIF = obj2copy.sendIF;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		this.source = in.readLong();
		this.destination = in.readLong();
		this.sagaID = in.readInt();
		this.txID = in.readInt();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(this.source);
		out.writeLong(this.destination);
		out.writeInt(this.sagaID);
		out.writeInt(this.txID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFactoryId() {
		return factoryID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getId() {
		return messageID;
	}

	/**
	 * @return the source
	 */
	public long getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(long source) {
		this.source = source;
	}

	/**
	 * @return the destination
	 */
	public long getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(long destination) {
		this.destination = destination;
	}

	/**
	 * @return the sagaID
	 */
	public int getSagaID() {
		return sagaID;
	}

	/**
	 * @param sagaID the sagaID to set
	 */
	public void setSagaID(int sagaID) {
		this.sagaID = sagaID;
	}

	/**
	 * @return the txID
	 */
	public int getTxID() {
		return txID;
	}

	/**
	 * @param txID the txID to set
	 */
	public void setTxID(int txID) {
		this.txID = txID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		String retValue = "BaseMsgC(";
		retValue += "source=" + source;
		retValue += ",destination=" + destination;
		retValue += ",sagaID=" + sagaID;
		retValue += ",txID=" + txID;
		retValue += ",factoryID=" + factoryID;
		retValue += ",messageID=" + messageID;
		retValue += ")";
		return retValue;
	}

	/**
	 * Method to send the message to the destination.
	 * @throws NullPointerException Thrown if no send interface is present.
	 */
	public void send() throws NullPointerException {
		log.trace("send()");
		if( null == this.sendIF ) {
			throw new NullPointerException("No sendif provided for message with factoryID " + getFactoryId() + " and message id " + getId() + ".");
		}
		this.sendIF.publishMessage(this);
	}

}
