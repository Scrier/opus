package io.github.scrier.opus.duke.io;

import static org.junit.Assert.*;

import java.util.Map;

import io.github.scrier.opus.TestHelper;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;


public class XmlSettingsTest {
	
	public static Logger log = LogManager.getLogger(XmlSettingsTest.class);
	
	@BeforeClass
	public static void setupClass() {
		TestHelper.INSTANCE.setLogLevel(Level.TRACE);
	}

	@Test
	public void testDefautSettings() {
		XmlSettings testObject = new XmlSettings(getClass().getResource("/DefaultSettings.xml").getPath());
		assertTrue(testObject.init());
		assertEquals(11, testObject.getSettings().size());
		log.info(testObject);
	}
	
	@Test
	public void testTestMesage() {
		XmlSettings testObject = new XmlSettings(getClass().getResource("/TestXml.xml").getPath());
		assertTrue(testObject.init());
		assertEquals(11, testObject.getSettings().size());
		log.info(testObject);
		Map<String,String> map = testObject.getSettings();
		assertEquals("1", map.get("name1"));
		assertEquals("2", map.get("name2"));
		assertEquals("3", map.get("name3"));
		assertEquals("4", map.get("name4"));
		assertEquals("5", map.get("name5"));
		assertEquals("6", map.get("name6"));
		assertEquals("7", map.get("name7"));
		assertEquals("8", map.get("name8"));
		assertEquals("9", map.get("name9"));
		assertEquals("10", map.get("name10"));
		assertEquals("11", map.get("name11"));
	}

}
