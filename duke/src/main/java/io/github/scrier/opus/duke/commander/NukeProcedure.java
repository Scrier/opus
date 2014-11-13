package io.github.scrier.opus.duke.commander;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.common.nuke.NukeState;

public class NukeProcedure extends BaseProcedure implements INukeInfo {
	
	private static Logger log = LogManager.getLogger(NukeProcedure.class);
	
	private NukeInfo local;
	
	private final int WORKING = CREATED + 1;
	
	public NukeProcedure(NukeInfo info) {
		log.trace("NukeProcedure(" + info + ")");
		local = new NukeInfo(info);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws Exception {
		log.trace("init()");
		setState(WORKING);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() throws Exception {
		log.trace("shutDown()");
		local = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnUpdated(BaseNukeC data) {
		log.trace("handleOnUpdated(" + data + ")");
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
		return getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int handleOnEvicted(BaseNukeC data) {
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
  public int handleOnRemoved(BaseNukeC data) {
		log.trace("handleOnRemoved(" + data + ")");
		switch( data.getId() ) {
			case NukeFactory.NUKE_INFO:
			{
				NukeInfo info = new NukeInfo(data);
				handleRemoved(info);
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
	
	private void handleUpdated(NukeInfo info) {
		log.trace("handleUpdated(" + info + ")");
		long modified = local.compare(info);
		if( 0 < ( NukeInfo.NUKE_ID_MODIFIED & modified ) ) {
			log.error("Received modified id of the NukeInfo object. cannot continue.");
			setState(ABORTED);
		} else {
			if( 0 < ( NukeInfo.ACTIVE_COMMANDS_MODIFIED & modified ) ) {
				log.debug("Active commands changed from " + local.getActiveCommands() + " to " + info.getActiveCommands() + ".");
				local.setActiveCommands(info.getActiveCommands());
			}
			if( 0 < ( NukeInfo.COMPLETED_COMMANDS_MODIFIED & modified ) ) {
				log.debug("Completed commands changed from " + local.getCompletedCommands() + " to " + info.getCompletedCommands() + ".");
				local.setCompletedCommands(info.getCompletedCommands());
			}
			if( 0 < ( NukeInfo.NUMBER_OF_USERS_MODIFIED & modified ) ) {
				log.debug("Number of users changed from " + local.getNumberOfUsers() + " to " + info.getNumberOfUsers() + ".");
				local.setNumberOfUsers(info.getNumberOfUsers());
			}
			if( 0 < ( NukeInfo.REPEATED_MODIFIED & modified ) ) {
				log.debug("Repeated changed from " + local.isRepeated() + " to " + info.isRepeated() + ".");
				local.setRepeated(info.isRepeated());
			}
			if( 0 < ( NukeInfo.REQUESTED_COMMANDS_MODIFIED & modified ) ) {
				log.debug("Requested commands changed from " + local.getRequestedCommands() + " to " + info.getRequestedCommands() + ".");
				local.setRequestedCommands(info.getRequestedCommands());
			}
			if( 0 < ( NukeInfo.REQUESTED_USERS_MODIFIED & modified ) ) {
				log.debug("Requested users changed from " + local.getRequestedUsers() + " to " + info.getRequestedUsers() + ".");
				local.setRequestedUsers(info.getRequestedUsers());
			}
			if( 0 < ( NukeInfo.STATE_MODIFIED & modified ) ) {
				log.debug("State changed from " + local.getState() + " to " + info.getState() + ".");
				local.setState(info.getState());
			}
			info.resetValuesModified();
		}
	}
	
	private void handleEvicted(NukeInfo info) {
		log.trace("handleEvicted(" + info + ")");
		if( getIdentity() == info.getNukeID() ) {
			log.error("Entry " + info + " was evicted, aborting handler for it.");
			setState(ABORTED);
		}
	}
	
	private void handleRemoved(NukeInfo info) {
		log.trace("handleRemoved(" + info + ")");
		if( getIdentity() == info.getNukeID() ) {
			log.error("Entry: " + info + " was removed, aborting handler for it.");
			setState(ABORTED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int getNoOfUsers() {
	  return local.getNumberOfUsers();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public int getRequestedNoOfUsers() {
	  return local.getRequestedUsers();
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

}
