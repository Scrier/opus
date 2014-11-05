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
import io.github.scrier.opus.common.TestHelper;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class NukeFactoryTest {

	NukeFactory testObject;
	
	@BeforeClass
	public static void setupClass() {
		TestHelper.INSTANCE.setLogLevel(Level.TRACE);
	}
	
	@Before
	public void setup() {
		testObject = new NukeFactory();
	}
	
	@After
	public void tearDown() {
		testObject = null;
	}
	
	@Test
	public void testNukeInfoFactoryCreate() {
		IdentifiedDataSerializable actual = testObject.create(NukeFactory.NUKE_INFO);
		assertNotNull(actual);
		NukeInfo expected = new NukeInfo();
		assertEquals(expected.getClass(), actual.getClass());
	}
	
	@Test
	public void testNullFactoryCreate() {
		IdentifiedDataSerializable actual = testObject.create(-1);
		assertNull(actual);
	}
	
	@Test
	public void testFactoryID() {
		assertEquals(801023, NukeFactory.FACTORY_ID);
	}
	
	

}
