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

import java.util.Map;

import io.github.scrier.opus.common.aoc.BaseActiveObject;
import io.github.scrier.opus.common.duke.DukeMsgFactory;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;
import io.github.scrier.opus.duke.commander.Context;
import io.github.scrier.opus.duke.commander.DukeCommander;
import io.github.scrier.opus.duke.io.XmlSettings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;

public class DukeAOC extends BaseActiveObject {
	
	public static Logger log = LogManager.getLogger(DukeAOC.class);
	
	private DukeCommander commander;
	private XmlSettings settings;
	
	public DukeAOC(HazelcastInstance instance) {
		super(instance);
		log.trace("DukeRunner(" + instance + ")");
		commander = null;
		settings = new XmlSettings();
	}
	
	public DukeAOC(HazelcastInstance instance, String xmlFile) {
		super(instance);
		log.trace("DukeRunner(" + instance + ", " + xmlFile + ")");
		commander = null;
		settings = new XmlSettings(xmlFile);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		log.trace("init()");
		commander = new DukeCommander(getInstance());
		Context.INSTANCE.init(commander, this);
		if( true != registerOnFactory(DukeMsgFactory.FACTORY_ID) ) {
			log.fatal("Unable to register on factory message. " + DukeMsgFactory.FACTORY_ID + ", cannot continue.");
			shutDown();
			return;
		}
		if( true != registerOnFactory(NukeMsgFactory.FACTORY_ID) ) {
			log.fatal("Unable to register on factory message. " + NukeMsgFactory.FACTORY_ID + ", cannot continue.");
			shutDown();
			return;
		}
		settings.init();
		log.info("Settings read: ");
		for( Map.Entry<String, String> item : settings.getSettings().entrySet() ) {
			try {
				log.info(item.getKey() + " => " + item.getValue() + ".");
	      getSettings().put(item.getKey(), item.getValue());
      } catch (InvalidOperationException e) {
	      log.error("Received error on put method call to distributed settings.", e);
      }
		}
		commander.init();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() {
		log.trace("shutDown()");
		if( true != unRegisterOnFactory(DukeMsgFactory.FACTORY_ID) ) {
			log.fatal("Unable to remove registration for factory id: " + DukeMsgFactory.FACTORY_ID + ".");
		}
		if( true != unRegisterOnFactory(NukeMsgFactory.FACTORY_ID) ) {
			log.fatal("Unable to remove registration for factory id: " + NukeMsgFactory.FACTORY_ID + ".");
		}
		commander.shutDown();
		Context.INSTANCE.shutDown();
		getInstance().getLifecycleService().shutdown();
		log.info("System exit.");
		System.exit(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void handleInMessage(BaseMsgC message) {
		log.trace("handleInMessage(" + message + ")");
	  try {
	    commander.handleInMessage(message);
    } catch (InvalidOperationException e) {
	    log.error("Command threw a InvalidOperationException from handleInMessage method.", e);
    }
  }

	
}
