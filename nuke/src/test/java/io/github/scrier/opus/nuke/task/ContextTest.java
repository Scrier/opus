package io.github.scrier.opus.nuke.task;

import static org.junit.Assert.*;
import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.exception.InvalidOperationException;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContextTest {
	
	static TestHelper helper = TestHelper.INSTANCE;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		helper.setLogLevel(Level.TRACE);
	}

	@Before
	public void setUp() throws Exception {
		Context.INSTANCE.shutDown();
	}

	@Test(expected=NullPointerException.class)
	public void testInstance() throws Exception {
		Context testObject = Context.INSTANCE;
		assertNull(testObject.getInstance());
		assertEquals(1, testObject.getNextTxID());
		assertNull(testObject.getTask());
		assertNull(testObject.getIdentity());
		fail("Expected earlier exception to be thrown.");
	}

}
