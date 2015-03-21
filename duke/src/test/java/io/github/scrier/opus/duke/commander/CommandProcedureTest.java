package io.github.scrier.opus.duke.commander;

import static org.junit.Assert.*;
import io.github.scrier.opus.TestHelper;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeExecuteIndMsgC;
import io.github.scrier.opus.common.nuke.NukeExecuteReqMsgC;
import io.github.scrier.opus.common.nuke.NukeExecuteRspMsgC;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;

public class CommandProcedureTest {
	
	static TestHelper theHelper = TestHelper.INSTANCE;
	
	HazelcastInstance instance;
	long identity = theHelper.getNextLong();
	long sagaID = theHelper.getNextLong();
	long component = theHelper.getNextLong();
	Context theContext = Context.INSTANCE;
	BaseActiveObjectMock theBaseAOC;
	private MessageServiceMock SendIF = new MessageServiceMock();
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
		theHelper.mockIdGen(instance, Shared.Hazelcast.COMMON_SAGA_ID, sagaID);
		theMap = theHelper.mockMap(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		theBaseAOC = new BaseActiveObjectMock(instance);
		theBaseAOC.preInit();
		theBaseAOC.setMsgService(SendIF);
		theContext.init(new DukeCommander(instance), theBaseAOC);
	}

	@After
	public void tearDown() throws Exception {
		theContext.shutDown();
	}

	@Test
	public void testProcedure() {
		CommandProcedure testObject = new CommandProcedure(component, "this is command");
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(identity, testObject.getIdentity());
		assertEquals(1, testObject.getTxID());
	}
	
  @Test
	public void testInit() throws Exception {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.init();
		assertEquals(testObject.INITIALIZING, testObject.getState());
		assertEquals(1, SendIF.size());
		BaseMsgC msg = SendIF.getMessage(0);
		assertEquals(NukeMsgFactory.FACTORY_ID, msg.getFactoryId());
		assertEquals(NukeMsgFactory.NUKE_EXECUTE_REQ, msg.getId());
		NukeExecuteReqMsgC check = new NukeExecuteReqMsgC(msg);
		assertEquals(testObject.getTxID(), check.getTxID());
		assertEquals(component, check.getDestination());
		assertEquals(identity, check.getSource());
		assertEquals("this is yet another command", check.getCommand());
		assertTrue(check.isRepeated());
		assertTrue(check.getFolder().isEmpty());
	}
	
	@Test
	public void testHandleOnUpdatedWrongData() throws Exception {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.init();
		NukeInfo info = new NukeInfo();
		testObject.handleOnUpdated(info);
		assertEquals(testObject.INITIALIZING, testObject.getState());
	}
	
	@Test
	public void testNukeExecuteRspMsgCWrongTxID() {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.setState(testObject.INITIALIZING);
		NukeExecuteRspMsgC input = new NukeExecuteRspMsgC();
		input.setSource(component);
		input.setTxID(testObject.getTxID() + 1);
		input.setDestination(identity);
		testObject.handleInMessage(input);
		assertEquals(testObject.INITIALIZING, testObject.getState());
	}
	
	@Test
	public void testNukeExecuteRspMsgCWrongState() {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.setState(testObject.WORKING);
		NukeExecuteRspMsgC input = new NukeExecuteRspMsgC();
		input.setSource(component);
		input.setTxID(testObject.getTxID());
		input.setDestination(identity);
		testObject.handleInMessage(input);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testNukeExecuteRspMsgCOK() {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.setState(testObject.INITIALIZING);
		NukeExecuteRspMsgC input = new NukeExecuteRspMsgC();
		input.setSource(component);
		input.setTxID(testObject.getTxID());
		input.setDestination(identity);
		testObject.handleInMessage(input);
		assertEquals(testObject.WORKING, testObject.getState());
	}
	
	@Test
	public void tesNukeExecuteIndMsgCWrongSource() {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.setState(testObject.WORKING);
		NukeExecuteIndMsgC input = new NukeExecuteIndMsgC();
		input.setSource(component + 1);
		input.setDestination(identity);
		input.setStatus(CommandState.UNDEFINED);
		testObject.handleInMessage(input);
		assertEquals(testObject.WORKING, testObject.getState());
	}
	
	@Test
	public void tesNukeExecuteIndMsgCWrongState() {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.setState(testObject.INITIALIZING);
		NukeExecuteIndMsgC input = new NukeExecuteIndMsgC();
		input.setSource(component);
		input.setDestination(identity);
		input.setStatus(CommandState.UNDEFINED);
		testObject.handleInMessage(input);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void tesNukeExecuteIndMsgCDefaultCommand() {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.setState(testObject.WORKING);
		testDefaultCommandState(testObject, CommandState.UNDEFINED, testObject.WORKING);
		testDefaultCommandState(testObject, CommandState.EXECUTE, testObject.WORKING);
		testDefaultCommandState(testObject, CommandState.QUERY, testObject.WORKING);
		testDefaultCommandState(testObject, CommandState.STOP, testObject.WORKING);
		testDefaultCommandState(testObject, CommandState.TERMINATE, testObject.WORKING);
		testDefaultCommandState(testObject, CommandState.WORKING, testObject.WORKING);
	}
	
	@Test
	public void tesNukeExecuteIndMsgCDoneCommand() {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.setState(testObject.WORKING);
		testDefaultCommandState(testObject, CommandState.DONE, testObject.COMPLETED);
	}

	@Test
	public void tesNukeExecuteIndMsgCAbortedCommand() {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.setState(testObject.WORKING);
		testDefaultCommandState(testObject, CommandState.ABORTED, testObject.ABORTED);
	}
	
	@Test
	public void testShutdown() throws Exception {
		CommandProcedure testObject = new CommandProcedure(component, "this is yet another command", true);
		testObject.init();
		testObject.shutDown();
		assertEquals(testObject.INITIALIZING, testObject.getState());
	}
	
	private void testDefaultCommandState(CommandProcedure testObject, CommandState commandState, int expectedState) {
	  NukeExecuteIndMsgC input = new NukeExecuteIndMsgC();
		input.setSource(component);
		input.setDestination(identity);
		input.setStatus(commandState);
		testObject.handleInMessage(input);
		assertEquals(expectedState, testObject.getState());
  }

}
