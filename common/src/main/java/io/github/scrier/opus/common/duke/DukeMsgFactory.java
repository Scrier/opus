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
package io.github.scrier.opus.common.duke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DukeMsgFactory implements DataSerializableFactory {

	private static Logger log = LogManager.getLogger(DukeDataFactory.class);

	public static final int FACTORY_ID = 150112;
	
	public static final int DUKE_COMMAND = 1;
	
	/**
	 * Constructor
	 */
	public DukeMsgFactory() {
		log.trace("DukeMsgFactory()");
	}
	
	@Override
	public IdentifiedDataSerializable create(int dataID) {
		log.trace("create(" + dataID + ")");
		IdentifiedDataSerializable retValue = null;
		switch(dataID) {
			case DUKE_COMMAND:
				retValue = new DukeCommandMsgC();
				break;
		}
		return retValue;
	}
	
}
