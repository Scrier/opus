package io.github.scrier.opus.nuke.task.procedures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.common.nuke.NukeState;
import io.github.scrier.opus.nuke.task.BaseTaskProcedure;

/**
 * Class that will handle state changes of the NukeInfo object related to this
 * nuke node.
 * @author andreas.joelsson
 */
public class NukeProcedure extends BaseTaskProcedure {

	private static Logger log = LogManager.getLogger(NukeProcedure.class);

	private long identity;

	public final int WAITING_TO_BE_TAKEN = CREATED + 1;
	public final int INITIALIZING        = CREATED + 2;
	public final int RUNNING             = CREATED + 3;
	public final int SHUTTING_DOWN       = CREATED + 4;

	public NukeProcedure(long identity) {
		setIdentity(identity);
		getNukeInfo().setNukeID(getIdentity());
		getNukeInfo().setKey(getIdentity());
		getNukeInfo().setState(NukeState.AVAILABLE);
		log.info("Publishing nuke info to map that we are available: " + getNukeInfo() + "."); 
		addEntry(getNukeInfo());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		log.trace("init()");
		setState(WAITING_TO_BE_TAKEN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() throws Exception {
		log.trace("shutDown()");
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnUpdated(BaseNukeC data) {
		log.trace("handleOnUpdated(" + data + ")");
		if( data.getKey() == getNukeInfo().getKey() ) {
			switch( data.getId() ) {
				case NukeFactory.NUKE_INFO: {
					NukeInfo nukeInfo = new NukeInfo(data);
					handleUpdate(nukeInfo);
					break;
				}
				case NukeFactory.NUKE_COMMAND:
				default: {
					// do nothing.
					break;
				}
			}
		}
		return getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnEvicted(BaseNukeC data) {
		log.trace("handleOnEvicted(" + data + ")");
		if( data.getKey() == getNukeInfo().getKey() ) {
			switch( data.getId() ) {
				case NukeFactory.NUKE_INFO: {
					log.fatal("Nuke info about this node was evicted from the map. Terminating.");
					throw new RuntimeException("Nuke info about node " + getNukeInfo().getKey() + " was evicted from the map. Terminating.");
				}
				case NukeFactory.NUKE_COMMAND:
				default: {
					throw new RuntimeException("Unimplemented id " + data.getId() + " received in NukeProcedure.handleOnEvicted.");
				}
			}		
		}
		return getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnRemoved(BaseNukeC data) {
		log.trace("handleOnRemoved(" + data + ")");
		if( data.getKey() == getNukeInfo().getKey() ) {
			switch( data.getId() ) {
				case NukeFactory.NUKE_INFO: {
					// do something here, when other is done.
					throw new RuntimeException("not implemented NukeProcedure.handleOnRemoved.");
				}
				case NukeFactory.NUKE_COMMAND:
				default: {
					throw new RuntimeException("Unimplemented id " + data.getId() + " received in NukeProcedure.handleOnRemoved.");
				}
			}
		}
		return getState();
	}

	/**
	 * Method to handle updates of the NukeInfo message.
	 * @param msg NukeInfo object.
	 */
	protected void handleUpdate(NukeInfo msg) {
		log.trace("handleUpdate(" + msg + ")");
		long fieldsChanged = getNukeInfo().compare(msg);
		if( 0 < (NukeInfo.STATE_MODIFIED & fieldsChanged) ) handleNewState(msg.getState());
		if( 0 < (NukeInfo.ACTIVE_COMMANDS_MODIFIED & fieldsChanged) ) throw new RuntimeException("Someone other than nuke id: " + getIdentity() + " modified Active Commands.");
		if( 0 < (NukeInfo.COMPLETED_COMMANDS_MODIFIED & fieldsChanged) ) throw new RuntimeException("Someone other than nuke id: " + getIdentity() + " modified Completed Commands.");
		if( 0 < (NukeInfo.NUKE_ID_MODIFIED & fieldsChanged) ) throw new RuntimeException("Someone modified nuke id field.");
		if( 0 < (NukeInfo.NUMBER_OF_USERS_MODIFIED & fieldsChanged) ) throw new RuntimeException("Someone other than nuke id: " + getIdentity() + " modified Number of Users.");
		if( 0 < (NukeInfo.REPEATED_MODIFIED & fieldsChanged) ) throw new RuntimeException("Someone other that nuke id: " + getIdentity() + " modified Repeated.");
		if( 0 < (NukeInfo.REQUESTED_COMMANDS_MODIFIED & fieldsChanged) ) throw new RuntimeException("Someone other that nuke id: " + getIdentity() + " modified Requested Commands.");
		if( 0 < (NukeInfo.REQUESTED_USERS_MODIFIED & fieldsChanged) ) throw new RuntimeException("Someone other that nuke id: " + getIdentity() + " modified Requested Users.");
	}

	/**
	 * Method to handle updated to the state.
	 * @param state NukeState change to.
	 */
	protected void handleNewState(NukeState state) {
		log.trace("handleNewState(" + state + ")");
		log.info("State changed from " + getNukeInfo().getState() + " to " + state + ".");
		switch (state) {
			case TAKEN: {
				handleInitialize();
				break;
			}
			case UNRESPONSIVE: {
				handleUnresponsive();
				break;
			}
			default: {
				throw new RuntimeException("Duke is not allowed to change state of NukeInfo to " + state + ".");
			}
		}
	}
	
	protected void handleInitialize() {
		log.trace("handleInitialize()");
		getNukeInfo().setState(NukeState.INTITIALIZED); // Wont inform any duke about this step atm.
		setState(INITIALIZING);
		// do initialization.
		getNukeInfo().setState(NukeState.RUNNING);
		setState(RUNNING);
	}
	
	protected void handleUnresponsive() {
		log.trace("handleUnresponsive()");
		throw new RuntimeException("Not implemented handleUnresponsive.");
	}

	/**
	 * @return the identity
	 */
	private long getIdentity() {
		return identity;
	}

	/**
	 * @param identity the identity to set
	 */
	private void setIdentity(long identity) {
		this.identity = identity;
	}

}
