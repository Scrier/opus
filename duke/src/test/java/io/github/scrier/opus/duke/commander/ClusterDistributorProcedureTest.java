package io.github.scrier.opus.duke.commander;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.scrier.opus.TestHelper;
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
		assertEquals(5, testObject.getWaitingForNukeUpdateSeconds());
		assertEquals(5, testObject.getRampDownUpdateSeconds());
		assertEquals(false, testObject.isRepeated());
		assertEquals(true, testObject.isShutDownOnce());
		assertEquals("", testObject.getCommand());
		assertEquals("", testObject.getFolder());
		assertEquals(0, testObject.getLocalUserRampedUp());
		assertEquals(-1L, testObject.getTimerID());
		assertEquals(-1L, testObject.getTerminateID());
	}
	
	@Test
	public void testGetDistributionSuggestionEmpty0User() throws Exception {
		ClusterDistributorProcedure testObject = new ClusterDistributorProcedure();
		Map<Long, Integer> check = testObject.getDistributionSuggestion(0);
		assertNull(check);
	}
	
	@Test
	public void testGetDistributionSuggestionEmpty5User() throws Exception {
		ClusterDistributorProcedure testObject = new ClusterDistributorProcedure();
		Map<Long, Integer> check = testObject.getDistributionSuggestion(5);
		assertNull(check);
	}
	
	@Test
	public void testGetDistributionSuggestion5WrongState0User() throws Exception {
		addNukeInfoObjects(5, NukeState.AVAILABLE);
		ClusterDistributorProcedure testObject = new ClusterDistributorProcedure();
		Map<Long, Integer> check = testObject.getDistributionSuggestion(0);
		assertNull(check);
	}
	
	@Test
	public void testGetDistributionSuggestion5WrongState5User() throws Exception {
		addNukeInfoObjects(5, NukeState.AVAILABLE);
		ClusterDistributorProcedure testObject = new ClusterDistributorProcedure();
		Map<Long, Integer> check = testObject.getDistributionSuggestion(5);
		assertNull(check);
	}
	
	@Test
	public void testGetDistributionSuggestion1CorrectState0User() throws Exception {
		addNukeInfoObjects(1);
		ClusterDistributorProcedure testObject = new ClusterDistributorProcedure();
		Map<Long, Integer> check = testObject.getDistributionSuggestion(0);
		assertNotNull(check);
		assertEquals(0, check.size());
	}
	
	@Test
	public void testGetDistributionSuggestion1CorrectState5User() throws Exception {
		List<Long> list = addNukeInfoObjects(1);
		ClusterDistributorProcedure testObject = new ClusterDistributorProcedure();
		Map<Long, Integer> check = testObject.getDistributionSuggestion(5);
		assertNotNull(check);
		assertEquals(1, check.size());
		assertEquals(1, list.size());
		assertEquals(5, check.get(list.get(0)).intValue());
	}
	
	@Test
	public void testGetDistributionSuggestion5CorrectState0User() throws Exception {
		addNukeInfoObjects(5);
		ClusterDistributorProcedure testObject = new ClusterDistributorProcedure();
		Map<Long, Integer> check = testObject.getDistributionSuggestion(0);
		assertNotNull(check);
		assertEquals(0, check.size());
	}
	
	@Test
	public void testGetDistributionSuggestion5CorrectState5User() throws Exception {
		List<Long> list = addNukeInfoObjects(5);
		ClusterDistributorProcedure testObject = new ClusterDistributorProcedure();
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
	
	/**
	 * Helper class
	 */
	static long nukeIdCounter = 0;
	public class NukeInfoMock implements INukeInfo {
		
		public long nukeIdReturn;
		public int noOfUsersReturn;
		public int requestedNoOfUsersReturned;
		public NukeState infoStateReturned;
		public int noOfActiveCommandsReturned;
		public int noOfRequestedCommandsReturned;
		public int noOfCompletedCommandsReturned;
		
		public NukeInfoMock(int requestedNoOfUsers) {
			this(requestedNoOfUsers, NukeState.RUNNING);
		}
		
		public NukeInfoMock(int requestedNoOfUsers, NukeState infoState) {
			this.requestedNoOfUsersReturned = requestedNoOfUsers;
			this.infoStateReturned = infoState;
			this.nukeIdReturn = ++nukeIdCounter;
    }

		@Override
    public long getNukeID() {
	    return nukeIdReturn;
    }

		@Override
    public int getNoOfUsers() {
	    return noOfUsersReturn;
    }

		@Override
    public int getRequestedNoOfUsers() {
	    return requestedNoOfUsersReturned;
    }

		@Override
    public NukeState getInfoState() {
	    return infoStateReturned;
    }

		@Override
    public int getNoOfActiveCommands() {
	    return noOfActiveCommandsReturned;
    }

		@Override
    public int getNoOfRequestedCommands() {
	    return noOfRequestedCommandsReturned;
    }

		@Override
    public int getNoOfCompletedCommands() {
	    return noOfCompletedCommandsReturned;
    }
		
	}

}
