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

public class CommandProcedureTest {
	
	static TestHelper theHelper = TestHelper.INSTANCE;
	
	HazelcastInstance instance;
	long identity = 8239421L;
	Context theContext = Context.INSTANCE;
	BaseActiveObjectMock theBaseAOC;
	IMap theMap;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		theHelper.setLogLevel(Level.TRACE);
	}

  @Before
	public void setUp() throws Exception {
		instance = theHelper.mockHazelcast();
		theHelper.mockIdGen(instance, Shared.Hazelcast.COMMON_NODE_ID, identity);
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
	
	@Test
	public void testInit() throws Exception {
		CommandProcedure testObject = new CommandProcedure("this is yet another command", CommandState.EXECUTE, true);
		testObject.init();
		assertEquals(testObject.WORKING, testObject.getState());
		verify(theMap, times(1)).put(anyLong(), any());
	}

}
