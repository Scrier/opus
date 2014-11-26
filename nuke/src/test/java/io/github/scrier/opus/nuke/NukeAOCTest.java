package io.github.scrier.opus.nuke;

import static org.junit.Assert.*;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.exception.InvalidOperationException;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class NukeAOCTest {
	
	static TestHelper helper = TestHelper.INSTANCE;
	
	long identity = 4804252L;
	HazelcastInstance instance;
	@SuppressWarnings("rawtypes")
  IMap map;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		helper.setLogLevel(Level.TRACE);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		instance = helper.mockHazelcast();
		helper.mockIdGen(instance, Shared.Hazelcast.COMMON_MAP_UNIQUE_ID, identity);
		map = helper.mockMap(instance, Shared.Hazelcast.BASE_NUKE_MAP);
	}

	@After
	public void tearDown() throws Exception {
		instance = null;
	}

	@Test
	public void testPreInit() throws Exception {
		NukeAOC testObject = new NukeAOC(instance);
		testObject.preInit();
		assertEquals(identity, testObject.getIdentity());
	}

}
