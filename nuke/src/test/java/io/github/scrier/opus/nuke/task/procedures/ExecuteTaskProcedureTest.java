package io.github.scrier.opus.nuke.task.procedures;

import static org.mockito.Matchers.any;
import static org.junit.Assert.*;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeExecuteIndMsgC;
import io.github.scrier.opus.common.nuke.NukeExecuteReqMsgC;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;
import io.github.scrier.opus.nuke.BaseActiveObjectMock;
import io.github.scrier.opus.nuke.task.Context;
import io.github.scrier.opus.nuke.task.NukeTasks;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ExecuteTaskProcedureTest {
	
	private static Logger log = LogManager.getLogger(ExecuteTaskProcedureTest.class);
	
	private static TestHelper helper = TestHelper.INSTANCE;

	private HazelcastInstance instance;
	private long identity = 8239421L;
	private Context theContext = Context.INSTANCE;
	private BaseActiveObjectMock theBaseAOC;
	@SuppressWarnings("rawtypes")
  private IMap theMap;
	private NukeExecuteReqMsgC command;
	private int txID = 2525;
	private MessageServiceMock SendIF;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		helper.setLogLevel(Level.TRACE);
	}

  @Before
	public void setUp() throws Exception {
		instance = helper.mockHazelcast();
		helper.mockIdGen(instance, Shared.Hazelcast.COMMON_MAP_UNIQUE_ID, identity);
		theMap = helper.mockMap(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		theBaseAOC = new BaseActiveObjectMock(instance);
		theBaseAOC.preInit();
		theContext.init(new NukeTasks(instance), theBaseAOC);
		SendIF = new MessageServiceMock();
		theBaseAOC.setMsgService(SendIF);
		command = new NukeExecuteReqMsgC();
		command.setDestination(identity);
		command.setCommand("sleep 2");
		command.setRepeated(false);
		command.setTxID(txID);
	}

	@After
	public void tearDown() throws Exception {
		theContext.shutDown();
		command = null;
		theMap = null;
		instance = null;
		theBaseAOC = null;
		command = null;
	}

	@Test
	public void testConstructor() {
		ExecuteTaskProcedure testObject = new ExecuteTaskProcedure(command);
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(command.getCommand(), testObject.getCommand());
		assertCommands(testObject, 0, 0, 0);
	}
	
	@Test
	public void testInit() throws Exception {
		ExecuteTaskProcedure testObject = new ExecuteTaskProcedure(command);
		testObject.init();
		int timeout = 100;
		while( timeout-- > 0 ) {
			Thread.sleep(10);
			if( 1 == SendIF.getMessages().size() ) break; // wait for the task switcing to occur.
		}
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(1, SendIF.getMessages().size());
		assertNukeExecuteIndMsgC(SendIF.getMessage(0), CommandState.WORKING, "");
		assertCommands(testObject, 1, 0, 1);
	}
	
	@Test
	public void testInitOK() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		ExecuteTaskProcedure testObject = new ExecuteTaskProcedure(command);
		testObject.init();
		Thread.sleep(1); // force taskswitch
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(1, SendIF.getMessages().size());
		assertNukeExecuteIndMsgC(SendIF.getMessage(0), CommandState.WORKING, "");
		assertCommands(testObject, 1, 0, 1);
	}
	
	@Test
	public void testWaitForComplete() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		ExecuteTaskProcedure testObject = new ExecuteTaskProcedure(command);
		testObject.init();
		int timeout = 100;
		while( timeout-- > 0 ) {
			Thread.sleep(10);
			if( 1 == SendIF.getMessages().size() ) break; // wait for the task switcing to occur.
		}
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(1, SendIF.getMessages().size());
		assertNukeExecuteIndMsgC(SendIF.getMessage(0), CommandState.WORKING, "");
		timeout = 400; // command is sleep 2, so that means I need to wait 10 * 400 to get 4 seconds.
		while( timeout-- > 0 ) {
			Thread.sleep(10);
			if( 2 == SendIF.getMessages().size() ) break; // wait for the task switching to occur.
		}
		assertEquals(2, SendIF.getMessages().size());
		assertNukeExecuteIndMsgC(SendIF.getMessage(1), CommandState.DONE, "");
		while( timeout-- > 0 ) {
			Thread.sleep(10);
			if( testObject.COMPLETED == testObject.getState() ) break; // wait for the task switching to occur.
		}
		assertEquals(testObject.COMPLETED, testObject.getState());
	}
	
	@Test
	public void testWaitUnhandledCommand() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		command.setCommand("dir");
		ExecuteTaskProcedure testObject = new ExecuteTaskProcedure(command);
		testObject.init();
		int timeout = 100;
		while( timeout-- > 0 ) {
			Thread.sleep(10);
			if( 2 == SendIF.getMessages().size() ) break; // wait for the task switcing to occur.
		}
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(2, SendIF.getMessages().size());
		assertNukeExecuteIndMsgC(SendIF.getMessage(1), CommandState.ABORTED, "");
	}
	
	@Test
	public void testShutDown() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		ExecuteTaskProcedure testObject = new ExecuteTaskProcedure(command);
		testObject.init();
		int timeout = 100;
		while( timeout-- > 0 ) {
			Thread.sleep(10);
			if( 2 == SendIF.getMessages().size() ) break; // wait for the task switcing to occur.
		}
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(1, SendIF.getMessages().size());
		assertNukeExecuteIndMsgC(SendIF.getMessage(0), CommandState.WORKING, "");
		assertCommands(testObject, 1, 0, 1);
		testObject.setState(testObject.COMPLETED);
		testObject.shutDown();
		assertCommands(testObject, 0, 1, 1);
	}
	
	@Test(expected=RuntimeException.class)
	public void testShutDownException() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		ExecuteTaskProcedure testObject = new ExecuteTaskProcedure(command);
		testObject.init();
		int timeout = 100;
		while( timeout-- > 0 ) {
			Thread.sleep(10);
			if( 2 == SendIF.getMessages().size() ) break; // wait for the task switcing to occur.
		}
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(1, SendIF.getMessages().size());
		assertNukeExecuteIndMsgC(SendIF.getMessage(0), CommandState.WORKING, "");
		assertCommands(testObject, 1, 0, 1);
		testObject.shutDown();
		assertCommands(testObject, 0, 1, 1);
	}
	
	@Test
	public void testhandleOnUpdated() {
		ExecuteTaskProcedure testObject = new ExecuteTaskProcedure(command);
		testObject.handleOnUpdated(new NukeInfo());
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(command.getCommand(), testObject.getCommand());
		assertCommands(testObject, 0, 0, 0);
	}
	
	@Test
	public void testhandleOnEvictedFinished() {
		ExecuteTaskProcedure testObject = new ExecuteTaskProcedure(command);
		testObject.setState(testObject.COMPLETED);
		testObject.handleOnEvicted(new NukeInfo());
		assertEquals(testObject.COMPLETED, testObject.getState());
		assertEquals(command.getCommand(), testObject.getCommand());
		assertCommands(testObject, 0, 0, 0);
	}
	
	@Test
	public void testhandleOnRemovedFinished() {
		ExecuteTaskProcedure testObject = new ExecuteTaskProcedure(command);
		testObject.setState(testObject.COMPLETED);
		testObject.handleOnRemoved(12345L);
		assertEquals(testObject.COMPLETED, testObject.getState());
		assertEquals(command.getCommand(), testObject.getCommand());
		assertCommands(testObject, 0, 0, 0);
	}
	
	/**
	 * Common test methods
	 * @param testObject ExecuteTaskProcedure instance
	 * @param expectedActive active commands expected
	 * @param expectedCompleted completed commands expected
	 * @param expectedRequested requested commands expected
	 */
	private void assertCommands(ExecuteTaskProcedure testObject, int expectedActive, int expectedCompleted, int expectedRequested) {
	  assertEquals(expectedActive, testObject.getNukeInfo().getActiveCommands());
		assertEquals(expectedCompleted, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(expectedRequested, testObject.getNukeInfo().getRequestedCommands());
  }
	
	/**
	 * Method to check the NukeExecuteInd Message
	 * @param msg BaseMsg to check
	 * @param expectedStatus expected status
	 * @param containsResponse contains response
	 */
	private void assertNukeExecuteIndMsgC(BaseMsgC msg, CommandState expectedStatus, String containsResponse) {
		assertEquals(NukeMsgFactory.FACTORY_ID, msg.getFactoryId());
		assertEquals(NukeMsgFactory.NUKE_EXECUTE_IND, msg.getId());
		NukeExecuteIndMsgC check = new NukeExecuteIndMsgC(msg);
		assertEquals(expectedStatus, check.getStatus());
		assertTrue(check.getResponse().contains(containsResponse));
	}

}
