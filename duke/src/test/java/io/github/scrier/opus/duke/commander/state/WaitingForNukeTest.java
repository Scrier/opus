package io.github.scrier.opus.duke.commander.state;

import static org.junit.Assert.*;
import io.github.scrier.opus.ClusterDistributorProcedureTestObj;
import io.github.scrier.opus.StateImpl;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.data.BaseDataC;
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
		testObject.setState(testObject.WAITING_FOR_NUKE);
		testObject.updated(new BaseDataC(1, 2));
		assertEquals(distributor.WAITING_FOR_NUKE, testObject.getState());
		checkDefault(testObject);
	}
	
	@Test(expected=RuntimeException.class)
	public void testUpdatedException() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.updated(new BaseDataC(1, 2));
		fail("Expected an exepction before this.");
	}
	
	@Test
	public void testEvicted() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.setState(testObject.WAITING_FOR_NUKE);
		testObject.evicted(new BaseDataC(1, 2));
		assertEquals(distributor.WAITING_FOR_NUKE, testObject.getState());
		checkDefault(testObject);
	}
	
	@Test
	public void testRemoved() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.setState(testObject.WAITING_FOR_NUKE);
		testObject.removed(12345L);
		assertEquals(distributor.WAITING_FOR_NUKE, testObject.getState());
		checkDefault(testObject);
	}

	@Test
	public void testTimeoutTerminateID() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.setState(testObject.WAITING_FOR_NUKE);
		testObject.timeout(testObject.getTerminateID());
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testTimeoutWrongID() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.setState(testObject.WAITING_FOR_NUKE);
		long id = 0;
		while( id == testObject.getTimerID() || id == testObject.getTerminateID() ) {
			id++;
		}
		testObject.timeout(id);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testTimeoutTimeoutNotWaiting() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.setState(testObject.WAITING_FOR_NUKE);
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.WAITING_FOR_NUKE, testObject.getState());
		assertEquals(testObject.getTimerID(), distributor.TimeoutTimerID);
		assertEquals(testObject.getWaitingForNukeTimeout(), distributor.TimeoutTime);
		assertEquals(1, distributor.TimeoutCalls);
	}
	
	@Test
	public void testTimeoutTimeoutOK() {
		WaitingForNuke testObject = new WaitingForNuke(distributor);
		testObject.setState(testObject.WAITING_FOR_NUKE);
		distributor.nukesReady = true;
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.RAMPING_UP, testObject.getState());
		assertEquals(0, distributor.TimeoutCalls);
	}
	
	public void checkDefault(WaitingForNuke testObject) {
		assertEquals(distributor.getFolder(), testObject.getFolder());
		assertEquals(distributor.getCommand(), testObject.getCommand());
		assertEquals(distributor.getMaxUsers(), testObject.getMaxUsers());
		assertEquals(distributor.getPeakDelaySeconds(), testObject.getPeakDelaySeconds());
		assertEquals(distributor.getState(), testObject.getState());
		assertEquals(distributor.getTerminateID(), testObject.getTerminateID());
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
		assertEquals("WaitingForNuke", testObject.getClassName());
	}

}
