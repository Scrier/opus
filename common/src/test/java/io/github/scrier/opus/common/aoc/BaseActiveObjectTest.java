package io.github.scrier.opus.common.aoc;

import static org.junit.Assert.*;
import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.TestHelper;
import io.github.scrier.opus.common.exception.InvalidOperationException;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;

public class BaseActiveObjectTest {

	private HazelcastInstance instance;
	private IdGenerator idGen;
	private IMap<Object, Object> settings;
	private long id = 12345L;

	@BeforeClass
	public static void setupClass() {
		TestHelper.INSTANCE.setLogLevel(Level.TRACE);
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		instance = Mockito.mock(HazelcastInstance.class);
		idGen = Mockito.mock(IdGenerator.class);
		settings = Mockito.mock(IMap.class);
		Mockito.when(idGen.newId()).thenReturn(id).thenReturn(Constants.HC_UNDEFINED);
		Mockito.when(instance.getIdGenerator(Shared.Hazelcast.COMMON_MAP_UNIQUE_ID)).thenReturn(idGen);
		Mockito.when(instance.getMap(Shared.Hazelcast.SETTINGS_MAP)).thenReturn(settings);
	}

	@After
	public void tearDown() {
		instance = null;
	}

	@Test
	public void testHazelcastInstanceConstructor() throws Exception {
		BaseActiveObjectMock testObject = new BaseActiveObjectMock(instance);
		assertNotNull(testObject.getInstance());
		assertFalse((Boolean)TestHelper.INSTANCE.invokeMethod(BaseActiveObject.class, "isCorrectInitPerformed", testObject));
	}

	@Test(expected=InvalidOperationException.class)
	public void testHazelcastInstanceConstructorExceptionSettings() throws InvalidOperationException {
		BaseActiveObjectMock testObject = new BaseActiveObjectMock(instance);
		testObject.getSettings();
	}

	@Test(expected=InvalidOperationException.class)
	public void testHazelcastInstanceConstructorExceptionIdentity() throws InvalidOperationException {
		BaseActiveObjectMock testObject = new BaseActiveObjectMock(instance);
		testObject.getIdentity();
	}

	@Test
	public void testPreInitMethod() throws Exception {
		BaseActiveObjectMock testObject = new BaseActiveObjectMock(instance);
		testObject.preInit();
		assertNotNull(testObject.getInstance());
		assertNotNull(testObject.getSettings());
		assertEquals(id, testObject.getIdentity());
		assertTrue((Boolean)TestHelper.INSTANCE.invokeMethod(BaseActiveObject.class, "isCorrectInitPerformed", testObject));
	}

}
