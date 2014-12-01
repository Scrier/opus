package io.github.scrier.opus.nuke.task.procedures;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeCommand;
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
	private long identity = 8239421L;
	private Context theContext = Context.INSTANCE;
	private BaseActiveObjectMock theBaseAOC;
	@SuppressWarnings("rawtypes")
  private IMap theMap;
	private NukeCommand command;

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
		command = new NukeCommand();
		command.setComponent(identity);
		command.setCommand("sleep 2");
		command.setRepeated(true);
		command.setState(CommandState.EXECUTE);
		command.setKey(12345L);
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
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(command.getComponent(), testObject.getCommand().getComponent());
		assertEquals(command.getCommand(), testObject.getCommand().getCommand());
		assertEquals(command.isRepeated(), testObject.getCommand().isRepeated());
		assertEquals(command.getState(), testObject.getCommand().getState());
		assertEquals(command.getKey(), testObject.getCommand().getKey());
		assertEquals(0, testObject.getNukeInfo().getActiveCommands());
		assertEquals(0, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(0, testObject.getNukeInfo().getRequestedCommands());
	}
	
	@Test
	public void testInit() throws Exception {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		Thread.sleep(1000); // force taskswitch
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(CommandState.WORKING, testObject.getCommand().getState());
		assertEquals(1, testObject.getNukeInfo().getActiveCommands());
		assertEquals(0, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(1, testObject.getNukeInfo().getRequestedCommands());
	}
	
	@Test
	public void testInitOK() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		Thread.sleep(1); // force taskswitch
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(0, testObject.getCompletedCommands());
		assertEquals(CommandState.WORKING, testObject.getCommand().getState());
		assertEquals(1, testObject.getNukeInfo().getActiveCommands());
		assertEquals(0, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(1, testObject.getNukeInfo().getRequestedCommands());
	}
	
	@Test
	public void testWaitForSecondIterate() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		Thread.sleep(1); // force taskswitch
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(CommandState.WORKING, testObject.getCommand().getState());
		Thread.sleep(3000);
		assertEquals(1, testObject.getCompletedCommands());
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(CommandState.WORKING, testObject.getCommand().getState());
	}
	
	@Test
	public void testWaitForComplete() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		Thread.sleep(1); // force taskswitch
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(CommandState.WORKING, testObject.getCommand().getState());
		Thread.sleep(3000);
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(CommandState.WORKING, testObject.getCommand().getState());
		command.setState(CommandState.STOP);
		testObject.handleOnUpdated(command);
		Thread.sleep(3000);
		assertEquals(testObject.COMPLETED, testObject.getState());
		assertEquals(CommandState.DONE, testObject.getCommand().getState());
	}
	
	@Test
	public void testWaitForTerminate() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		Thread.sleep(10); // force taskswitch
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(CommandState.WORKING, testObject.getCommand().getState());
		Thread.sleep(3000);
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(CommandState.WORKING, testObject.getCommand().getState());
		command.setState(CommandState.TERMINATE);
		testObject.handleOnUpdated(command);
		Thread.sleep(500);
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(CommandState.ABORTED, testObject.getCommand().getState());
	}
	
	@Test
	public void testWaitUnhandledCommand() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		command.setCommand("dir");
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		Thread.sleep(3000);
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(CommandState.ABORTED, testObject.getCommand().getState());
	}
	
	@Test
	public void testShutDown() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		Thread.sleep(1); // force taskswitch
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(CommandState.WORKING, testObject.getCommand().getState());
		assertEquals(1, testObject.getNukeInfo().getActiveCommands());
		assertEquals(0, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(1, testObject.getNukeInfo().getRequestedCommands());
		testObject.setState(testObject.COMPLETED);
		testObject.shutDown();
		assertEquals(0, testObject.getNukeInfo().getActiveCommands());
		assertEquals(1, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(1, testObject.getNukeInfo().getRequestedCommands());
	}
	
	@Test(expected=RuntimeException.class)
	public void testShutDownException() throws Exception {
		Mockito.when(theMap.containsKey(any())).thenReturn(true);
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.init();
		Thread.sleep(1); // force taskswitch
		assertEquals(testObject.RUNNING, testObject.getState());
		assertEquals(CommandState.WORKING, testObject.getCommand().getState());
		assertEquals(1, testObject.getNukeInfo().getActiveCommands());
		assertEquals(0, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(1, testObject.getNukeInfo().getRequestedCommands());
		testObject.shutDown();
		assertEquals(0, testObject.getNukeInfo().getActiveCommands());
		assertEquals(1, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(1, testObject.getNukeInfo().getRequestedCommands());
	}
	
	@Test
	public void testhandleOnUpdated() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.handleOnUpdated(command);
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(command.getComponent(), testObject.getCommand().getComponent());
		assertEquals(command.getCommand(), testObject.getCommand().getCommand());
		assertEquals(command.isRepeated(), testObject.getCommand().isRepeated());
		assertEquals(command.getState(), testObject.getCommand().getState());
		assertEquals(command.getKey(), testObject.getCommand().getKey());
		assertEquals(0, testObject.getNukeInfo().getActiveCommands());
		assertEquals(0, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(0, testObject.getNukeInfo().getRequestedCommands());
	}
	
	@Test
	public void testhandleOnEvicted() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		log.debug(">>> object has: " + testObject.getNukeInfo() );
		testObject.handleOnEvicted(command);
		log.debug(">>> sending in: " + command);
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(command.getComponent(), testObject.getCommand().getComponent());
		assertEquals(command.getCommand(), testObject.getCommand().getCommand());
		assertEquals(command.isRepeated(), testObject.getCommand().isRepeated());
		assertEquals(command.getState(), testObject.getCommand().getState());
		assertEquals(command.getKey(), testObject.getCommand().getKey());
		assertEquals(0, testObject.getNukeInfo().getActiveCommands());
		assertEquals(0, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(0, testObject.getNukeInfo().getRequestedCommands());
	}
	
	@Test
	public void testhandleOnEvictedFinished() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.setState(testObject.COMPLETED);
		testObject.handleOnEvicted(command);
		assertEquals(testObject.COMPLETED, testObject.getState());
		assertEquals(command.getComponent(), testObject.getCommand().getComponent());
		assertEquals(command.getCommand(), testObject.getCommand().getCommand());
		assertEquals(command.isRepeated(), testObject.getCommand().isRepeated());
		assertEquals(command.getState(), testObject.getCommand().getState());
		assertEquals(command.getKey(), testObject.getCommand().getKey());
		assertEquals(0, testObject.getNukeInfo().getActiveCommands());
		assertEquals(0, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(0, testObject.getNukeInfo().getRequestedCommands());
	}
	
	@Test
	public void testhandleOnRemoved() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.handleOnRemoved(command);
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(command.getComponent(), testObject.getCommand().getComponent());
		assertEquals(command.getCommand(), testObject.getCommand().getCommand());
		assertEquals(command.isRepeated(), testObject.getCommand().isRepeated());
		assertEquals(command.getState(), testObject.getCommand().getState());
		assertEquals(command.getKey(), testObject.getCommand().getKey());
		assertEquals(0, testObject.getNukeInfo().getActiveCommands());
		assertEquals(0, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(0, testObject.getNukeInfo().getRequestedCommands());
	}
	
	@Test
	public void testhandleOnRemovedFinished() {
		RepeatedExecuteTaskProcedure testObject = new RepeatedExecuteTaskProcedure(command);
		testObject.setState(testObject.COMPLETED);
		testObject.handleOnRemoved(command);
		assertEquals(testObject.COMPLETED, testObject.getState());
		assertEquals(command.getComponent(), testObject.getCommand().getComponent());
		assertEquals(command.getCommand(), testObject.getCommand().getCommand());
		assertEquals(command.isRepeated(), testObject.getCommand().isRepeated());
		assertEquals(command.getState(), testObject.getCommand().getState());
		assertEquals(command.getKey(), testObject.getCommand().getKey());
		assertEquals(0, testObject.getNukeInfo().getActiveCommands());
		assertEquals(0, testObject.getNukeInfo().getCompletedCommands());
		assertEquals(0, testObject.getNukeInfo().getRequestedCommands());
	}

}
