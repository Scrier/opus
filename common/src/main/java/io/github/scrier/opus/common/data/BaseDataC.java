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
package io.github.scrier.opus.common.data;

import io.github.scrier.opus.common.Constants;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class BaseDataC implements IdentifiedDataSerializable {
	
	private static Logger log = LogManager.getLogger(BaseDataC.class);
	
	private long key;
	private int txID;
	private int factoryID;
	private int messageID;
	
	public BaseDataC(int factoryID, int messageID) {
		log.trace("BaseDataC(" + factoryID + ", " + messageID + ")");
		setKey(Constants.HC_UNDEFINED);
		setFactoryID(factoryID);
		setMessageID(messageID);
	}
	
	public BaseDataC(BaseDataC obj2copy) {
		log.trace("BaseDataC(" + obj2copy + ")");
		setKey(obj2copy.getKey());
		setTxID(obj2copy.getTxID());
		setFactoryID(obj2copy.getFactoryId());
		setMessageID(obj2copy.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		log.trace("readData(" + in + ")"); 
		setKey(in.readLong());
		setTxID(in.readInt());
		setFactoryID(in.readInt());
		setMessageID(in.readInt());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		out.writeLong(getKey());
		out.writeInt(getTxID());
		out.writeInt(getFactoryId());
		out.writeInt(getId());
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
	 * @param factoryID the factoryID to set
	 */
	private void setFactoryID(int factoryID) {
		this.factoryID = factoryID;
	}
	
	/**
	 * @param messageID the messageID to set
	 */
	private void setMessageID(int messageID) {
		this.messageID = messageID;
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
	 * @return the key
	 */
  public long getKey() {
	  return key;
  }

	/**
	 * @param key the key to set
	 */
  public void setKey(long key) {
	  this.key = key;
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String retValue = "BaseDataC{key: " + getKey();
		retValue += ", txID:" + getTxID();
		retValue += ", factoryID:" + getFactoryId();
		retValue += ", messageID:" + getId() + "}";
		return retValue;
	}

}
