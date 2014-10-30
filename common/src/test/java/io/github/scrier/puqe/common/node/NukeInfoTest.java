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
package io.github.scrier.puqe.common.node;

import static org.junit.Assert.*;

import java.io.IOException;

import io.github.scrier.puqe.common.ObjectDataInputMock;
import io.github.scrier.puqe.common.ObjectDataOutputMock;
import io.github.scrier.puqe.common.TestHelper;

import org.apache.logging.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;

public class NukeInfoTest {

	@BeforeClass
	public static void setupClass() {
		TestHelper.INSTANCE.setLogLevel(Level.TRACE);
	}
	
	@Test
	public void testDefaultConstructor() {
		NukeInfo testObject = new NukeInfo();
		assertEquals(NukeFactory.FACTORY_ID, testObject.getFactoryId());
		assertEquals(NukeFactory.NUKE_INFO, testObject.getId());
		assertEquals(0L, testObject.getNukeID());
		assertEquals(0, testObject.getNumberOfUsers());
		assertEquals(0, testObject.getRequestedUsers());
		assertEquals(NukeState.UNDEFINED, testObject.getState());
		assertEquals(false, testObject.isRepeated());
	}
	
	@Test
	public void testReadWriteData() throws IOException {
		NukeInfo expected = new NukeInfo();
		expected.setNukeID(12345L);
		expected.setNumberOfUsers(12);
		expected.setRepeated(true);
		expected.setRequestedUsers(14);
		expected.setState(NukeState.RUNNING);
		ObjectDataOutputMock out = new ObjectDataOutputMock();
		expected.writeData(out);
		out.close();
		ObjectDataInputMock in = new ObjectDataInputMock(out.getTempFile());
		NukeInfo actual = new NukeInfo();
		actual.readData(in);
		assertEquals(expected.getNukeID(), actual.getNukeID());
		assertEquals(expected.getNumberOfUsers(), actual.getNumberOfUsers());
		assertEquals(expected.isRepeated(), actual.isRepeated());
		assertEquals(expected.getRequestedUsers(), actual.getRequestedUsers());
		assertEquals(expected.getState(), actual.getState());
	}

}
