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
package io.github.scrier.opus.duke;

import io.github.scrier.opus.common.aoc.BaseActiveObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;

public class DukeAOC extends BaseActiveObject {
	
	public static Logger log = LogManager.getLogger(DukeAOC.class);
	
	public DukeAOC(HazelcastInstance instance) {
		super(instance);
		log.trace("DukeRunner(" + instance + ")");
	}
	
	public DukeAOC(HazelcastInstance instance, String xmlFile) {
		super(instance);
		log.trace("DukeRunner(" + instance + ", " + xmlFile + ")");
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

	
}
