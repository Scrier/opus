package io.github.scrier.opus.duke.commander;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import io.github.scrier.opus.TestHelper;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.common.nuke.NukeInfo;

public class CommandProcedureTest {
	
	static TestHelper theHelper = TestHelper.INSTANCE;
	
	HazelcastInstance instance;
	long identity = 8239421L;
	Context theContext = Context.INSTANCE;
	BaseActiveObjectMock theBaseAOC;
	@SuppressWarnings("rawtypes")
  IMap theMap;

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
	public void testProcedure() {
		CommandProcedure testObject = new CommandProcedure("this is command", CommandState.EXECUTE);
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(identity, testObject.getIdentity());
		assertEquals(1, testObject.getTxID());
		NukeCommand check = testObject.getNukeCommand();
		assertEquals("this is command", check.getCommand());
		assertEquals("", check.getResponse());
		assertEquals(CommandState.EXECUTE, check.getState());
		assertEquals(testObject.getTxID(), check.getTxID());
		assertEquals(false, check.isRepeated());
	}
	
	@Test
	public void testProcedure2() {
		CommandProcedure testObject = new CommandProcedure("this is another command", CommandState.QUERY, true);
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(identity, testObject.getIdentity());
		assertEquals(1, testObject.getTxID());
		NukeCommand check = testObject.getNukeCommand();
		assertEquals("this is another command", check.getCommand());
		assertEquals("", check.getResponse());
		assertEquals(CommandState.QUERY, check.getState());
		assertEquals(testObject.getTxID(), check.getTxID());
		assertEquals(true, check.isRepeated());
	}
	
	@SuppressWarnings("unchecked")
  @Test
	public void testInit() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		assertEquals(testObject.WORKING, testObject.getState());
		verify(theMap, times(1)).put(anyLong(), any(NukeCommand.class));
	}
	
	@Test
	public void testHandleOnUpdatedWrongData() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeInfo info = new NukeInfo();
		testObject.handleOnUpdated(info);
		assertEquals(testObject.WORKING, testObject.getState());
	}
	
	@Test
	public void testHandleOnUpdatedWrongTxID() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID() + 1);
		testObject.handleOnUpdated(command);
		assertEquals(testObject.WORKING, testObject.getState());
	}
	
	@Test
	public void testHandleOnUpdatedWrongAborted() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID());
		command.setState(CommandState.ABORTED);
		testObject.handleOnUpdated(command);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testHandleOnUpdatedWrongQuery() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID());
		command.setState(CommandState.QUERY);
		testObject.handleOnUpdated(command);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testHandleOnUpdatedWrongUndefined() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID());
		command.setState(CommandState.UNDEFINED);
		testObject.handleOnUpdated(command);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testHandleOnUpdatedWrongWorking() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID());
		command.setState(CommandState.WORKING);
		testObject.handleOnUpdated(command);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testHandleOnUpdatedWrongExecute() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID());
		command.setState(CommandState.EXECUTE);
		testObject.handleOnUpdated(command);
		assertEquals(testObject.WORKING, testObject.getState());
	}
	
	@Test
	public void testHandleOnUpdatedWrongDone() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID());
		command.setState(CommandState.DONE);
		testObject.handleOnUpdated(command);
		assertEquals(testObject.REMOVING, testObject.getState());
	}
	
	@Test
	public void testHandleOnEvictedWrongTxID() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID() + 1);
		testObject.handleOnEvicted(command);
		assertEquals(testObject.WORKING, testObject.getState());
	}
	
	@Test
	public void testHandleOnEvictedOK() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID());
		testObject.handleOnEvicted(command);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testHandleOnRemovedWrongTxID() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID() + 1);
		testObject.handleOnRemoved(command);
		assertEquals(testObject.WORKING, testObject.getState());
	}
	
	@Test
	public void testHandleOnRemovedWrongState() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID());
		testObject.handleOnRemoved(command);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testHandleOnRemovedOK() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		testObject.setState(testObject.REMOVING);
		NukeCommand command = new NukeCommand();
		command.setTxID(testObject.getTxID());
		testObject.handleOnRemoved(command);
		assertEquals(testObject.COMPLETED, testObject.getState());
	}
	
	@Test
	public void testShutdown() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		testObject.shutDown();
		assertEquals(testObject.WORKING, testObject.getState());
	}

}
