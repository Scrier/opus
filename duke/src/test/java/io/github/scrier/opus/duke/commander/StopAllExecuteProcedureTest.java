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
import io.github.scrier.opus.CommonCheck;
import io.github.scrier.opus.ICommandCallbackImpl;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.NukeMsgFactory;
import io.github.scrier.opus.common.nuke.NukeStopAllRspMsgC;

import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class StopAllExecuteProcedureTest {

private static TestHelper theHelper;
	
	private final long identity = theHelper.getNextLong();
	private final long nukeId = theHelper.getNextLong();
//	
	private HazelcastInstance instance;
	private Context theContext = Context.INSTANCE;
	private BaseActiveObjectMock theBaseAOC;
	@SuppressWarnings({ "rawtypes", "unused" })
  private IMap theMap;
	private MessageServiceMock SendIF = new MessageServiceMock();
	private int expectedTxID;
	private ICommandCallbackImpl callback = new ICommandCallbackImpl();

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
		callback.resetValues();
		theContext.init(new DukeCommander(instance), theBaseAOC);
		expectedTxID = theContext.getNextTxID() + 1;
	}
	
	@Test
	public void testConstructor() {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		assertEquals(identity, testObject.getIdentity());
		assertEquals(nukeId, testObject.getNukeID());
		assertEquals(testObject.CREATED, testObject.getState());
		assertEquals(null, testObject.getResult());
		assertEquals(expectedTxID, testObject.getTxID());
		assertEquals(0, SendIF.getMessages().size());
		assertEquals(Constants.HC_UNDEFINED, callback.NukeID);
		assertEquals(Constants.HC_UNDEFINED, callback.ProcessID);
		assertEquals("", callback.Query);
		assertEquals("", callback.Result);
		assertEquals(-1, callback.State);
	}
	
	@Test
	public void testInit() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.init();
		assertEquals(testObject.WAITING_FOR_STOP_RSP, testObject.getState());
		assertEquals(1, SendIF.getMessages().size());
		BaseMsgC msg = SendIF.getMessage(0);
		CommonCheck.assertCorrectBaseMessage(msg, NukeMsgFactory.FACTORY_ID, NukeMsgFactory.NUKE_STOP_ALL_REQ);
		assertEquals(testObject.getIdentity(), msg.getSource());
		assertEquals(testObject.getNukeID(), msg.getDestination());
		assertEquals(testObject.getTxID(), msg.getTxID());
	}
	
	@Test
	public void testShutDown() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.shutDown();
		assertEquals(nukeId, callback.NukeID);
		assertEquals(Constants.HC_UNDEFINED, callback.ProcessID);
		assertEquals(null, callback.Query);
		assertEquals(null, callback.Result);
		assertEquals(testObject.CREATED, callback.State);
	}
	
	@Test
	public void testNukeStopAllRspWrongTxID() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.setState(testObject.WAITING_FOR_STOP_RSP);
		NukeStopAllRspMsgC pNukeStopAllRsp = new NukeStopAllRspMsgC();
		pNukeStopAllRsp.setSource(nukeId);
		pNukeStopAllRsp.setDestination(identity);
		pNukeStopAllRsp.setTxID(expectedTxID + 1);
		pNukeStopAllRsp.setSuccess(true);
		pNukeStopAllRsp.setStatus("Status from nuke");
		testObject.handleInMessage(pNukeStopAllRsp);
		assertEquals(testObject.WAITING_FOR_STOP_RSP, testObject.getState());
	}
	
	@Test
	public void testNukeStopAllRspWrongState() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		NukeStopAllRspMsgC pNukeStopAllRsp = new NukeStopAllRspMsgC();
		pNukeStopAllRsp.setSource(nukeId);
		pNukeStopAllRsp.setDestination(identity);
		pNukeStopAllRsp.setTxID(expectedTxID);
		pNukeStopAllRsp.setSuccess(true);
		pNukeStopAllRsp.setStatus("Status from nuke");
		testObject.handleInMessage(pNukeStopAllRsp);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testNukeStopAllRspWrongNoSuccess() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.setState(testObject.WAITING_FOR_STOP_RSP);
		NukeStopAllRspMsgC pNukeStopAllRsp = new NukeStopAllRspMsgC();
		pNukeStopAllRsp.setSource(nukeId);
		pNukeStopAllRsp.setDestination(identity);
		pNukeStopAllRsp.setTxID(expectedTxID);
		pNukeStopAllRsp.setSuccess(false);
		pNukeStopAllRsp.setStatus("Status from nuke");
		testObject.handleInMessage(pNukeStopAllRsp);
		assertEquals(testObject.ABORTED, testObject.getState());
	}
	
	@Test
	public void testNukeStopAllRspWrongOK() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.setState(testObject.WAITING_FOR_STOP_RSP);
		NukeStopAllRspMsgC pNukeStopAllRsp = new NukeStopAllRspMsgC();
		pNukeStopAllRsp.setSource(nukeId);
		pNukeStopAllRsp.setDestination(identity);
		pNukeStopAllRsp.setTxID(expectedTxID);
		pNukeStopAllRsp.setSuccess(true);
		pNukeStopAllRsp.setStatus("Status from nuke");
		testObject.handleInMessage(pNukeStopAllRsp);
		assertEquals(testObject.COMPLETED, testObject.getState());
	}
	
	@Test
	public void testHandleInMessageUnknowMessage() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.setState(testObject.WAITING_FOR_STOP_RSP);
		BaseMsgC pBaseMsg = new BaseMsgC(-1, -1);
		pBaseMsg.setSource(nukeId);
		pBaseMsg.setDestination(identity);
		pBaseMsg.setTxID(expectedTxID);
		testObject.handleInMessage(pBaseMsg);
		assertEquals(testObject.WAITING_FOR_STOP_RSP, testObject.getState());
	}
	
	@Test(expected=AssertionError.class)
	public void testHandleInMessageUndefinedSource() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.setState(testObject.WAITING_FOR_STOP_RSP);
		BaseMsgC pBaseMsg = new BaseMsgC(-1, -1);
		pBaseMsg.setSource(Constants.HC_UNDEFINED);
		pBaseMsg.setDestination(identity);
		pBaseMsg.setTxID(expectedTxID);
		testObject.handleInMessage(pBaseMsg);
		fail("Expected assertion here.");
	}
	
	@Test(expected=AssertionError.class)
	public void testHandleInMessageUndefinedDestination() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.setState(testObject.WAITING_FOR_STOP_RSP);
		BaseMsgC pBaseMsg = new BaseMsgC(-1, -1);
		pBaseMsg.setSource(nukeId);
		pBaseMsg.setDestination(Constants.HC_UNDEFINED);
		pBaseMsg.setTxID(expectedTxID);
		testObject.handleInMessage(pBaseMsg);
		fail("Expected assertion here.");
	}
	
	@Test
	public void testhandleOnUpdated() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.setState(testObject.WAITING_FOR_STOP_RSP);
		testObject.handleOnUpdated(new BaseDataMockC());
		assertEquals(testObject.WAITING_FOR_STOP_RSP, testObject.getState());
	}
	
	@Test
	public void testhandleOnEvicted() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.setState(testObject.WAITING_FOR_STOP_RSP);
		testObject.handleOnEvicted(new BaseDataMockC());
		assertEquals(testObject.WAITING_FOR_STOP_RSP, testObject.getState());
	}
	
	@Test
	public void testhandleOnRemoved() throws Exception {
		StopAllExecuteProcedure testObject = new StopAllExecuteProcedure(nukeId, callback);
		testObject.setState(testObject.WAITING_FOR_STOP_RSP);
		testObject.handleOnRemoved(theHelper.getNextLong());
		assertEquals(testObject.WAITING_FOR_STOP_RSP, testObject.getState());
	}

}
