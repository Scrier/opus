package io.github.scrier.opus.duke.commander.state;

import static org.junit.Assert.*;
import io.github.scrier.opus.StateImpl;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.ClusterDistributorProcedureTestObj;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.duke.commander.BaseActiveObjectMock;
import io.github.scrier.opus.duke.commander.CommandProcedure;
import io.github.scrier.opus.duke.commander.Context;
import io.github.scrier.opus.duke.commander.DukeCommander;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class StateTest {

	private static TestHelper theHelper = TestHelper.INSTANCE;

	private HazelcastInstance instance;
	private long identity = 824951L;
	private long sagaID = 847L;
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
		State testObject = new StateImpl(distributor);
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
		assertEquals(distributor.CREATED, testObject.getState());
		assertEquals("StateImpl", testObject.getClassName());
	}
	
	@Test
	public void testInit() {
		State testObject = new StateImpl(distributor);
		testObject.init();
		assertEquals(testObject.CREATED, testObject.getState());
	}
	
	@Test
	public void testEvicted() {
		State testObject = new StateImpl(distributor);
		testObject.evicted(new BaseDataC(1,  2));
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testRemoved() {
		State testObject = new StateImpl(distributor);
		testObject.removed(1234L);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testTimeout() {
		State testObject = new StateImpl(distributor);
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testUpdated() {
		State testObject = new StateImpl(distributor);
		testObject.updated(new BaseDataC(1,  2));
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testTimer() {
		State testObject = new StateImpl(distributor);
		testObject.startTimeout(25, 12345L);
		assertEquals(25, distributor.TimeoutTime);
		assertEquals(12345L, distributor.TimeoutTimerID);
		assertEquals(1, distributor.TimeoutCalls);
	}
	
	@Test
	public void testRegister() {
		State testObject = new StateImpl(distributor);
		testObject.registerProcedure(new CommandProcedure(1234L, "this is command"));
		assertFalse(theContext.getCommander().getProceduresToAdd(CommandProcedure.class).isEmpty());
	}

}
