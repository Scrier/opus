package io.github.scrier.opus.duke.commander;

import static org.junit.Assert.*;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.common.nuke.NukeState;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class NukeProcedureTest {
	
	private static TestHelper theHelper;
	
	private long identity = 489234L;
	
	private HazelcastInstance instance;
	private Context theContext = Context.INSTANCE;
	private BaseActiveObjectMock theBaseAOC;
	@SuppressWarnings("rawtypes")
  private IMap theMap;
	private NukeInfo info;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		theHelper = TestHelper.INSTANCE;
		theHelper.setLogLevel(Level.TRACE);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		instance = theHelper.mockHazelcast();
		theHelper.mockIdGen(instance, Shared.Hazelcast.COMMON_MAP_UNIQUE_ID, identity);
		theMap = theHelper.mockMap(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		theBaseAOC = new BaseActiveObjectMock(instance);
		theBaseAOC.preInit();
		theContext.init(new DukeCommander(instance), theBaseAOC);
		info = new NukeInfo();
		info.setActiveCommands(123);
		info.setCompletedCommands(120);
		info.setNukeID(identity);
		info.setNumberOfUsers(34);
		info.setRepeated(true);
		info.setRequestedCommands(125);
		info.setRequestedUsers(35);
		info.setState(NukeState.RUNNING);
		info.setTxID(1);
	}

	@After
	public void tearDown() throws Exception {
		theContext.shutDown();
		info = null;
	}

	@Test
	public void testConstructor() {
		NukeProcedure testObject = new NukeProcedure(info);
		assertEquals(identity, testObject.getIdentity());
		assertEquals(info.getState(), testObject.getInfoState());
		assertEquals(info.getActiveCommands(), testObject.getNoOfActiveCommands());
		assertEquals(info.getCompletedCommands(), testObject.getNoOfCompletedCommands());
		assertEquals(info.getRequestedCommands(), testObject.getNoOfRequestedCommands());
		assertEquals(info.getNumberOfUsers(), testObject.getNoOfUsers());
		assertEquals(info.getRequestedUsers(), testObject.getRequestedNoOfUsers());
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(1, testObject.getTxID());
	}
	
	@Test
	public void testInit() throws Exception {
		NukeProcedure testObject = new NukeProcedure(info);
		testObject.init();
		assertEquals(identity, testObject.getIdentity());
		assertEquals(info.getState(), testObject.getInfoState());
		assertEquals(info.getActiveCommands(), testObject.getNoOfActiveCommands());
		assertEquals(info.getCompletedCommands(), testObject.getNoOfCompletedCommands());
		assertEquals(info.getRequestedCommands(), testObject.getNoOfRequestedCommands());
		assertEquals(info.getNumberOfUsers(), testObject.getNoOfUsers());
		assertEquals(info.getRequestedUsers(), testObject.getRequestedNoOfUsers());
		assertEquals(testObject.INITIALIZING, testObject.getState());
		assertEquals(1, testObject.getTxID());
	}
	
	@Test
	public void testInitAlreadyExist() throws Exception {
		NukeProcedure testObject = new NukeProcedure(info);
		theContext.addNuke(identity, testObject);
		testObject.init();
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test(expected=NullPointerException.class)
	public void testShutDown() throws Exception {
		NukeProcedure testObject = new NukeProcedure(info);
		testObject.init();
		testObject.shutDown();
		testObject.getNoOfActiveCommands();
		fail("Should throw exception above,");
	}
	
	@Test(expected=InvalidOperationException.class)
	public void testShutDownInvalidOperation() throws Exception {
		NukeProcedure testObject = new NukeProcedure(info);
		testObject.shutDown();
		fail("Should throw exception above,");
	}
	
	@Test
	public void testOnUpdateCommand() throws Exception {
		NukeProcedure testObject = new NukeProcedure(info);
		testObject.init();
		testObject.handleOnUpdated(new NukeCommand());
		assertEquals(testObject.INITIALIZING, testObject.getState());
	}

}
