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
package io.github.scrier.opus.common.node;

import static org.junit.Assert.*;

import java.io.IOException;

import io.github.scrier.opus.common.ObjectDataInputMock;
import io.github.scrier.opus.common.ObjectDataOutputMock;
import io.github.scrier.opus.common.TestHelper;
import io.github.scrier.opus.common.nuke.NukeDataFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.common.nuke.NukeState;

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
		assertEquals(NukeDataFactory.FACTORY_ID, testObject.getFactoryId());
		assertEquals(NukeDataFactory.NUKE_INFO, testObject.getId());
		assertEquals(0L, testObject.getNukeID());
		assertEquals(0, testObject.getNumberOfThreads());
		assertEquals(0, testObject.getRequestedThreads());
		assertEquals(NukeState.UNDEFINED, testObject.getState());
		assertEquals(false, testObject.isRepeated());
	}
	
	@Test
	public void testReadWriteData() throws IOException {
		NukeInfo expected = new NukeInfo();
		expected.setNukeID(12345L);
		expected.setNumberOfThreads(12);
		expected.setRepeated(true);
		expected.setRequestedThreads(14);
		expected.setState(NukeState.RUNNING);
		ObjectDataOutputMock out = new ObjectDataOutputMock();
		expected.writeData(out);
		out.close();
		ObjectDataInputMock in = new ObjectDataInputMock(out.getTempFile());
		NukeInfo actual = new NukeInfo();
		actual.readData(in);
		assertEquals(expected.getNukeID(), actual.getNukeID());
		assertEquals(expected.getNumberOfThreads(), actual.getNumberOfThreads());
		assertEquals(expected.isRepeated(), actual.isRepeated());
		assertEquals(expected.getRequestedThreads(), actual.getRequestedThreads());
		assertEquals(expected.getState(), actual.getState());
	}

}
