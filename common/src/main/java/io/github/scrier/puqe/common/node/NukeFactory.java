package io.github.scrier.puqe.common.node;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class NukeFactory implements DataSerializableFactory {
	
	private static Logger log = LogManager.getLogger(NukeFactory.class);
	
	public NukeFactory() { 
		log.trace("NukeFactory()");
	}
	
	public static final int FACTORY_ID = 801023;
	
	public static final int NUKE_INFO = 1;

	@Override
	public IdentifiedDataSerializable create(int dataID) {
		log.trace("create(" + dataID + ")");
		IdentifiedDataSerializable retValue = null;
		switch(dataID) {
			case NUKE_INFO:
				retValue = new NukeInfo();
				break;
		}
		return retValue;
	}
	
}
