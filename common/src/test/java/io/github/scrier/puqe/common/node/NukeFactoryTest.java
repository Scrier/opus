package io.github.scrier.puqe.common.node;

import static org.junit.Assert.*;
import io.github.scrier.puqe.common.TestHelper;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class NukeFactoryTest {

	NukeFactory testObject;
	
	@BeforeClass
	public static void setupClass() {
		TestHelper.INSTANCE.setLogLevel(Level.TRACE);
	}
	
	@Before
	public void setup() {
		testObject = new NukeFactory();
	}
	
	@After
	public void tearDown() {
		testObject = null;
	}
	
	@Test
	public void testNukeInfoFactoryCreate() {
		IdentifiedDataSerializable actual = testObject.create(NukeFactory.NUKE_INFO);
		assertNotNull(actual);
		NukeInfo expected = new NukeInfo();
		assertEquals(expected.getClass(), actual.getClass());
	}
	
	@Test
	public void testNullFactoryCreate() {
		IdentifiedDataSerializable actual = testObject.create(-1);
		assertNull(actual);
	}
	
	@Test
	public void testFactoryID() {
		assertEquals(801023, NukeFactory.FACTORY_ID);
	}
	
	

}
