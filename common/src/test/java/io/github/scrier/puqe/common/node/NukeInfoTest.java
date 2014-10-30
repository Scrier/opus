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
