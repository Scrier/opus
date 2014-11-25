package io.github.scrier.opus.common.aoc;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.management.request.GetMemberSystemPropertiesRequest;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class BaseNukeC implements IdentifiedDataSerializable {
	
	private static Logger log = LogManager.getLogger(BaseNukeC.class);
	
	private int txID;
	private int factoryID;
	private int messageID;
	
	public BaseNukeC(int factoryID, int messageID) {
		setFactoryID(factoryID);
		setMessageID(messageID);
		setTxID(-1);
	}
	
	public BaseNukeC(BaseNukeC obj2copy) {
		setFactoryID(obj2copy.getFactoryId());
		setMessageID(obj2copy.getId());
		setTxID(obj2copy.getTxID());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		log.trace("readData(" + in + ")"); 
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String retValue = "BaseNukeC{txID:" + getTxID();
		retValue += ", factoryID:" + getFactoryId();
		retValue += ", messageID:" + getId() + "}";
		return retValue;
	}

}
