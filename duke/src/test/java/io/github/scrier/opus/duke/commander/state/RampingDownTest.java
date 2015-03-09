package io.github.scrier.opus.duke.commander.state;

import static org.junit.Assert.*;
import io.github.scrier.opus.ClusterDistributorProcedureTestObj;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.nuke.NukeState;
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

public class RampingDownTest {

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
		RampingDown testObject = new RampingDown(distributor);
		assertEquals(-1, testObject.getOldUsers());
		assertTrue(testObject.isDoOnce());
		assertEquals(0, testObject.getActiveNukeCommands().size());
		assertEquals(5, testObject.getRampDownUpdateSeconds());
		assertEquals("RampingDown", testObject.getClassName());
	}
	
	@Test
	public void testInit() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.init();
		assertEquals(distributor.TimeoutTime, testObject.getRampDownUpdateSeconds());
		assertEquals(distributor.TimeoutTimerID, testObject.getTimerID());
		assertEquals(1, distributor.TimeoutCalls);
	}
	
	@Test
	public void testUpdated() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		testObject.updated(new BaseDataC(1, 2));
		assertEquals(distributor.RAMPING_DOWN, testObject.getState());
	}
	
	@Test(expected=RuntimeException.class)
	public void testUpdatedException() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.updated(new BaseDataC(1, 2));
		fail("Expected an exepction before this.");
	}
	
	@Test
	public void testEvicted() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		testObject.evicted(new BaseDataC(1, 2));
		assertEquals(distributor.RAMPING_DOWN, testObject.getState());
	}
	
	@Test
	public void testRemoved() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		testObject.removed(12345L);
		assertEquals(distributor.RAMPING_DOWN, testObject.getState());
	}
	
	@Test
	public void testTimeoutTerminateID() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		testObject.timeout(testObject.getTerminateID());
		assertEquals(testObject.TERMINATING, testObject.getState());
	}
	
	@Test(expected=RuntimeException.class)
	public void testTimeoutWrongID() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		long id = 0;
		while( id == testObject.getTimerID() || id == testObject.getTerminateID() ) {
			id++;
		}
		testObject.timeout(id);
		fail("Should throw exception.");
	}
	
	@Test
	public void testTimeoutTimeoutOK() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		distributor.nukesReady = true;
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.RAMPING_DOWN, testObject.getState());
		assertEquals(1, distributor.TimeoutCalls);
	}
	
	@Test(expected=RuntimeException.class)
	public void testFinishedWrongState() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		testObject.finished(1L, 2L, testObject.ABORTED, "haha", "hoho");
		fail("Should throw exception.");
	}
	
	@Test
	public void testFinishedTerminating() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.TERMINATING);
		testObject.finished(1L, 2L, testObject.TERMINATING, "haha", "hoho");
		assertEquals(testObject.TERMINATING, testObject.getState());
	}
	
	@Test(expected=RuntimeException.class)
	public void testFinishedUnknownID() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		testObject.finished(1L, 2L, testObject.COMPLETED, "haha", "hoho");
		fail("Should throw exception.");
	}
	
	@Test
	public void testFinishedActiveID() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		testObject.getActiveNukeCommands().add(1L);
		distributor.timeoutActive = true;
		testObject.finished(1L, 2L, testObject.COMPLETED, "haha", "hoho");
		assertEquals(0, distributor.TimeoutCalls);
	}
	
	@Test
	public void testFinishedActiveIDNotActive() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		testObject.getActiveNukeCommands().add(1L);
		distributor.timeoutActive = false;
		testObject.finished(1L, 2L, testObject.COMPLETED, "haha", "hoho");
		assertEquals(distributor.TimeoutTime, testObject.getRampDownUpdateSeconds());
		assertEquals(distributor.TimeoutTimerID, testObject.getTimerID());
		assertEquals(1, distributor.TimeoutCalls);
	}
	
	@Test
	public void handleTimerTickOneNuke() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		theContext.addNuke(1L, new NukeInfoMock(5, NukeState.RUNNING));
		distributor.timeoutActive = true;
		testObject.timeout(testObject.getTimerID());
		assertEquals(0, distributor.TimeoutCalls);
	}
	
	@Test
	public void handleTimerTickSecondCall() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		theContext.addNuke(1L, new NukeInfoMock(5, NukeState.RUNNING));
		testObject.setOldUsers(1);
		testObject.setDoOnce(false);
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.COMPLETED, testObject.getState());
	}
	
	@Test
	public void handleTimerTickSecondCallNoUsers() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		theContext.addNuke(1L, new NukeInfoMock(5, NukeState.RUNNING));
		testObject.setOldUsers(0);
		testObject.setDoOnce(false);
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.COMPLETED, testObject.getState());
	}
	
	@Test
	public void handleTimerTickSecondCallStillUsers() {
		RampingDown testObject = new RampingDown(distributor);
		testObject.setState(testObject.RAMPING_DOWN);
		theContext.addNuke(1L, new NukeInfoMock(5, NukeState.RUNNING));
		testObject.setOldUsers(5);
		testObject.getActiveNukeCommands().add(1L);
		testObject.setDoOnce(false);
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.RAMPING_DOWN, testObject.getState());
		assertEquals(distributor.TimeoutTime, testObject.getRampDownUpdateSeconds());
		assertEquals(distributor.TimeoutTimerID, testObject.getTimerID());
		assertEquals(1, distributor.TimeoutCalls);
	}

}
