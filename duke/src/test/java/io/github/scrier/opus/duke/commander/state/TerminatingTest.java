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

public class TerminatingTest {

	private static TestHelper theHelper = TestHelper.INSTANCE;

	private HazelcastInstance instance;
	private long identity = theHelper.getNextLong();
	private long sagaID = theHelper.getNextLong();
	private long timerID = theHelper.getNextLong();
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
		theHelper.mockIdGen(instance, Shared.Hazelcast.COMMON_SAGA_ID, sagaID);
		theHelper.mockIdGen(instance, Shared.Hazelcast.COMMON_UNIQUE_ID, timerID);
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
		Terminating testObject = new Terminating(distributor);
		assertEquals(0, testObject.getActiveNukeCommands().size());
		assertEquals(30, testObject.getTerminateTimeout());
		assertEquals("Terminating", testObject.getClassName());
	}
	
	@Test
	public void testInit() {
		Terminating testObject = new Terminating(distributor);
		testObject.init();
		assertEquals(distributor.TimeoutTime, testObject.getTerminateTimeout());
		assertEquals(distributor.TimeoutTimerID, testObject.getTimerID());
		assertEquals(1, distributor.TimeoutCalls);
		assertEquals(0, testObject.getActiveNukeCommands().size());
	}
	
	@Test
	public void testInitNukes() {
		Terminating testObject = new Terminating(distributor);
		theContext.addNuke(1L, new NukeInfoMock(4, NukeState.RUNNING));
		testObject.init();
		assertEquals(distributor.TimeoutTime, testObject.getTerminateTimeout());
		assertEquals(distributor.TimeoutTimerID, testObject.getTimerID());
		assertEquals(1, distributor.TimeoutCalls);
		assertEquals(1, testObject.getActiveNukeCommands().size());
	}
	
	@Test
	public void testUpdated() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		testObject.updated(new BaseDataC(1, 2));
		assertEquals(distributor.TERMINATING, testObject.getState());
	}
	
	@Test(expected=RuntimeException.class)
	public void testUpdatedException() {
		Terminating testObject = new Terminating(distributor);
		testObject.updated(new BaseDataC(1, 2));
		fail("Expected an exepction before this.");
	}
	
	@Test
	public void testEvicted() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		assertEquals(testObject.TERMINATING, distributor.getState());
		assertEquals(testObject.TERMINATING, testObject.getState());
		testObject.evicted(new BaseDataC(1, 2));
		assertEquals(distributor.TERMINATING, testObject.getState());
	}
	
	@Test
	public void testRemoved() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		testObject.removed(12345L);
		assertEquals(distributor.TERMINATING, testObject.getState());
	}
	
	@Test(expected=RuntimeException.class)
	public void testTimeoutTerminateID() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		testObject.timeout(testObject.getTerminateID());
		fail("Should throw exception.");
	}
	
	@Test(expected=RuntimeException.class)
	public void testTimeoutWrongID() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		long id = 0;
		while( id == testObject.getTimerID() || id == testObject.getTerminateID() ) {
			id++;
		}
		testObject.timeout(id);
		fail("Should throw exception.");
	}
	
	@Test(expected=RuntimeException.class)
	public void testTimeoutTimeoutOK() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		distributor.nukesReady = true;
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.TERMINATING, testObject.getState());
		assertEquals(1, distributor.TimeoutCalls);
	}
	
	@Test(expected=RuntimeException.class)
	public void testFinishedWrongState() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		testObject.finished(1L, 2L, testObject.ABORTED, "haha", "hoho");
		fail("Should throw exception.");
	}
	
	@Test(expected=RuntimeException.class)
	public void testFinishedTerminating() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		testObject.finished(1L, 2L, testObject.TERMINATING, "haha", "hoho");
		assertEquals(testObject.TERMINATING, testObject.getState());
	}
	
	@Test(expected=RuntimeException.class)
	public void testFinishedUnknownID() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		testObject.finished(1L, 2L, testObject.COMPLETED, "haha", "hoho");
		fail("Should throw exception.");
	}
	
	@Test
	public void testFinishedActiveID() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		testObject.getActiveNukeCommands().add(1L);
		distributor.timeoutActive = true;
		testObject.finished(1L, 2L, testObject.COMPLETED, "haha", "hoho");
		assertEquals(0, distributor.TimeoutCalls);
		assertEquals(testObject.COMPLETED, testObject.getState());
	}
	
	@Test
	public void testFinishedMoreID() {
		Terminating testObject = new Terminating(distributor);
		testObject.setState(testObject.TERMINATING);
		testObject.getActiveNukeCommands().add(1L);
		testObject.getActiveNukeCommands().add(2L);
		distributor.timeoutActive = true;
		testObject.finished(1L, 2L, testObject.COMPLETED, "haha", "hoho");
		assertEquals(0, distributor.TimeoutCalls);
		assertEquals(testObject.TERMINATING, testObject.getState());
	}

}
