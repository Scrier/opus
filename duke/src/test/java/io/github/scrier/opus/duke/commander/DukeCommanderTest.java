package io.github.scrier.opus.duke.commander;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import io.github.scrier.opus.TestHelper;
import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.duke.DukeCommandEnum;
import io.github.scrier.opus.common.duke.DukeCommandReqMsgC;
import io.github.scrier.opus.common.duke.DukeCommandRspMsgC;
import io.github.scrier.opus.common.duke.DukeMsgFactory;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeDataFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.common.nuke.NukeState;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;

public class DukeCommanderTest {
	
	private static Logger log = LogManager.getLogger(DukeCommanderTest.class);
	
	static TestHelper helper = TestHelper.INSTANCE;
	
	long identity = 972591621L;
	long otherIdentity = 9785345L;
	long sagaID = 83873L;
	HazelcastInstance instance;
	BaseActiveObjectMock theBaseAOC;
	Context theContext = Context.INSTANCE;
	@SuppressWarnings("rawtypes")
  IMap map;
	IMap settings;
	MessageServiceMock MessageMock = new MessageServiceMock();

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
		helper.mockIdGen(instance, Shared.Hazelcast.COMMON_MAP_UNIQUE_ID, identity);
		helper.mockIdGen(instance, Shared.Hazelcast.COMMON_SAGA_ID, sagaID);
		map = helper.mockMap(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		helper.mockIdGen(instance, Shared.Hazelcast.COMMON_UNIQUE_ID, 11L);
		settings = helper.mockMap(instance, Shared.Hazelcast.SETTINGS_MAP);
		Mockito.when(settings.get(Shared.Settings.EXECUTE_MINIMUM_NODES)).thenReturn("1");
		Mockito.when(settings.get(Shared.Settings.EXECUTE_MAX_USERS)).thenReturn("2");
		Mockito.when(settings.get(Shared.Settings.EXECUTE_INTERVAL)).thenReturn("3");
		Mockito.when(settings.get(Shared.Settings.EXECUTE_USER_INCREASE)).thenReturn("4");
		Mockito.when(settings.get(Shared.Settings.EXECUTE_PEAK_DELAY)).thenReturn("5");
		Mockito.when(settings.get(Shared.Settings.EXECUTE_TERMINATE)).thenReturn("16");
		Mockito.when(settings.get(Shared.Settings.EXECUTE_REPEATED)).thenReturn("true");
		Mockito.when(settings.get(Shared.Settings.EXECUTE_COMMAND)).thenReturn("command");
		Mockito.when(settings.get(Shared.Settings.EXECUTE_FOLDER)).thenReturn("folder");
		theBaseAOC = new BaseActiveObjectMock(instance);
		theBaseAOC.preInit();
		theBaseAOC.setMsgService(MessageMock);
		MessageMock.clear();
	}

	@After
	public void tearDown() throws Exception {
		instance = null;
		theContext.shutDown();
	}
	
	@Test
	public void testConstants() {
		DukeCommander testObject = new DukeCommander(instance);
		BaseProcedureMock mockCheck = new BaseProcedureMock();
		assertEquals(testObject.CREATED, mockCheck.CREATED);
		assertEquals(testObject.ABORTED, mockCheck.ABORTED);
		assertEquals(testObject.COMPLETED, mockCheck.COMPLETED);
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
		Collection<BaseDataC> l = new LinkedList<BaseDataC>();
		NukeInfo info = new NukeInfo();
		info.setState(NukeState.RUNNING);
		l.add(info);
		l.add(new NukeInfo());
		Mockito.when(map.values()).thenReturn(l);
		testObject.init();
		assertFalse(testObject.getProcedures().isEmpty());
		assertTrue(testObject.getProceduresToAdd().isEmpty());
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
		BaseDukeProcedure mock = Mockito.mock(BaseDukeProcedure.class);
		Mockito.doThrow(new Exception()).when(mock).shutDown();
		ArrayList<BaseDukeProcedure> list = new ArrayList<BaseDukeProcedure>();
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
		assertTrue(testObject.getProceduresToAdd().isEmpty());
  }
  
  @Test
  public void testEntryAddedUnknown() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseDataC input = Mockito.mock(BaseDataC.class);
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
		assertEquals(NukeDataFactory.NUKE_INFO, procedure.getOnEvicted().getId());
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
		assertEquals(NukeDataFactory.NUKE_INFO, procedure.getOnEvicted().getId());
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
		assertEquals(NukeDataFactory.NUKE_INFO, procedure.getOnEvicted().getId());
  }
  
  @Test
  public void testEntryRemovedNukeInfoCreated() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryRemoved(identity);
		testObject.postEntry();
		assertFalse(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnRemoved());
		assertEquals(identity, procedure.getOnRemoved());
  }
  
  @Test
  public void testEntryRemovedNukeInfoCompleted() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		procedure.setOnRemovedReturn(procedure.COMPLETED);
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryRemoved(identity);
		testObject.postEntry();
		assertTrue(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnRemoved());
		assertEquals(identity, procedure.getOnRemoved());
  }
  
  @Test
  public void testEntryRemovedNukeInfoAborted() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure = new BaseProcedureMock();
		procedure.setOnRemovedReturn(procedure.ABORTED);
		testObject.getProcedures().add(procedure);
		testObject.preEntry();
		testObject.entryRemoved(identity);
		testObject.postEntry();
		assertTrue(testObject.getProcedures().isEmpty());
		assertNotNull(procedure.getOnRemoved());
		assertEquals(identity, procedure.getOnRemoved());
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
		assertEquals(NukeDataFactory.NUKE_INFO, procedure.getOnUpdated().getId());
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
		assertEquals(NukeDataFactory.NUKE_INFO, procedure.getOnUpdated().getId());
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
		assertEquals(NukeDataFactory.NUKE_INFO, procedure.getOnUpdated().getId());
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
  
  @Test
  public void testGetClasses() {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		BaseProcedureMock procedure1 = new BaseProcedureMock();
		procedure1.setOnEvictedReturn(1);
		BaseProcedureMock procedure2 = new BaseProcedureMock();
		procedure2.setOnEvictedReturn(2);
		BaseProcedureMock procedure3 = new BaseProcedureMock();
		procedure3.setOnEvictedReturn(3);
		BaseProcedureMock procedure4 = new BaseProcedureMock();
		procedure4.setOnEvictedReturn(4);
		BaseProcedureMock procedure5 = new BaseProcedureMock();
		procedure5.setOnEvictedReturn(5);
		testObject.getProcedures().add(procedure1);
		testObject.getProcedures().add(procedure2);
		testObject.getProcedures().add(procedure3);
		testObject.getProcedures().add(procedure4);
		testObject.getProcedures().add(new ClusterDistributorProcedure());
		testObject.getProcedures().add(new CommandProcedure(12345L, "this is command"));
		testObject.getProcedures().add(new NukeProcedure(new NukeInfo()));
		testObject.getProceduresToRemove().add(procedure5);
		List<BaseDukeProcedure> check = testObject.getProcedures(BaseProcedureMock.class);
		assertEquals(4, check.size());
  }
  
  @Test
  public void testDukeCommandReqUndefinedCommand() throws InvalidOperationException {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		DukeCommandReqMsgC input = new DukeCommandReqMsgC();
		input.setSource(otherIdentity);
		input.setDestination(identity);
		input.setTxID(2525);
		input.setDukeCommand(DukeCommandEnum.UNDEFINED);
		testObject.handleInMessage(input);
		assertEquals(1, MessageMock.getMessages().size());
		BaseMsgC baseMsg = MessageMock.getMessage(0);
		assertEquals(DukeMsgFactory.FACTORY_ID, baseMsg.getFactoryId());
		assertEquals(DukeMsgFactory.DUKE_COMMAND_RSP, baseMsg.getId());
		assertEquals(otherIdentity, baseMsg.getDestination());
		assertEquals(identity, baseMsg.getSource());
		assertEquals(2525, baseMsg.getTxID());
		assertEquals(0, theBaseAOC.ShutDownCalls);
		DukeCommandRspMsgC response = new DukeCommandRspMsgC(baseMsg);
		assertTrue(response.getResponse().contains("We received unknown"));
  }
  
  @Test
  public void testDukeCommandReqTerminateCommand() throws InvalidOperationException {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		DukeCommandReqMsgC input = new DukeCommandReqMsgC();
		input.setSource(otherIdentity);
		input.setDestination(identity);
		input.setTxID(2525);
		input.setDukeCommand(DukeCommandEnum.TERMINATE);
		testObject.handleInMessage(input);
		assertEquals(1, MessageMock.getMessages().size());
		BaseMsgC baseMsg = MessageMock.getMessage(0);
		assertEquals(DukeMsgFactory.FACTORY_ID, baseMsg.getFactoryId());
		assertEquals(DukeMsgFactory.DUKE_COMMAND_RSP, baseMsg.getId());
		assertEquals(otherIdentity, baseMsg.getDestination());
		assertEquals(identity, baseMsg.getSource());
		assertEquals(1, theBaseAOC.ShutDownCalls);
		assertEquals(2525, baseMsg.getTxID());
		DukeCommandRspMsgC response = new DukeCommandRspMsgC(baseMsg);
		assertTrue(response.getResponse().contains("Command to terminate is received"));
  }
  
  @Test
  public void testDukeCommandReqStopCommandNoDistributors() throws InvalidOperationException {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		DukeCommandReqMsgC input = new DukeCommandReqMsgC();
		input.setSource(otherIdentity);
		input.setDestination(identity);
		input.setTxID(2525);
		input.setDukeCommand(DukeCommandEnum.STOP);
		testObject.handleInMessage(input);
		assertEquals(1, MessageMock.getMessages().size());
		BaseMsgC baseMsg = MessageMock.getMessage(0);
		assertEquals(DukeMsgFactory.FACTORY_ID, baseMsg.getFactoryId());
		assertEquals(DukeMsgFactory.DUKE_COMMAND_RSP, baseMsg.getId());
		assertEquals(otherIdentity, baseMsg.getDestination());
		assertEquals(identity, baseMsg.getSource());
		assertEquals(0, theBaseAOC.ShutDownCalls);
		assertEquals(2525, baseMsg.getTxID());
		DukeCommandRspMsgC response = new DukeCommandRspMsgC(baseMsg);
		assertTrue(response.getResponse().contains("We have no ClusterDistributor running"));
  }
  
  @Test
  public void testDukeCommandReqStopCommandTooManyDistributors() throws InvalidOperationException {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		testObject.getProcedures().add(new ClusterDistributorProcedure());
		testObject.getProcedures().add(new ClusterDistributorProcedure());
		DukeCommandReqMsgC input = new DukeCommandReqMsgC();
		input.setSource(otherIdentity);
		input.setDestination(identity);
		input.setTxID(2525);
		input.setDukeCommand(DukeCommandEnum.STOP);
		testObject.handleInMessage(input);
		assertEquals(1, MessageMock.getMessages().size());
		BaseMsgC baseMsg = MessageMock.getMessage(0);
		assertEquals(DukeMsgFactory.FACTORY_ID, baseMsg.getFactoryId());
		assertEquals(DukeMsgFactory.DUKE_COMMAND_RSP, baseMsg.getId());
		assertEquals(otherIdentity, baseMsg.getDestination());
		assertEquals(identity, baseMsg.getSource());
		assertEquals(0, theBaseAOC.ShutDownCalls);
		assertEquals(2525, baseMsg.getTxID());
		DukeCommandRspMsgC response = new DukeCommandRspMsgC(baseMsg);
		assertTrue(response.getResponse().contains("We hade more than one ClusterDistributor"));
  }
  
  @Test
  public void testDukeCommandReqStopCommandDistributors() throws Exception {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		ClusterDistributorProcedure clusterDist = new ClusterDistributorProcedure();
		testObject.getProcedures().add(clusterDist);
		clusterDist.init();
		DukeCommandReqMsgC input = new DukeCommandReqMsgC();
		input.setSource(otherIdentity);
		input.setDestination(identity);
		input.setTxID(2525);
		input.setDukeCommand(DukeCommandEnum.STOP);
		testObject.handleInMessage(input);
		assertEquals(1, MessageMock.getMessages().size());
		BaseMsgC baseMsg = MessageMock.getMessage(0);
		assertEquals(DukeMsgFactory.FACTORY_ID, baseMsg.getFactoryId());
		assertEquals(DukeMsgFactory.DUKE_COMMAND_RSP, baseMsg.getId());
		assertEquals(otherIdentity, baseMsg.getDestination());
		assertEquals(identity, baseMsg.getSource());
		assertEquals(0, theBaseAOC.ShutDownCalls);
		assertEquals(2525, baseMsg.getTxID());
		DukeCommandRspMsgC response = new DukeCommandRspMsgC(baseMsg);
		assertTrue(response.getResponse().contains("Command to terminate has been sent to the nukes"));
		assertEquals(clusterDist.TERMINATING, clusterDist.getState());
  }
  
  @Test
  public void testDukeCommandReqStatusCommandDistributors() throws Exception {
  	DukeCommander testObject = new DukeCommander(instance);
		theContext.init(testObject, theBaseAOC);
		testObject.getProcedures().add(new ClusterDistributorProcedure());
		DukeCommandReqMsgC input = new DukeCommandReqMsgC();
		input.setSource(otherIdentity);
		input.setDestination(identity);
		input.setTxID(2525);
		input.setDukeCommand(DukeCommandEnum.STATUS);
		testObject.handleInMessage(input);
		assertEquals(1, MessageMock.getMessages().size());
		BaseMsgC baseMsg = MessageMock.getMessage(0);
		assertEquals(DukeMsgFactory.FACTORY_ID, baseMsg.getFactoryId());
		assertEquals(DukeMsgFactory.DUKE_COMMAND_RSP, baseMsg.getId());
		assertEquals(otherIdentity, baseMsg.getDestination());
		assertEquals(identity, baseMsg.getSource());
		assertEquals(0, theBaseAOC.ShutDownCalls);
		assertEquals(2525, baseMsg.getTxID());
		DukeCommandRspMsgC response = new DukeCommandRspMsgC(baseMsg);
		log.info("Message: " + response);
		assertTrue(response.getResponse().contains("Status of duke with id"));
		assertTrue(response.getResponse().contains("procedures currently working"));
  }

}
