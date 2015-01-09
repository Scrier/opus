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
	
	boolean stopped;
	boolean terminated;
	
	public NukeProcedure(long identity) {
		setIdentity(identity);
		setStopped(false);
		setTerminated(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		log.trace("init()");
		getNukeInfo().setNukeID(getIdentity());
		getNukeInfo().setKey(getIdentity());
		getNukeInfo().setState(NukeState.AVAILABLE);
		log.info("[" + getTxID() + "] Publishing nuke info to map that we are available: " + getNukeInfo() + "."); 
		addEntry(getNukeInfo());
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
					log.fatal("[" + getTxID() + "] Nuke info about this node was evicted from the map. Terminating.");
					throw new RuntimeException("Nuke info about node " + getNukeInfo().getKey() + " was evicted from the map. Terminating.");
				}
				case NukeFactory.NUKE_COMMAND:
				default: {
					log.fatal("[" + getTxID() + "] Unimplemented id " + data.getId() + " received in NukeProcedure.handleOnEvicted.");
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
	public int handleOnRemoved(Long key) {
		log.trace("handleOnRemoved(" + key + ")");
		if( key == getNukeInfo().getKey() ) {
			setState(ABORTED);
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
		// the items below are modified by command and give a fals positive.
//		if( 0 < (NukeInfo.ACTIVE_COMMANDS_MODIFIED & fieldsChanged) ) {
//			log.fatal("[" + getTxID() + "] Someone other than nuke id: " + getIdentity() + " modified Active Commands.");
//			throw new RuntimeException("Someone other than nuke id: " + getIdentity() + " modified Active Commands.");
//		}
//		if( 0 < (NukeInfo.COMPLETED_COMMANDS_MODIFIED & fieldsChanged) ) {
//			log.fatal("[" + getTxID() + "] Someone other than nuke id: " + getIdentity() + " modified Completed Commands.");
//			throw new RuntimeException("Someone other than nuke id: " + getIdentity() + " modified Completed Commands.");
//		}
		if( 0 < (NukeInfo.NUKE_ID_MODIFIED & fieldsChanged) ) {
			log.fatal("[" + getTxID() + "] Someone modified nuke id field.");
			throw new RuntimeException("Someone modified nuke id field.");
		}
		if( 0 < (NukeInfo.NUMBER_OF_THREADS_MODIFIED & fieldsChanged) ) {
			log.fatal("[" + getTxID() + "] Someone other than nuke id: " + getIdentity() + " modified Number of Users.");
			throw new RuntimeException("Someone other than nuke id: " + getIdentity() + " modified Number of Users.");
		}
		if( 0 < (NukeInfo.REPEATED_MODIFIED & fieldsChanged) ) {
			log.fatal("[" + getTxID() + "] Someone other that nuke id: " + getIdentity() + " modified Repeated.");
			throw new RuntimeException("Someone other that nuke id: " + getIdentity() + " modified Repeated.");
		}
	// the items below are modified by command and give a fals positive.
//		if( 0 < (NukeInfo.REQUESTED_COMMANDS_MODIFIED & fieldsChanged) ) {
//			log.fatal("[" + getTxID() + "] Someone other that nuke id: " + getIdentity() + " modified Requested Commands.");
//			throw new RuntimeException("Someone other that nuke id: " + getIdentity() + " modified Requested Commands.");
//		}
		if( 0 < (NukeInfo.REQUESTED_THREADS_MODIFIED & fieldsChanged) ) {
			log.fatal("[" + getTxID() + "] Someone other that nuke id: " + getIdentity() + " modified Requested Users.");
			throw new RuntimeException("Someone other that nuke id: " + getIdentity() + " modified Requested Users.");
		}
	}

	/**
	 * Method to handle updated to the state.
	 * @param state NukeState change to.
	 */
	protected void handleNewState(NukeState state) {
		log.trace("handleNewState(" + state + ")");
		log.info("[" + getTxID() + "] State changed from " + getNukeInfo().getState() + " to " + state + ".");
		switch (state) {
			case TAKEN: {
				getNukeInfo().setState(state);
				handleInitialize();
				break;
			}
			case UNRESPONSIVE: {
				getNukeInfo().setState(state);
				handleUnresponsive();
				break;
			}
			default: {
				log.fatal("[" + getTxID() + "] Duke is not allowed to change state of NukeInfo to " + state + ".");
				throw new RuntimeException("Duke is not allowed to change state of NukeInfo to " + state + ".");
			}
		}
	}
	
	protected void handleInitialize() {
		log.trace("handleInitialize()");
		log.info("[" + getTxID() + "] Changing state from " + getNukeInfo().getState() + " to " + NukeState.INTITIALIZED + ".");
		getNukeInfo().setState(NukeState.INTITIALIZED); // Wont inform any duke about this step atm.
		setState(INITIALIZING);
		// do initialization.
		log.info("[" + getTxID() + "] Changing state from " + getNukeInfo().getState() + " to " + NukeState.RUNNING + ".");
		getNukeInfo().setState(NukeState.RUNNING);
		setState(RUNNING);
	}
	
	protected void handleUnresponsive() {
		log.trace("handleUnresponsive()");
		log.fatal("[" + getTxID() + "] Not implemented handleUnresponsive.");
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
	
  /**
   * @param stopped the stopped to set
   */
  public void setStopped(boolean stopped) {
  	this.stopped = stopped;
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public boolean isStopped() {
	  return this.stopped;
  }
	
  /**
   * @param terminated the terminated to set
   */
  public void setTerminated(boolean terminated) {
  	this.terminated = terminated;
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public boolean isTerminated() {
	  return this.terminated;
  }

}
