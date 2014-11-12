package io.github.scrier.opus.duke.commander;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.client.MapClearRequest;
import com.hazelcast.queue.impl.AddAllBackupOperation;

public class DukeCommanderTest {
	
	static TestHelper helper = TestHelper.INSTANCE;
	
	long identity = 972591621L;
	HazelcastInstance instance;
	BaseActiveObjectMock theBaseAOC;
	Context theContext = Context.INSTANCE;
	IMap map;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		helper.setLogLevel(Level.TRACE);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		instance = helper.mockHazelcast();
		helper.mockIdGen(instance, Shared.Hazelcast.COMMON_NODE_ID, identity);
		map = helper.mockMap(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		theBaseAOC = new BaseActiveObjectMock(instance);
		theBaseAOC.preInit();
	}

	@After
	public void tearDown() throws Exception {
		instance = null;
		theContext.shutDown();
	}

	@Test
	public void testConstructor() {
		DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		assertEquals(0, testObject.getEntries().size());
		assertTrue(testObject.getProcedures().isEmpty());
		assertTrue(testObject.getProceduresToAdd().isEmpty());
		assertTrue(testObject.getProceduresToRemove().isEmpty());
	}
	
	@Test
	public void testInit() {
		DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		Collection<BaseNukeC> l = new LinkedList<BaseNukeC>();
		l.add(new NukeInfo());
		l.add(new NukeCommand());
		Mockito.when(map.values()).thenReturn(l);
		testObject.init();
		assertTrue(testObject.getProcedures().isEmpty());
		assertFalse(testObject.getProceduresToAdd().isEmpty());
		assertTrue(testObject.getProceduresToRemove().isEmpty());
	}
	
	@Test
	public void testRegisterProcedure() {
		DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		assertEquals(true, testObject.registerProcedure(new BaseProcedureMock()));
		assertTrue(testObject.getProcedures().isEmpty());
		assertFalse(testObject.getProceduresToAdd().isEmpty());
		assertTrue(testObject.getProceduresToRemove().isEmpty());
	}
	
	@Test
	public void testRegisterProcedurSecond() {
		DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		assertEquals(true, testObject.registerProcedure(procedure));
		assertEquals(false, testObject.registerProcedure(procedure));
		assertTrue(testObject.getProcedures().isEmpty());
		assertFalse(testObject.getProceduresToAdd().isEmpty());
		assertTrue(testObject.getProceduresToRemove().isEmpty());
	}
	
	@Test
	public void testShutDown() {
		DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock add = new BaseProcedureMock();
		BaseProcedureMock in = new BaseProcedureMock();
		BaseProcedureMock rem = new BaseProcedureMock();
		testObject.getProceduresToAdd().add(add);
		testObject.getProcedures().add(in);
		testObject.getProceduresToRemove().add(rem);
		testObject.shutDown();
		assertTrue(testObject.getProcedures().isEmpty());
		assertTrue(testObject.getProceduresToAdd().isEmpty());
		assertTrue(testObject.getProceduresToRemove().isEmpty());
		assertTrue(add.isShutDownCalled());
		assertTrue(in.isShutDownCalled());
		assertTrue(rem.isShutDownCalled());
	}
	
  @Test
	public void testClearException() throws Exception {
		DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedure mock = Mockito.mock(BaseProcedure.class);
		Mockito.doThrow(new Exception()).when(mock).shutDown();
		ArrayList<BaseProcedure> list = new ArrayList<BaseProcedure>();
		list.add(mock);
		testObject.clear(list);
		assertTrue(testObject.getProcedures().isEmpty());
		assertTrue(testObject.getProceduresToAdd().isEmpty());
		assertTrue(testObject.getProceduresToRemove().isEmpty());
	}
  
  @Test
  public void testMapCleared() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		testObject.getProcedures().add(new BaseProcedureMock());
		testObject.getProcedures().add(new BaseProcedureMock());
		assertFalse(testObject.getProcedures().isEmpty());
		MapEvent event = Mockito.mock(MapEvent.class);
		testObject.mapCleared(event);
		assertTrue(testObject.getProcedures().isEmpty());
		assertTrue(testObject.getProceduresToAdd().isEmpty());
		assertTrue(testObject.getProceduresToRemove().isEmpty());
  }
  
  @Test
  public void testMapEvicted() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		testObject.getProcedures().add(new BaseProcedureMock());
		testObject.getProcedures().add(new BaseProcedureMock());
		assertFalse(testObject.getProcedures().isEmpty());
		MapEvent event = Mockito.mock(MapEvent.class);
		testObject.mapEvicted(event);
		assertTrue(testObject.getProcedures().isEmpty());
		assertTrue(testObject.getProceduresToAdd().isEmpty());
		assertTrue(testObject.getProceduresToRemove().isEmpty());
  }
  
  @Test
  public void testEntryAddedNukeInfo() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		testObject.preEntry();
		testObject.entryAdded(identity, new NukeInfo());
		testObject.postEntry();
		assertFalse(testObject.getProceduresToAdd().isEmpty());
  }
  
  @Test
  public void testEntryAddedNukeCommand() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		testObject.preEntry();
		testObject.entryAdded(identity, new NukeCommand());
		testObject.postEntry();
		assertTrue(testObject.getProceduresToAdd().isEmpty());
  }
  
  @Test
  public void testEntryAddedUnknown() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseNukeC input = Mockito.mock(BaseNukeC.class);
		testObject.preEntry();
		testObject.entryAdded(identity, input);
		testObject.postEntry();
		assertTrue(testObject.getProceduresToAdd().isEmpty());
  }
  
  @Test
  public void testEntryEvictedNukeInfoCreated() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryEvicted(identity, new NukeInfo());
		testObject.postEntry();
		assertFalse(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnEvicted());
		assertEquals(NukeFactory.NUKE_INFO, procedure.getOnEvicted().getId());
  }
  
  @Test
  public void testEntryEvictedNukeInfoCompleted() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		procedure.setOnEvictedReturn(procedure.COMPLETED);
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryEvicted(identity, new NukeInfo());
		testObject.postEntry();
		assertTrue(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnEvicted());
		assertEquals(NukeFactory.NUKE_INFO, procedure.getOnEvicted().getId());
  }
  
  @Test
  public void testEntryEvictedNukeInfoAborted() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		procedure.setOnEvictedReturn(procedure.ABORTED);
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryEvicted(identity, new NukeInfo());
		testObject.postEntry();
		assertTrue(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnEvicted());
		assertEquals(NukeFactory.NUKE_INFO, procedure.getOnEvicted().getId());
  }
  
  @Test
  public void testEntryRemovedNukeInfoCreated() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryRemoved(identity, new NukeInfo());
		testObject.postEntry();
		assertFalse(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnRemoved());
		assertEquals(NukeFactory.NUKE_INFO, procedure.getOnRemoved().getId());
  }
  
  @Test
  public void testEntryRemovedNukeInfoCompleted() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		procedure.setOnRemovedReturn(procedure.COMPLETED);
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryRemoved(identity, new NukeInfo());
		testObject.postEntry();
		assertTrue(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnRemoved());
		assertEquals(NukeFactory.NUKE_INFO, procedure.getOnRemoved().getId());
  }
  
  @Test
  public void testEntryRemovedNukeInfoAborted() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		procedure.setOnRemovedReturn(procedure.ABORTED);
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryRemoved(identity, new NukeInfo());
		testObject.postEntry();
		assertTrue(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnRemoved());
		assertEquals(NukeFactory.NUKE_INFO, procedure.getOnRemoved().getId());
  }
  
  @Test
  public void testEntryUpdatedNukeInfoCreated() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryUpdated(identity, new NukeInfo());
		testObject.postEntry();
		assertFalse(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnUpdated());
		assertEquals(NukeFactory.NUKE_INFO, procedure.getOnUpdated().getId());
  }
  
  @Test
  public void testEntryUpdatedNukeInfoCompleted() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		procedure.setOnUpdateReturn(procedure.COMPLETED);
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryUpdated(identity, new NukeInfo());
		testObject.postEntry();
		assertTrue(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnUpdated());
		assertEquals(NukeFactory.NUKE_INFO, procedure.getOnUpdated().getId());
  }
  
  @Test
  public void testEntryUpdatedNukeInfoAborted() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		procedure.setOnUpdateReturn(procedure.ABORTED);
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryUpdated(identity, new NukeInfo());
		testObject.postEntry();
		assertTrue(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnUpdated());
		assertEquals(NukeFactory.NUKE_INFO, procedure.getOnUpdated().getId());
  }
  
  @Test
  public void testPreEntry() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		testObject.getProceduresToAdd().add(procedure);
		testObject.preEntry();
		assertFalse(testObject.getProcedures().isEmpty());
		assertTrue(testObject.getProceduresToAdd().isEmpty());
		assertTrue(procedure.isInitCalled());
  }

}
