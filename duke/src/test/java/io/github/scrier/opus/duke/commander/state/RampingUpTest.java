package io.github.scrier.opus.duke.commander.state;

import static org.junit.Assert.*;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.nuke.NukeState;
import io.github.scrier.opus.duke.commander.BaseActiveObjectMock;
import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;
import io.github.scrier.opus.duke.commander.Context;
import io.github.scrier.opus.duke.commander.DukeCommander;
import io.github.scrier.opus.duke.commander.INukeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	private long identity = 82345L;
	private long component = 23581L;
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
		assertEquals(distributor.getTimerID(), testObject.getTimerID());
		assertEquals(distributor.getUserIncrease(), testObject.getUserIncrease());
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

		@Override
    public void setRequestedNoOfUsers(int users) {
	    this.requestedNoOfUsersReturned = users;
    }
		
	}

}
