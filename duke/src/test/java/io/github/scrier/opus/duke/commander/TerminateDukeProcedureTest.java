/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Andreas Joelsson (andreas.joelsson@gmail.com)
 */
package io.github.scrier.opus.duke.commander;

import static org.junit.Assert.*;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.duke.DukeCommandEnum;
import io.github.scrier.opus.common.duke.DukeCommandReqMsgC;
import io.github.scrier.opus.common.duke.DukeCommandRspMsgC;
import io.github.scrier.opus.common.duke.DukeInfo;
import io.github.scrier.opus.common.duke.DukeMsgFactory;
import io.github.scrier.opus.common.duke.DukeState;
import io.github.scrier.opus.common.message.BaseMsgC;
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

public class TerminateDukeProcedureTest {
	
	private static TestHelper theHelper;
	
	private final long identity = theHelper.getNextLong();
	private final long idOtherDuke = theHelper.getNextLong();
	
	private HazelcastInstance instance;
	private Context theContext = Context.INSTANCE;
	private BaseActiveObjectMock theBaseAOC;
	@SuppressWarnings("rawtypes")
  private IMap theMap;
	private DukeInfo dukeInfo;
	private ProcedureWaitImpl callback = new ProcedureWaitImpl();
	private MessageServiceMock SendIF = new MessageServiceMock();
	private int expectedTxID;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		theHelper = TestHelper.INSTANCE;
		theHelper.setLogLevel(Level.TRACE);
	}

	@Before
	public void setUp() throws Exception {
		instance = theHelper.mockHazelcast();
		theHelper.mockIdGen(instance, Shared.Hazelcast.COMMON_MAP_UNIQUE_ID, identity);
		theMap = theHelper.mockMap(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		theBaseAOC = new BaseActiveObjectMock(instance);
		theBaseAOC.preInit();
		theBaseAOC.setMsgService(SendIF);
		theContext.init(new DukeCommander(instance), theBaseAOC);
		expectedTxID = theContext.getNextTxID() + 1;
		callback.resetProcedure();
		dukeInfo = new DukeInfo();
		dukeInfo.setDukeID(idOtherDuke);
		dukeInfo.setKey(idOtherDuke);
		dukeInfo.setState(DukeState.RUNNING);
	}
	
	@After
	public void tearDown() {
		theContext.terminateTimeouts();
	}

	@Test
	public void testConstructor() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
	}
	
	@Test
	public void testInit() throws Exception {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.init();
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_STATUS, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(1, SendIF.size());
		BaseMsgC msg = SendIF.getMessage(0);
		assertEquals(DukeMsgFactory.FACTORY_ID, msg.getFactoryId());
		assertEquals(DukeMsgFactory.DUKE_COMMAND_REQ, msg.getId());
		DukeCommandReqMsgC check = new DukeCommandReqMsgC(msg);
		assertEquals(identity, check.getSource());
		assertEquals(idOtherDuke, check.getDestination());
		assertEquals(testObject.getTxID(), check.getTxID());
		assertEquals(DukeCommandEnum.STATUS, check.getDukeCommand());
	}
	
	@Test
	public void testShutDown() throws Exception {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.COMPLETED);
		testObject.shutDown();
		assertEquals(identity, callback.Identity);
		assertEquals(testObject.COMPLETED, callback.State);
	}

	@Test
	public void testShutDownAborted() throws Exception {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.ABORTED);
		testObject.shutDown();
		assertEquals(identity, callback.Identity);
		assertEquals(testObject.ABORTED, callback.State);
	}
	
	@Test
	public void testOnUpdated() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.handleOnUpdated(new BaseDataMockC());
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
	}
	
	@Test
	public void testOnEvicted() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.handleOnEvicted(new BaseDataMockC());
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
	}
	
	@Test
	public void testOnRemoved() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.handleOnRemoved(identity);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
	}
	
	@Test
	public void testOnRemovedExpected() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.handleOnRemoved(idOtherDuke);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.COMPLETED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
	}
	
	@Test
	public void handleInMessageWrongID() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.handleInMessage(new BaseMsgMockC(DukeMsgFactory.FACTORY_ID, DukeMsgFactory.DUKE_COMMAND_RSP + 1));
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
	}
	
	@Test
	public void handleInMessageAbortedState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.ABORTED);
		DukeCommandRspMsgC input = new DukeCommandRspMsgC();
		testObject.handleInMessage(input);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void handleInMessageCreatedState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		DukeCommandRspMsgC input = new DukeCommandRspMsgC();
		testObject.handleInMessage(input);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void handleInMessageWaitingForStatusStateWrongTxID() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_STATUS);
		DukeCommandRspMsgC input = new DukeCommandRspMsgC();
		input.setTxID(expectedTxID + 1); 
		testObject.handleInMessage(input);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_STATUS, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void handleInMessageWaitingForStatusState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_STATUS);
		DukeCommandRspMsgC input = new DukeCommandRspMsgC();
		input.setTxID(expectedTxID); 
		input.setResponse("This is a rather bad response.");
		testObject.handleInMessage(input);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_STOP, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertTrue(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
		assertEquals(1, SendIF.size());
		BaseMsgC msg = SendIF.getMessage(0);
		assertEquals(DukeMsgFactory.FACTORY_ID, msg.getFactoryId());
		assertEquals(DukeMsgFactory.DUKE_COMMAND_REQ, msg.getId());
		DukeCommandReqMsgC check = new DukeCommandReqMsgC(msg);
		assertEquals(identity, check.getSource());
		assertEquals(idOtherDuke, check.getDestination());
		assertEquals(testObject.getTxID(), check.getTxID());
		assertEquals(DukeCommandEnum.STOP, check.getDukeCommand());
	}
	
	@Test
	public void handleInMessageWaitingForStopStateWrongTxID() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_STOP);
		DukeCommandRspMsgC input = new DukeCommandRspMsgC();
		input.setTxID(expectedTxID + 1); 
		testObject.handleInMessage(input);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_STOP, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void handleInMessageWaitingForStopState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_STOP);
		DukeCommandRspMsgC input = new DukeCommandRspMsgC();
		input.setTxID(expectedTxID); 
		input.setResponse("This is a rather bad response.");
		testObject.handleInMessage(input);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_REMOVE, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertTrue(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
		assertEquals(0, SendIF.size());
	}
	
	@Test
	public void handleInMessageWaitingForTerminateStateWrongTxID() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_TERMINATE);
		DukeCommandRspMsgC input = new DukeCommandRspMsgC();
		input.setTxID(expectedTxID + 1); 
		testObject.handleInMessage(input);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_TERMINATE, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void handleInMessageWaitingForTerminateState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_TERMINATE);
		DukeCommandRspMsgC input = new DukeCommandRspMsgC();
		input.setTxID(expectedTxID); 
		input.setResponse("This is a rather bad response.");
		testObject.handleInMessage(input);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_REMOVE, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertTrue(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
		assertEquals(0, SendIF.size());
	}
	
	@Test
	public void handleInMessageCompletedState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.COMPLETED);
		DukeCommandRspMsgC input = new DukeCommandRspMsgC();
		testObject.handleInMessage(input);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.COMPLETED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void handleInMessageUnknownState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.COMPLETED - 15);
		DukeCommandRspMsgC input = new DukeCommandRspMsgC();
		testObject.handleInMessage(input);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void timeoutTriggeredAbortedState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.ABORTED);
		testObject.timeOutTriggered(testObject.STATUS_TIMEOUT_ID);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void timeoutTriggeredCreatedState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.timeOutTriggered(testObject.STATUS_TIMEOUT_ID);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void timeoutTriggeredWaitingForStatusStateWrongId() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_STATUS);
		testObject.timeOutTriggered(testObject.REMOVE_TIMEOUT_ID + 100);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_STATUS, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void timeoutTriggeredWaitingForStatusState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_STATUS);
		testObject.timeOutTriggered(testObject.STATUS_TIMEOUT_ID);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_TERMINATE, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertTrue(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
		assertEquals(1, SendIF.size());
		BaseMsgC msg = SendIF.getMessage(0);
		assertEquals(DukeMsgFactory.FACTORY_ID, msg.getFactoryId());
		assertEquals(DukeMsgFactory.DUKE_COMMAND_REQ, msg.getId());
		DukeCommandReqMsgC check = new DukeCommandReqMsgC(msg);
		assertEquals(identity, check.getSource());
		assertEquals(idOtherDuke, check.getDestination());
		assertEquals(testObject.getTxID(), check.getTxID());
		assertEquals(DukeCommandEnum.TERMINATE, check.getDukeCommand());
	}
	
	@Test
	public void timeoutTriggeredWaitingForStopStateWrongId() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_STOP);
		testObject.timeOutTriggered(testObject.STOP_TIMEOUT_ID + 100);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_STOP, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void timeoutTriggeredWaitingForStopState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_STOP);
		testObject.timeOutTriggered(testObject.STOP_TIMEOUT_ID);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_TERMINATE, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertTrue(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
		assertEquals(1, SendIF.size());
		BaseMsgC msg = SendIF.getMessage(0);
		assertEquals(DukeMsgFactory.FACTORY_ID, msg.getFactoryId());
		assertEquals(DukeMsgFactory.DUKE_COMMAND_REQ, msg.getId());
		DukeCommandReqMsgC check = new DukeCommandReqMsgC(msg);
		assertEquals(identity, check.getSource());
		assertEquals(idOtherDuke, check.getDestination());
		assertEquals(testObject.getTxID(), check.getTxID());
		assertEquals(DukeCommandEnum.TERMINATE, check.getDukeCommand());
	}
	
	@Test
	public void timeoutTriggeredWaitingForTerminateStateWrongId() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_TERMINATE);
		testObject.timeOutTriggered(testObject.TERMINATE_TIMEOUT_ID + 100);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.WAITING_FOR_TERMINATE, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void timeoutTriggeredWaitingForTerminateState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.WAITING_FOR_TERMINATE);
		testObject.timeOutTriggered(testObject.TERMINATE_TIMEOUT_ID);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
		assertEquals(0, SendIF.size());
	}
	
	@Test
	public void timeoutTriggeredCompletedState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.COMPLETED);
		testObject.timeOutTriggered(testObject.STATUS_TIMEOUT_ID);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.COMPLETED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	@Test
	public void timeoutTriggeredUnknownState() {
		TerminateDukeProcedure testObject = new TerminateDukeProcedure(identity, callback, dukeInfo);
		testObject.setState(testObject.COMPLETED + 15);
		testObject.timeOutTriggered(testObject.STATUS_TIMEOUT_ID);
		assertEquals(identity, testObject.getIdentity());
		assertNotNull(testObject.getSendIF());
		assertEquals(testObject.ABORTED, testObject.getState());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(Constants.HC_UNDEFINED, callback.Identity);
		assertEquals(-1, callback.State);
		assertEquals(0, SendIF.size());
		assertFalse(testObject.isTimeoutActive(testObject.REMOVE_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STATUS_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.STOP_TIMEOUT_ID));
		assertFalse(testObject.isTimeoutActive(testObject.TERMINATE_TIMEOUT_ID));
	}
	
	
	public class BaseMsgMockC extends BaseMsgC {

		public BaseMsgMockC(int factoryID, int messageID) {
	    super(factoryID, messageID);
	    // TODO Auto-generated constructor stub
    }
		
	}
	
}
