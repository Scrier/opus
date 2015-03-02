package io.github.scrier.opus.duke.commander.state;

import static org.junit.Assert.*;
import io.github.scrier.opus.ClusterDistributorProcedureTestObj;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.duke.commander.BaseActiveObjectMock;
import io.github.scrier.opus.duke.commander.Context;
import io.github.scrier.opus.duke.commander.DukeCommander;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class PeakDelayTest {

	private static TestHelper theHelper = TestHelper.INSTANCE;

	private HazelcastInstance instance;
	private long identity = 82495154L;
	private Context theContext = Context.INSTANCE;
	private BaseActiveObjectMock theBaseAOC;
	@SuppressWarnings("rawtypes")
	private IMap theMap;
	private ClusterDistributorProcedureTestObj distributor;

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
		distributor = theHelper.getRandomDistributor();
	}

	@After
	public void tearDown() throws Exception {
		theContext.shutDown();
	}
	
	@Test
	public void testConstructor() {
		PeakDelay testObject = new PeakDelay(distributor);
		assertEquals("PeakDelay", testObject.getClassName());
	}
	
	@Test
	public void testInit() {
		PeakDelay testObject = new PeakDelay(distributor);
		testObject.init();
		assertEquals(distributor.TimeoutTime, testObject.getPeakDelaySeconds());
		assertEquals(distributor.TimeoutTimerID, testObject.getTimerID());
		assertEquals(1, distributor.TimeoutCalls);
	}
	
	@Test
	public void testUpdated() {
		PeakDelay testObject = new PeakDelay(distributor);
		testObject.setState(testObject.PEAK_DELAY);
		testObject.updated(new BaseDataC(1, 2));
		assertEquals(distributor.PEAK_DELAY, testObject.getState());
	}
	
	@Test(expected=RuntimeException.class)
	public void testUpdatedException() {
		PeakDelay testObject = new PeakDelay(distributor);
		testObject.updated(new BaseDataC(1, 2));
		fail("Expected an exepction before this.");
	}
	
	@Test
	public void testEvicted() {
		PeakDelay testObject = new PeakDelay(distributor);
		testObject.setState(testObject.PEAK_DELAY);
		testObject.evicted(new BaseDataC(1, 2));
		assertEquals(distributor.PEAK_DELAY, testObject.getState());
	}
	
	@Test
	public void testRemoved() {
		PeakDelay testObject = new PeakDelay(distributor);
		testObject.setState(testObject.PEAK_DELAY);
		testObject.removed(12345L);
		assertEquals(distributor.PEAK_DELAY, testObject.getState());
	}
	
	@Test
	public void testTimeoutTerminateID() {
		PeakDelay testObject = new PeakDelay(distributor);
		testObject.setState(testObject.PEAK_DELAY);
		testObject.timeout(testObject.getTerminateID());
		assertEquals(testObject.TERMINATING, testObject.getState());
	}
	
	@Test(expected=RuntimeException.class)
	public void testTimeoutWrongID() {
		PeakDelay testObject = new PeakDelay(distributor);
		testObject.setState(testObject.PEAK_DELAY);
		long id = 0;
		while( id == testObject.getTimerID() || id == testObject.getTerminateID() ) {
			id++;
		}
		testObject.timeout(id);
		fail("Should throw exception.");
	}
	
	@Test
	public void testTimeoutTimeoutOK() {
		PeakDelay testObject = new PeakDelay(distributor);
		testObject.setState(testObject.PEAK_DELAY);
		distributor.nukesReady = true;
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.RAMPING_DOWN, testObject.getState());
		assertEquals(0, distributor.TimeoutCalls);
	}

}
