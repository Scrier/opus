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
	public static final int NUKE_COMMAND = 2;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IdentifiedDataSerializable create(int dataID) {
		log.trace("create(" + dataID + ")");
		IdentifiedDataSerializable retValue = null;
		switch(dataID) {
			case NUKE_INFO:
				retValue = new NukeInfo();
				break;
			case NUKE_COMMAND:
				retValue = new NukeCommand();
				break;
		}
		return retValue;
	}
	
}
