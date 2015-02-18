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
package io.github.scrier.opus.duke.commander;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.common.nuke.NukeState;

public class NukeProcedure extends BaseDukeProcedure implements INukeInfo {
	
	private static Logger log = LogManager.getLogger(NukeProcedure.class);
	
	private NukeInfo local;
	private boolean publishToMap;
	private int requestedNumberOfThreads;
	
	public final int INITIALIZING = CREATED + 1;
	public final int WORKING      = CREATED + 2;
	
	public NukeProcedure(NukeInfo info) {
		log.trace("NukeProcedure(" + info + ")");
		local = new NukeInfo(info);
		setPublishToMap(false);
		setRequestedNoOfThreads(0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		log.trace("init()");
		if( true == theContext.addNuke(local.getNukeID(), this) ) {
			handleState(local.getState());
			setState(INITIALIZING);
		} else {
			log.error("[" + getTxID() + "] Unable to add " + this + " to the common map.");
			setState(ABORTED);
		}
		if( isPublishToMap() ) {
			updateEntry(local);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() throws Exception {
		log.trace("shutDown()");
		if( true != theContext.removeNuke(local.getNukeID(), this) ) {
			throw new InvalidOperationException("Unable to remove " + this + " from shared map.");
		}
		local = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnUpdated(BaseDataC data) {
		log.trace("handleOnUpdated(" + data + ")");
		if( local.getKey() == data.getKey()) {
			setPublishToMap(false);
			switch( data.getId() ) {
				case NukeFactory.NUKE_INFO:
				{
					NukeInfo info = new NukeInfo(data);
					handleUpdated(info);
					break;
				}
				case NukeFactory.NUKE_COMMAND: 
				default:
				{
					// do nothing.
					break;
				}
			}
			if( isPublishToMap() ) {
				updateEntry(local);
			}
		}
		return getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnEvicted(BaseDataC data) {
		log.trace("handleOnEvicted(" + data + ")");
		switch( data.getId() ) {
			case NukeFactory.NUKE_INFO:
			{
				NukeInfo info = new NukeInfo(data);
				handleEvicted(info);
				break;
			}
			case NukeFactory.NUKE_COMMAND: 
			default:
			{
				// do nothing.
				break;
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
		if( getIdentity() == key ) {
			log.error("[" + getTxID() + "] Entry: " + key + " was removed, aborting handler for it.");
			setState(ABORTED);
		}
	  return getState();
  }
	
	private void handleUpdated(NukeInfo info) {
		log.trace("handleUpdated(" + info + ")");
		long modified = local.compare(info);
		if( 0 < ( NukeInfo.NUKE_ID_MODIFIED & modified ) ) {
			log.error("[" + getTxID() + "] Received modified id of the NukeInfo object. cannot continue.");
			setState(ABORTED);
		} else if( local.getNukeID() == info.getNukeID() ){
			if( 0 < ( NukeInfo.ACTIVE_COMMANDS_MODIFIED & modified ) ) {
				log.debug("[" + getTxID() + "] Active commands changed from " + local.getActiveCommands() + " to " + info.getActiveCommands() + ".");
				local.setActiveCommands(info.getActiveCommands());
			}
			if( 0 < ( NukeInfo.COMPLETED_COMMANDS_MODIFIED & modified ) ) {
				log.debug("[" + getTxID() + "] Completed commands changed from " + local.getCompletedCommands() + " to " + info.getCompletedCommands() + ".");
				local.setCompletedCommands(info.getCompletedCommands());
			}
			if( 0 < ( NukeInfo.NUMBER_OF_THREADS_MODIFIED & modified ) ) {
				log.debug("[" + getTxID() + "] Number of users changed from " + local.getNumberOfThreads() + " to " + info.getNumberOfThreads() + ".");
				local.setNumberOfThreads(info.getNumberOfThreads());
			}
			if( 0 < ( NukeInfo.REPEATED_MODIFIED & modified ) ) {
				log.debug("[" + getTxID() + "] Repeated changed from " + local.isRepeated() + " to " + info.isRepeated() + ".");
				local.setRepeated(info.isRepeated());
			}
			if( 0 < ( NukeInfo.REQUESTED_COMMANDS_MODIFIED & modified ) ) {
				log.debug("[" + getTxID() + "] Requested commands changed from " + local.getRequestedCommands() + " to " + info.getRequestedCommands() + ".");
				local.setRequestedCommands(info.getRequestedCommands());
			}
			if( 0 < ( NukeInfo.REQUESTED_THREADS_MODIFIED & modified ) ) {
				log.debug("[" + getTxID() + "] Requested users changed from " + local.getRequestedThreads() + " to " + info.getRequestedThreads() + ".");
				log.error("Skipping to set requested users to the global state due to issue #16, this needs to be fixed for the next release");
				//local.setRequestedUsers(info.getRequestedUsers());
			}
			if( 0 < ( NukeInfo.STATE_MODIFIED & modified ) ) {
				log.debug("[" + getTxID() + "] State changed from " + local.getState() + " to " + info.getState() + ".");
				handleState(info.getState());
			}
			info.resetValuesModified();
		} else {
			// do nothing, another nuke procedure will take care of it.
		}
	}
	
	private void handleEvicted(NukeInfo info) {
		log.trace("handleEvicted(" + info + ")");
		if( getIdentity() == info.getNukeID() ) {
			log.error("[" + getTxID() + "] Entry " + info + " was evicted, aborting handler for it.");
			setState(ABORTED);
		}
	}
	
	protected void handleState(NukeState state) {
		log.trace("handleState(" + state + ")");
		switch( state ) {
			case ABORTED: {
				log.info("[" + getTxID() + "] Node " + getNukeID() + " aborted.");
				setState(ABORTED);
				local.setState(NukeState.ABORTED);
				break;
			}
			case COMPLETED: {
				log.info("[" + getTxID() + "] Node " + getNukeID() + " completed.");
				setState(COMPLETED);
				local.setState(NukeState.COMPLETED);
				break;
			}
			case AVAILABLE: {
				log.info("[" + getTxID() + "] Node " + getNukeID() + " is available, taking it.");
				local.setState(NukeState.TAKEN);
				setPublishToMap(true);
				break;
			}
			case INTITIALIZED: {
				log.info("[" + getTxID() + "] Node " + getNukeID() + " initialized.");
				local.setState(NukeState.INTITIALIZED);
				break;
			}
			case RUNNING: {
				log.info("[" + getTxID() + "] Node " + getNukeID() + " running.");
				setState(WORKING);
				local.setState(NukeState.RUNNING);
				break;
			}
			case TAKEN:
			case UNRESPONSIVE: {
				throw new RuntimeException("Someone other that duke set the nuke in state " + state + ".");
			}
			default: {
				throw new RuntimeException("Unhandled state " + state + " from NukeInfo.");
			}
		}
	}
	
	/**
	 * @return the publishToMap
	 */
  public boolean isPublishToMap() {
	  return publishToMap;
  }

	/**
	 * @param publishToMap the publishToMap to set
	 */
  public void setPublishToMap(boolean publishToMap) {
	  this.publishToMap = publishToMap;
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public long getNukeID() {
	  return local.getNukeID();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int getNoOfThreads() {
	  return local.getNumberOfThreads();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int getRequestedNoOfThreads() {
	  return requestedNumberOfThreads;
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public NukeState getInfoState() {
	  return local.getState();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int getNoOfActiveCommands() {
	  return local.getActiveCommands();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int getNoOfRequestedCommands() {
	  return local.getRequestedCommands();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int getNoOfCompletedCommands() {
	  return local.getCompletedCommands();
  }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public void setRequestedNoOfThreads(int threads) {
		requestedNumberOfThreads = threads;
  }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getActualNoOfThreads() {
		return local.getRequestedThreads();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return local.toString();
	}

  /**
   * {@inheritDoc}
   */
	@Override
  public int handleInMessage(BaseMsgC message) {
	  // TODO Auto-generated method stub
	  return getState();
  }

}
