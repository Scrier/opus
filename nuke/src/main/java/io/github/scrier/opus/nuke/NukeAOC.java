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
package io.github.scrier.opus.nuke;

import com.hazelcast.core.HazelcastInstance;

import io.github.scrier.opus.common.aoc.BaseActiveObject;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;
import io.github.scrier.opus.nuke.task.Context;
import io.github.scrier.opus.nuke.task.NukeTasks;

public class NukeAOC extends BaseActiveObject {

	private NukeTasks nukeTasks;
	
	public NukeAOC(HazelcastInstance instance) throws InvalidOperationException {
		super(instance);
		nukeTasks = new NukeTasks(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		log.trace("init()");
		Context.INSTANCE.init(nukeTasks, this);
		if( true != registerOnFactory(NukeMsgFactory.FACTORY_ID) ) {
			log.fatal("Unable to register on factoru message. " + NukeMsgFactory.FACTORY_ID + ", cannot continue.");
			shutDown();
			return;
		}
    nukeTasks.init();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() {
		Context.INSTANCE.shutDown();
		if( true != unRegisterOnFactory(NukeMsgFactory.FACTORY_ID) ) {
			log.fatal("Unable to remove registration for factory id: " + NukeMsgFactory.FACTORY_ID + ".");
		}
		nukeTasks.shutDown();
		Context.INSTANCE.shutDown();
		getInstance().getLifecycleService().shutdown();
		log.info("System exit.");
		System.exit(0);
	}

	@Override
  public void handleInMessage(BaseMsgC message) {
		log.trace("handleInMessage(" + message + ")");
	  try {
	  	nukeTasks.handleInMessage(message);
    } catch (InvalidOperationException e) {
	    log.error("Command threw a InvalidOperationException from handleInMessage method.", e);
    }
  }

}
