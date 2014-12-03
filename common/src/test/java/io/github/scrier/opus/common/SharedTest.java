package io.github.scrier.opus.common;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import io.github.scrier.opus.common.Shared.Hazelcast;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class SharedTest {
	
	private static Logger log = LogManager.getLogger(SharedTest.class);

	@Test
	public void testConstructor() {
		Hazelcast testObject = new Shared.Hazelcast();
		assertEquals(Shared.Hazelcast.COMMON_MAP_UNIQUE_ID, testObject.COMMON_MAP_UNIQUE_ID);
		assertEquals(Shared.Hazelcast.BASE_NUKE_MAP, testObject.BASE_NUKE_MAP);
		assertEquals(Shared.Hazelcast.SETTINGS_MAP, testObject.SETTINGS_MAP);
	}
	
	@Test
	public void testFormatString() {
		log.info(Shared.Methods.formatTime(70, TimeUnit.SECONDS));
		log.info(Shared.Methods.formatTime(70, TimeUnit.MINUTES));
		log.info(Shared.Methods.formatTime(3915, TimeUnit.SECONDS));
		log.info(Shared.Methods.formatTime(3915, TimeUnit.MINUTES));
		log.info(Shared.Methods.formatTime(3915));
		log.info(Shared.Methods.formatTime(93915, TimeUnit.SECONDS));
		log.info(Shared.Methods.formatTime(93915, TimeUnit.MINUTES));
		log.info(Shared.Methods.formatTime(93915));
		log.info(Shared.Methods.formatTime(93915, TimeUnit.HOURS));
		log.info(Shared.Methods.formatTime(93915, TimeUnit.DAYS));
		log.info(Shared.Methods.formatTime(Integer.MAX_VALUE, TimeUnit.DAYS));
		
		assertEquals("70", Shared.Methods.formatTime(70, TimeUnit.SECONDS));
		assertEquals("1:10", Shared.Methods.formatTime(70, TimeUnit.MINUTES));
		assertEquals("3915", Shared.Methods.formatTime(3915, TimeUnit.SECONDS));
		assertEquals("65:15", Shared.Methods.formatTime(3915, TimeUnit.MINUTES));
		assertEquals("1:05:15", Shared.Methods.formatTime(3915));
		assertEquals("93915", Shared.Methods.formatTime(93915, TimeUnit.SECONDS));
		assertEquals("1565:15", Shared.Methods.formatTime(93915, TimeUnit.MINUTES));
		assertEquals("26:05:15", Shared.Methods.formatTime(93915));
		assertEquals("26:05:15", Shared.Methods.formatTime(93915, TimeUnit.HOURS));
		assertEquals("1:02:05:15", Shared.Methods.formatTime(93915, TimeUnit.DAYS));
		assertEquals("24855:03:14:07", Shared.Methods.formatTime(Integer.MAX_VALUE, TimeUnit.DAYS));
	}

}
