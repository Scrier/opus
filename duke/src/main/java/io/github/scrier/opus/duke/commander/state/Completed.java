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
package io.github.scrier.opus.duke.commander.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;

/**
 * State handling for Completed transactions
 * @author andreas.joelsson
 */
public class Completed extends State {
	
	private static Logger log = LogManager.getLogger(Completed.class);

	public Completed(ClusterDistributorProcedure parent) {
	  super(parent);
  }

	@Override
	public void init() {
		log.trace("init()");
	}
	
	@Override
	public void shutDown() {
		log.trace("shutDown()");
	}
	
	@Override
	public void updated(BaseDataC data)  {
		log.trace("updated(" + data + ")");
	}  

	@Override
	public void evicted(BaseDataC data) {
		log.trace("evicted(" + data + ")");
	}

	@Override
	public void removed(Long key) {
		log.trace("removed(" + key + ")");
	}

	@Override
	public void timeout(long id) {
		log.trace("timeout(" + id + ")");
	}

}
