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
package io.github.scrier.opus.common.nuke;

import static org.junit.Assert.*;

import java.io.IOException;

import io.github.scrier.opus.common.ObjectDataInputMock;
import io.github.scrier.opus.common.ObjectDataOutputMock;
import io.github.scrier.opus.common.SendIFMock;
import io.github.scrier.opus.common.message.BaseMsgC;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class NukeExecuteReqMsgCTest {

	public static SendIFMock sendIF;

	@BeforeClass
	public static void setupClass() {
		sendIF = new SendIFMock();
	}

	@AfterClass
	public static void tearDownClass() {
		sendIF.clear();
		sendIF = null;
	}

	@Before()
	public void setup() {
		sendIF.clear();
	}

	@Test
	public void testNukeExecuteReqDefaultConstructor() {
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC();
		assertEquals(NukeMsgFactory.FACTORY_ID, testObject.getFactoryId());
		assertEquals(NukeMsgFactory.NUKE_EXECUTE_REQ, testObject.getId());
		assertEquals(-1L, testObject.getSource());
		assertEquals(-1L, testObject.getDestination());
		assertEquals(-1L, testObject.getTxID());
		assertEquals("", testObject.getCommand());
		assertEquals("", testObject.getFolder());
		assertEquals(false, testObject.isRepeated());
	}

	@Test
	public void testNukeExecuteReqSendIFConstructor() {
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC(sendIF);
		assertEquals(NukeMsgFactory.FACTORY_ID, testObject.getFactoryId());
		assertEquals(NukeMsgFactory.NUKE_EXECUTE_REQ, testObject.getId());
		assertEquals(-1L, testObject.getSource());
		assertEquals(-1L, testObject.getDestination());
		assertEquals(-1L, testObject.getTxID());
		assertEquals("", testObject.getCommand());
		assertEquals("", testObject.getFolder());
		assertEquals(false, testObject.isRepeated());
		assertEquals(0, sendIF.getSize());
		testObject.send();
		assertEquals(1, sendIF.getSize());
	}

	@Test
	public void testNukeExecuteReqCopyConstructor() {
		NukeExecuteReqMsgC inputObject = new NukeExecuteReqMsgC();
		inputObject.setSource(1212L);
		inputObject.setDestination(2323L);
		inputObject.setTxID(3434);
		inputObject.setCommand("command");
		inputObject.setFolder("folder");
		inputObject.setRepeated(true);
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC(inputObject);
		assertEquals(inputObject.getFactoryId(), testObject.getFactoryId());
		assertEquals(inputObject.getId(), testObject.getId());
		assertEquals(inputObject.getSource(), testObject.getSource());
		assertEquals(inputObject.getDestination(), testObject.getDestination());
		assertEquals(inputObject.getTxID(), testObject.getTxID());
		assertEquals(inputObject.getCommand(), testObject.getCommand());
		assertEquals(inputObject.getFolder(), testObject.getFolder());
		assertEquals(inputObject.isRepeated(), testObject.isRepeated());
	}

	@Test
	public void testNukeExecuteReqCastConstructor() {
		NukeExecuteReqMsgC inputObject = new NukeExecuteReqMsgC();
		inputObject.setSource(1212L);
		inputObject.setDestination(2323L);
		inputObject.setTxID(3434);
		inputObject.setCommand("command");
		inputObject.setFolder("folder");
		inputObject.setRepeated(true);
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC((BaseMsgC)inputObject);
		assertEquals(inputObject.getFactoryId(), testObject.getFactoryId());
		assertEquals(inputObject.getId(), testObject.getId());
		assertEquals(inputObject.getSource(), testObject.getSource());
		assertEquals(inputObject.getDestination(), testObject.getDestination());
		assertEquals(inputObject.getTxID(), testObject.getTxID());
		assertEquals(inputObject.getCommand(), testObject.getCommand());
		assertEquals(inputObject.getFolder(), testObject.getFolder());
		assertEquals(inputObject.isRepeated(), testObject.isRepeated());
	}

	@Test(expected=ClassCastException.class)
	public void testNukeExecuteReqInvalidCastConstructor() {
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC(new BaseMsgC(0,0));
		fail("Did not expect to come here!!!");
		testObject.getClass();
	}

	@Test(expected=NullPointerException.class)
	public void testNukeExecuteReqSendException() {
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC();
		testObject.send();
		fail("Did not expect to come here!!!");
	}

	@Test(expected=NullPointerException.class)
	public void testNukeExecuteReqCopyConstructorSendException() {
		NukeExecuteReqMsgC inputObject = new NukeExecuteReqMsgC();
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC(inputObject);
		testObject.send();
		fail("Did not expect to come here!!!");
	}

	@Test
	public void testNukeExecuteReqSendCopyConstructor() {
		NukeExecuteReqMsgC inputObject = new NukeExecuteReqMsgC(sendIF);
		inputObject.setSource(1212L);
		inputObject.setDestination(2323L);
		inputObject.setTxID(3434);
		inputObject.setCommand("command");
		inputObject.setFolder("folder");
		inputObject.setRepeated(true);
		inputObject.send();
		assertEquals(1, sendIF.getSize());
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC(sendIF.getItem(0));
		assertEquals(inputObject.getFactoryId(), testObject.getFactoryId());
		assertEquals(inputObject.getId(), testObject.getId());
		assertEquals(inputObject.getSource(), testObject.getSource());
		assertEquals(inputObject.getDestination(), testObject.getDestination());
		assertEquals(inputObject.getTxID(), testObject.getTxID());
		assertEquals(inputObject.getCommand(), testObject.getCommand());
		assertEquals(inputObject.getFolder(), testObject.getFolder());
		assertEquals(inputObject.isRepeated(), testObject.isRepeated());
	}

	@Test
	public void testNukeExecuteReqCopyConstructorSend() {
		NukeExecuteReqMsgC inputObject = new NukeExecuteReqMsgC(sendIF);
		inputObject.setSource(1212L);
		inputObject.setDestination(2323L);
		inputObject.setTxID(3434);
		inputObject.setCommand("command");
		inputObject.setFolder("folder");
		inputObject.setRepeated(true);
		NukeExecuteReqMsgC sendObject = new NukeExecuteReqMsgC(inputObject);
		sendObject.send();
		assertEquals(1, sendIF.getSize());
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC(sendIF.getItem(0));
		assertEquals(inputObject.getFactoryId(), testObject.getFactoryId());
		assertEquals(inputObject.getId(), testObject.getId());
		assertEquals(inputObject.getSource(), testObject.getSource());
		assertEquals(inputObject.getDestination(), testObject.getDestination());
		assertEquals(inputObject.getTxID(), testObject.getTxID());
		assertEquals(inputObject.getCommand(), testObject.getCommand());
		assertEquals(inputObject.getFolder(), testObject.getFolder());
		assertEquals(inputObject.isRepeated(), testObject.isRepeated());
	}

	@Test
	public void testNukeExecuteReqToStringMethod() {
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC();
		String testStr = testObject.toString();
		assertEquals(true, testStr.contains("NukeExecuteReqMsgC"));
		assertEquals(true, testStr.contains("command"));
		assertEquals(true, testStr.contains("folder"));
		assertEquals(true, testStr.contains("repeated"));
	}

	@Test
	public void testNukeExecuteReqReadWrite() throws IOException {
		NukeExecuteReqMsgC inputObject = new NukeExecuteReqMsgC();
		inputObject.setSource(1212L);
		inputObject.setDestination(2323L);
		inputObject.setTxID(3434);
		inputObject.setCommand("command");
		inputObject.setFolder("folder");
		inputObject.setRepeated(true);
		ObjectDataOutputMock out = new ObjectDataOutputMock();
		inputObject.writeData(out);
		out.close();
		ObjectDataInputMock in = new ObjectDataInputMock(out.getTempFile());
		NukeExecuteReqMsgC testObject = new NukeExecuteReqMsgC();
		testObject.readData(in);
		assertEquals(inputObject.getFactoryId(), testObject.getFactoryId());
		assertEquals(inputObject.getId(), testObject.getId());
		assertEquals(inputObject.getSource(), testObject.getSource());
		assertEquals(inputObject.getDestination(), testObject.getDestination());
		assertEquals(inputObject.getTxID(), testObject.getTxID());
		assertEquals(inputObject.getCommand(), testObject.getCommand());
		assertEquals(inputObject.getFolder(), testObject.getFolder());
		assertEquals(inputObject.isRepeated(), testObject.isRepeated());
		assertEquals(true, in.remove());
	}

}
