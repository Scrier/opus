package io.github.scrier.opus.duke.commander.state;

import static org.junit.Assert.*;
import io.github.scrier.opus.ClusterDistributorProcedureTestObj;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.nuke.NukeState;
import io.github.scrier.opus.duke.commander.BaseActiveObjectMock;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;
import io.github.scrier.opus.duke.commander.Context;
import io.github.scrier.opus.duke.commander.DukeCommander;
import io.github.scrier.opus.duke.commander.INukeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class RampingUpTest {
	
	private static TestHelper theHelper = TestHelper.INSTANCE;

	private HazelcastInstance instance;
	private long identity = theHelper.getNextLong();
	private long sagaID = theHelper.getNextLong();
	private long component = theHelper.getNextLong();
	private Context theContext = Context.INSTANCE;
	private BaseActiveObjectMock theBaseAOC;
	@SuppressWarnings("rawtypes")
	private IMap theMap;
	private ClusterDistributorProcedure distributor;

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
		RampingUp testObject = new RampingUp(distributor);
		assertEquals(distributor.getFolder(), testObject.getFolder());
		assertEquals(distributor.getCommand(), testObject.getCommand());
		assertEquals(RampingUp.DEFAULT_INTERVAL_SECONDS, testObject.getIntervalSeconds());
		assertEquals(0, testObject.getLocalUserRampedUp());
		assertEquals(distributor.getMaxUsers(), testObject.getMaxUsers());
		assertEquals(distributor.getPeakDelaySeconds(), testObject.getPeakDelaySeconds());
		assertEquals(distributor.getState(), testObject.getState());
		assertEquals(distributor.getTerminateID(), testObject.getTerminateID());
		assertEquals(distributor.getUserIncrease(), testObject.getUserIncrease());
	}
	
	@Test
	public void testInit() throws Exception {
		ClusterDistributorProcedureTestObj distrib = theHelper.getRandomDistributor();
		RampingUp testObject = new RampingUp(distrib);
		testObject.init();
		assertEquals(distrib.TimeoutTime, testObject.getIntervalSeconds());
		assertEquals(distrib.TimeoutTimerID, testObject.getTimerID());
		assertEquals(1, distrib.TimeoutCalls);
	}
	
	@Test
	public void testUpdated() {
		RampingUp testObject = new RampingUp(distributor);
		testObject.setState(testObject.RAMPING_UP);
		testObject.updated(new BaseDataC(1, 2));
		assertEquals(distributor.RAMPING_UP, testObject.getState());
	}
	
	@Test(expected=RuntimeException.class)
	public void testUpdatedException() {
		RampingUp testObject = new RampingUp(distributor);
		testObject.updated(new BaseDataC(1, 2));
		fail("Expected an exepction before this.");
	}
	
	@Test
	public void testEvicted() {
		RampingUp testObject = new RampingUp(distributor);
		testObject.setState(testObject.RAMPING_UP);
		testObject.evicted(new BaseDataC(1, 2));
		assertEquals(distributor.RAMPING_UP, testObject.getState());
	}
	
	@Test
	public void testRemoved() {
		RampingUp testObject = new RampingUp(distributor);
		testObject.setState(testObject.RAMPING_UP);
		testObject.removed(12345L);
		assertEquals(distributor.RAMPING_UP, testObject.getState());
	}
	
	@Test
	public void testTimeoutTerminateID() {
		RampingUp testObject = new RampingUp(distributor);
		testObject.setState(testObject.RAMPING_UP);
		testObject.timeout(testObject.getTerminateID());
		assertEquals(testObject.TERMINATING, testObject.getState());
	}
	
	@Test(expected=RuntimeException.class)
	public void testTimeoutWrongID() {
		RampingUp testObject = new RampingUp(distributor);
		testObject.setState(testObject.RAMPING_UP);
		long id = 0;
		while( id == testObject.getTimerID() || id == testObject.getTerminateID() ) {
			id++;
		}
		testObject.timeout(id);
		fail("Should throw exception");
	}
	
	@Test
	public void testTimeoutTimeoutOK() throws Exception {
		ClusterDistributorProcedureTestObj distrib = theHelper.getRandomDistributor();
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setMinNodes", int.class, distrib, 1);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setMaxUsers", int.class, distrib, 5);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setIntervalSeconds", int.class, distrib, 2);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setUserIncrease", int.class, distrib, 2);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setPeakDelaySeconds", int.class, distrib, 10);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setTerminateSeconds", int.class, distrib, 20);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setRepeated", boolean.class, distrib, true);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setShutDownOnce", boolean.class, distrib, true);
		theContext.addNuke(1L, new NukeInfoMock(2, NukeState.RUNNING));
		RampingUp testObject = new RampingUp(distrib);
		testObject.setState(testObject.RAMPING_UP);
		distrib.nukesReady = true;
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.RAMPING_UP, testObject.getState());
		assertEquals(1, distrib.TimeoutCalls);
	}
	
	@Test
	public void testTimeoutTimeoutChangeState() throws Exception {
		ClusterDistributorProcedureTestObj distrib = theHelper.getRandomDistributor();
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setMinNodes", int.class, distrib, 1);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setMaxUsers", int.class, distrib, 5);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setIntervalSeconds", int.class, distrib, 2);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setUserIncrease", int.class, distrib, 2);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setPeakDelaySeconds", int.class, distrib, 10);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setTerminateSeconds", int.class, distrib, 20);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setRepeated", boolean.class, distrib, true);
		theHelper.invokeSingleArg(ClusterDistributorProcedure.class, "setShutDownOnce", boolean.class, distrib, true);
		theContext.addNuke(1L, new NukeInfoMock(2, NukeState.RUNNING));
		RampingUp testObject = new RampingUp(distrib);
		testObject.setState(testObject.RAMPING_UP);
		distrib.nukesReady = true;
		testObject.setLocalUserRampedUp(5);
		testObject.timeout(testObject.getTimerID());
		assertEquals(testObject.PEAK_DELAY, testObject.getState());
	}
	
	@Test
	public void testGetDistributionSuggestionEmpty0User() throws Exception {
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(0);
		assertNull(check);
	}

	@Test
	public void testGetDistributionSuggestionEmpty5User() throws Exception {
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(5);
		assertNull(check);
	}

	@Test
	public void testGetDistributionSuggestion5WrongState0User() throws Exception {
		addNukeInfoObjects(5, NukeState.AVAILABLE);
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(0);
		assertNull(check);
	}

	@Test
	public void testGetDistributionSuggestion5WrongState5User() throws Exception {
		addNukeInfoObjects(5, NukeState.AVAILABLE);
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(5);
		assertNull(check);
	}

	@Test
	public void testGetDistributionSuggestion1CorrectState0User() throws Exception {
		addNukeInfoObjects(1);
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(0);
		assertNotNull(check);
		assertEquals(0, check.size());
	}

	@Test
	public void testGetDistributionSuggestion1CorrectState5User() throws Exception {
		List<Long> list = addNukeInfoObjects(1);
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(5);
		assertNotNull(check);
		assertEquals(1, check.size());
		assertEquals(1, list.size());
		assertEquals(5, check.get(list.get(0)).intValue());
	}
	
	@Test
	public void testGetDistributionSuggestion5CorrectState50User() throws Exception {
		List<Long> list = addNukeInfoObjects(5);
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(50);
		assertNotNull(check);
		assertEquals(5, check.size());
		assertEquals(5, list.size());
		assertEquals(10, check.get(list.get(0)).intValue());
		assertEquals(10, check.get(list.get(1)).intValue());
		assertEquals(10, check.get(list.get(2)).intValue());
		assertEquals(10, check.get(list.get(3)).intValue());
		assertEquals(10, check.get(list.get(4)).intValue());
	}
	
	@Test
	public void testGetDistributionSuggestion5CorrectState5User5Times() throws Exception {
		List<Long> list = addNukeInfoObjects(5);
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(3);
		assertNotNull(check);
		assertEquals(3, check.size());
		assertEquals(5, list.size());
		for( Long key : list ) {
			if( check.containsKey(key) ) {
				assertEquals(1, check.get(key).intValue());
			}
		}
		check = testObject.getDistributionSuggestion(3);
		assertEquals(3, check.size());
		for( Long key : list ) {
			if( check.containsKey(key) ) {
				assertEquals(1, check.get(key).intValue());
			}
		}
		check = testObject.getDistributionSuggestion(3);
		assertEquals(3, check.size());
		for( Long key : list ) {
			if( check.containsKey(key) ) {
				assertEquals(1, check.get(key).intValue());
			}
		}
		check = testObject.getDistributionSuggestion(3);
		assertEquals(3, check.size());
		for( Long key : list ) {
			if( check.containsKey(key) ) {
				assertEquals(1, check.get(key).intValue());
			}
		}
		check = testObject.getDistributionSuggestion(3);
		assertEquals(3, check.size());
		for( Long key : list ) {
			if( check.containsKey(key) ) {
				assertEquals(1, check.get(key).intValue());
			}
		}
		for( INukeInfo info : theContext.getNukes() ) {
			assertEquals(3, info.getRequestedNoOfThreads());
		}
	}
	
	@Test
	public void testGetDistributionSuggestion2CorrectState10User2Iterations() throws Exception {
		addNukeInfoObjects(2);
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(5);
		assertNotNull(check);
		assertEquals(2, check.size());
		long keyExpect3 = Constants.HC_UNDEFINED, keyExpect2 = Constants.HC_UNDEFINED;
		for( Map.Entry<Long, Integer> entry : check.entrySet() ) {
			if( 2 == entry.getValue() ) {
				keyExpect3 = entry.getKey();
			} else if ( 3 == entry.getValue() ) {
				keyExpect2 = entry.getKey();
			} else {
				fail("Should be balances between 2 and 3 for fair distribution.");
			}
		}
		assertNotEquals(Constants.HC_UNDEFINED, keyExpect2);
		assertNotEquals(Constants.HC_UNDEFINED, keyExpect3);
		check = testObject.getDistributionSuggestion(5);
		assertTrue(check.containsKey(keyExpect2));
		assertTrue(check.containsKey(keyExpect3));
		assertEquals(2, check.get(keyExpect2).intValue());
		assertEquals(3, check.get(keyExpect3).intValue());
	}

	@Test
	public void testGetDistributionSuggestion5CorrectState0User() throws Exception {
		addNukeInfoObjects(5);
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(0);
		assertNotNull(check);
		assertEquals(0, check.size());
	}

	@Test
	public void testGetDistributionSuggestion5CorrectState5User() throws Exception {
		List<Long> list = addNukeInfoObjects(5);
		RampingUp testObject = new RampingUp(distributor);
		Map<Long, Integer> check = testObject.getDistributionSuggestion(5);
		assertNotNull(check);
		assertEquals(5, check.size());
		assertEquals(5, list.size());
		for( Long l : list ) {
			assertEquals(1, check.get(l).intValue());
		}
	}
	
	/**
	 * Helper method
	 */
	List<Long> addNukeInfoObjects(int no) {
		return addNukeInfoObjects(no, NukeState.RUNNING);
	}
	
	List<Long> addNukeInfoObjects(int no, NukeState state) {
		ArrayList<Long> retValue = new ArrayList<Long>();
		for( int i = 0; i < no; i++ ) {
			NukeInfoMock mock = new NukeInfoMock(0, state);
			theContext.addNuke(mock.getNukeID(), mock);
			retValue.add(mock.getNukeID());
		}
		return retValue;
	}
	
}
