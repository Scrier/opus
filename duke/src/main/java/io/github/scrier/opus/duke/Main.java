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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

public class Main {
	
	private static Logger log = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		log.trace("main(" + args + ")");
		new Main(args);
	}
	
	public Main(String[] args) {
		log.trace("Main(" + args + ")");
		HazelcastInstance instance = HazelcastClient.newHazelcastClient(null);
		
		for( String str : args ) {
			log.info("Argument: " + str);
		}
		DukeRunnerAOC runner = null;
		if( args.length > 0 && args[0].contains(".xml") ) {
			log.info("Running custom xml file: " + args[0] + ".");
			runner = new DukeRunnerAOC(instance, args[0]);
		} else {
			log.info("Running standard settings");
			runner = new DukeRunnerAOC(instance);
		}
		runner.preInit();
	}

}
