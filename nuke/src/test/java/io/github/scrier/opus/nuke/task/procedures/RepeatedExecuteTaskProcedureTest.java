package io.github.scrier.opus.nuke.task.procedures;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;

import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeExecuteReqMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;
import io.github.scrier.opus.common.nuke.NukeStopAllReqMsgC;
import io.github.scrier.opus.common.nuke.NukeStopAllRspMsgC;
import io.github.scrier.opus.common.nuke.NukeTerminateAllReqMsgC;
import io.github.scrier.opus.common.nuke.NukeTerminateAllRspMsgC;
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

public class RepeatedExecuteTaskProcedureTest {

	private static Logger log = LogManager.getLogger(RepeatedExecuteTaskProcedureTest.class);
	
	private static TestHelper helper = TestHelper.INSTANCE;

	private HazelcastInstance instance;
	private long identity = 8239422L;
	private static Context theContext = Context.INSTANCE;
	private BaseActiveObjectMock theBaseAOC;
	@SuppressWarnings("rawtypes")
  private IMap theMap;
	@SuppressWarnings("rawtypes")
  private IMap settingsMap;
	private NukeExecuteReqMsgC command;
	private MessageServiceMock SendIF;
	private long processID = 123754L;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		helper.setLogLevel(Level.TRACE);
	}

  @Before
	public void setUp() throws Exception {
		instance = helper.mockHazelcast();
		helper.mockIdGen(instance, Shared.Hazelcast.COMMON_MAP_UNIQUE_ID, ++identity);
		helper.mockIdGen(instance, Shared.Hazelcast.COMMON_UNIQUE_ID, processID);
		theMap = helper.mockMap(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		settingsMap = helper.mockMap(instance, Shared.Hazelcast.SETTINGS_MAP);
		Mockito.when(settingsMap.containsKey(any())).thenReturn(false);
		theBaseAOC = new BaseActiveObjectMock(instance);
		theBaseAOC.preInit();
		SendIF = new MessageServiceMock();
		theBaseAOC.setMsgService(SendIF);
		theContext.init(new NukeTasks(instance), theBaseAOC);
		command = new NukeExecuteReqMsgC();
		command.setCommand("sleep 1");
		command.setRepeated(true);
	}

	@After
	public void tearDown() throws Exception {
		theContext.shutDown();
		command = null;
		theMap = null;
		instance = null;
		theBaseAOC = null;
		command = null;
		SendIF.clear();
		SendIF = null;
		System.gc();
	}

	@Test
	public void testConstructor() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(command.getCommand(), testObject.getCommand());
		assertCommands(testObject, 0, 0, 0);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testInit() throws Exception {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		SendIF.waitForMessages(2);
		assertEquals(2, SendIF.getMessages().size());
		CommonCheck.assertNukeExecuteIndMsgC(SendIF.getMessage(1), CommandState.WORKING, processID);
		assertCommands(testObject, 1, 0, 1);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testInitOK() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		SendIF.waitForMessages(2);
		assertEquals(2, SendIF.getMessages().size());
		CommonCheck.assertNukeExecuteIndMsgC(SendIF.getMessage(1), CommandState.WORKING, processID);
		assertCommands(testObject, 1, 0, 1);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testWaitForSecondIterate() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		int timeout = 3;
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		SendIF.clear();
		while( testObject.RUNNING != testObject.getState() && timeout-- > 0 ) {
			Thread.sleep(10); // force taskswitch
		}
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(1, SendIF.getMessages().size());
		CommonCheck.assertNukeExecuteIndMsgC(SendIF.getMessage(0), CommandState.WORKING, processID);
		timeout = 15;
		while( 1 != testObject.getCompletedCommands() && timeout-- > 0 ) {
			Thread.sleep(200);
		}
		assertEquals(1, testObject.getCompletedCommands());
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(1, SendIF.getMessages().size());
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testWaitForComplete() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		SendIF.waitForMessages(2);
		assertEquals(2, SendIF.getMessages().size());
		SendIF.clear();
		testObject.stopProcess();
		SendIF.waitForMessages(1);
		assertEquals(1, SendIF.getMessages().size());
		CommonCheck.assertNukeExecuteIndMsgC(SendIF.getMessage(0), CommandState.DONE, processID);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testWaitForTerminate() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		SendIF.waitForMessages(2);
		SendIF.clear();
		testObject.terminateProcess();
		SendIF.waitForMessages(1);
		assertEquals(1, SendIF.getMessages().size());
		assertEquals(testObject.ABORTED, testObject.getState());
		CommonCheck.assertNukeExecuteIndMsgC(SendIF.getMessage(0), CommandState.ABORTED, processID);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testWaitUnhandledCommand() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		command.setCommand("dir");
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		SendIF.clear();
		int timeout = 15;
		while( 1 > testObject.getCompletedCommands() && timeout-- > 0 ) {
			Thread.sleep(200);
		}
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(2, SendIF.getMessages().size());
		CommonCheck.assertNukeExecuteIndMsgC(SendIF.getMessage(1), CommandState.ABORTED, processID);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testShutDown() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		SendIF.clear();
		int timeout = 5;
		while( testObject.ABORTED != testObject.getState() && timeout-- > 0 ) {
			Thread.sleep(10); // force taskswitch
		}
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(1, SendIF.getMessages().size());
		CommonCheck.assertNukeExecuteIndMsgC(SendIF.getMessage(0), CommandState.WORKING, processID);
		assertCommands(testObject, 1, 0, 1);
		testObject.setState(testObject.COMPLETED);
		testObject.shutDown();
		assertCommands(testObject, 0, 1, 1);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test(expected=RuntimeException.class)
	public void testShutDownException() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		SendIF.clear();
		int timeout = 5;
		while( testObject.ABORTED != testObject.getState() && timeout-- > 0 ) {
			Thread.sleep(10); // force taskswitch
		}
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(1, SendIF.getMessages().size());
		CommonCheck.assertNukeExecuteIndMsgC(SendIF.getMessage(0), CommandState.WORKING, processID);
		assertCommands(testObject, 1, 0, 1);
		testObject.shutDown();
		assertCommands(testObject, 0, 1, 1);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testhandleOnUpdated() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
//		testObject.handleOnUpdated(command);
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(command.getCommand(), testObject.getCommand());
		assertCommands(testObject, 0, 0, 0);
		testObject = null;
	}
	
	@Test
	public void testhandleOnEvicted() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(command.getCommand(), testObject.getCommand());
		assertCommands(testObject, 0, 0, 0);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testhandleOnEvictedFinished() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.setState(testObject.COMPLETED);
//		testObject.handleOnEvicted(command);
		assertEquals(testObject.COMPLETED, testObject.getState());
		assertEquals(command.getCommand(), testObject.getCommand());
		assertCommands(testObject, 0, 0, 0);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testhandleOnRemoved() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(command.getCommand(), testObject.getCommand());
		assertCommands(testObject, 0, 0, 0);
		testObject.cleanUp();
		testObject = null;
	}
	
	@Test
	public void testhandleOnRemovedFinished() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.setState(testObject.COMPLETED);
		assertEquals(testObject.COMPLETED, testObject.getState());
		assertEquals(command.getCommand(), testObject.getCommand());
		assertCommands(testObject, 0, 0, 0);
		testObject.cleanUp();
		testObject = null;
	}
	
	/**
	 * Common test methods
	 * @param testObject ExecuteTaskProcedure instance
	 * @param expectedActive active commands expected
	 * @param expectedCompleted completed commands expected
	 * @param expectedRequested requested commands expected
	 */
	private void assertCommands(RepeatedExecuteTaskProcedure testObject, int expectedActive, int expectedCompleted, int expectedRequested) {
	  assertEquals(expectedActive, testObject.getNukeInfo().getActiveCommands());
		assertEquals(expectedCompleted, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(expectedRequested, testObject.getNukeInfo().getRequestedCommands());
  }

}
