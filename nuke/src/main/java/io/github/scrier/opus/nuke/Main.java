package io.github.scrier.opus.nuke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class Main {

	private static Logger log = LogManager.getLogger(Main.class);
	
	private HazelcastInstance instance;
	
	public static void main(String[] args) {
		log.trace("Main(" + args + ")");
		new Main(args);
	}
	
	public Main(String[] args) {
		log.trace("Main(" + args + ")");
		
		instance = Hazelcast.newHazelcastInstance(null);
		
		NukeAOC nukeAOC = new NukeAOC(instance);
		nukeAOC.preInit();
	}

}
