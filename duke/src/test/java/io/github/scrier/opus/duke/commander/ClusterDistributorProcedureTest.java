package io.github.scrier.opus.duke.commander;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.nuke.NukeState;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ClusterDistributorProcedureTest {
	
	private static TestHelper theHelper = TestHelper.INSTANCE;

	private HazelcastInstance instance;
	private long identity = 8239421L;
	private long component = 7283724L;
	private Context theContext = Context.INSTANCE;
	private BaseActiveObjectMock theBaseAOC;
	@SuppressWarnings("rawtypes")
	private IMap theMap;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		theHelper.setLogLevel(Level.TRACE);
	}

	@Before
	public void setUp() throws Exception {
		instance = theHelper.mockHazelcast();
		theHelper.mockIdGen(instance, Shared.Hazelcast.COMMON_MAP_UNIQUE_ID, identity);
		theMap = theHelper.mockMap(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		theBaseAOC = new BaseActiveObjectMock(instance);
		theBaseAOC.preInit();
		theContext.init(new DukeCommander(instance), theBaseAOC);
	}

	@After
	public void tearDown() throws Exception {
		theContext.shutDown();
	}

	@Test
	public void testConstructor() throws Exception {
		ClusterDistributorProcedure testObject = new ClusterDistributorProcedure();
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(0, testObject.getMinNodes());
		assertEquals(0, testObject.getMaxUsers());
		assertEquals(0, testObject.getIntervalSeconds());
		assertEquals(0, testObject.getUserIncrease());
		assertEquals(0, testObject.getPeakDelaySeconds());
		assertEquals(0, testObject.getTerminateSeconds());
		assertEquals(false, testObject.isRepeated());
		assertEquals(true, testObject.isShutDownOnce());
		assertEquals("", testObject.getCommand());
		assertEquals("", testObject.getFolder());
		assertEquals(Constants.HC_UNDEFINED, testObject.getTimerID());
		assertEquals(Constants.HC_UNDEFINED, testObject.getTerminateID());
	}

}
