package io.github.scrier.opus.duke.commander.state;

import static org.junit.Assert.*;
import io.github.scrier.opus.ClusterDistributorProcedureTestObj;
import io.github.scrier.opus.StateImpl;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.duke.commander.BaseActiveObjectMock;
import io.github.scrier.opus.duke.commander.Context;
import io.github.scrier.opus.duke.commander.DukeCommander;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class WaitingForNukeTest {

	private static TestHelper theHelper = TestHelper.INSTANCE;

	private HazelcastInstance instance;
	private long identity = 824951L;
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
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		checkDefault(testObject);
	}
	
	@Test
	public void testInit() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.init();
		assertEquals(distributor.TimeoutTime, testObject.getWaitingForNukeTimeout());
		assertEquals(distributor.TimeoutTimerID, testObject.getTimerID());
		assertEquals(1, distributor.TimeoutCalls);
	}
	
	@Test
	public void testUpdated() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.updated(new BaseNukeC(1, 2));
		checkDefault(testObject);
	}
	
	@Test
	public void testEvicted() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.evicted(new BaseNukeC(1, 2));
		checkDefault(testObject);
	}
	
	@Test
	public void testRemoved() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.removed(12345L);
		checkDefault(testObject);
	}

	
	public void checkDefault(WaitingForNuke testObject) {
		assertEquals(distributor.getFolder(), testObject.getFolder());
		assertEquals(distributor.getCommand(), testObject.getCommand());
		assertEquals(distributor.getMaxUsers(), testObject.getMaxUsers());
		assertEquals(distributor.getPeakDelaySeconds(), testObject.getPeakDelaySeconds());
		assertEquals(distributor.getState(), testObject.getState());
		assertEquals(distributor.getTerminateID(), testObject.getTerminateID());
		assertEquals(distributor.getTimerID(), testObject.getTimerID());
		assertEquals(distributor.getUserIncrease(), testObject.getUserIncrease());
		assertEquals(distributor.getState(), testObject.getState());
		assertEquals(distributor.isRepeated(), testObject.isRepeated());
		assertEquals(distributor.isNukesReady(), testObject.isNukesReady());
		assertEquals(distributor.isTimeoutActive(1123), testObject.isTimeoutActive(1123));
		assertEquals(distributor.ABORTED, testObject.ABORTED);
		assertEquals(distributor.COMPLETED, testObject.COMPLETED);
		assertEquals(distributor.CREATED, testObject.CREATED);
		assertEquals(distributor.PEAK_DELAY, testObject.PEAK_DELAY);
		assertEquals(distributor.RAMPING_DOWN, testObject.RAMPING_DOWN);
		assertEquals(distributor.RAMPING_UP, testObject.RAMPING_UP);
		assertEquals(distributor.TERMINATING, testObject.TERMINATING);
		assertEquals(distributor.WAITING_FOR_NUKE, testObject.WAITING_FOR_NUKE);
		assertEquals(distributor.CREATED, testObject.getState());
		assertEquals("WaitingForNuke", testObject.getClassName());
	}

}
