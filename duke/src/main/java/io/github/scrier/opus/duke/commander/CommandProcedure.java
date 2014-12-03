package io.github.scrier.opus.duke.commander;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeFactory;

public class CommandProcedure extends BaseDukeProcedure {
	
	private static Logger log = LogManager.getLogger(CommandProcedure.class);
	
	public final int WORKING = CREATED + 1;
	public final int REMOVING = CREATED + 2;
	
	private NukeCommand nukeCommand;
	private CommandState initialComand;
	
	private ICommandCallback callback;

	public CommandProcedure(long component, String command, CommandState state) {
		this(component, command, state, false, null);
	}
	
	public CommandProcedure(long component, String command, CommandState state, ICommandCallback callback) {
		this(component, command, state, false, callback);
	}
	
	public CommandProcedure(long component, String command, CommandState state, boolean repeated) {
		this(component, command, state, repeated, null);
	}
	
	public CommandProcedure(long component, String command, CommandState state, boolean repeated, ICommandCallback callback) {
		log.trace("CommandProcedure(" + component + ", " + command + ", " + state + ", " + repeated + ", " + callback + ")");
		this.nukeCommand = new NukeCommand();
		this.nukeCommand.setComponent(component);
		this.nukeCommand.setCommand(command);
		this.nukeCommand.setRepeated(repeated);
		this.nukeCommand.setState(state);
		this.nukeCommand.setTxID(getTxID());
		setInitialCommand(state);
		setCallback(callback);
	}

	@Override
	public void init() throws Exception {
		log.trace("init()");
		addEntry(getNukeCommand());
		setState(WORKING);
	}

	@Override
	public void shutDown() throws Exception {
		log.trace("shutDown()");
		if( null != getCallback() ) {
			getCallback().finished(getNukeCommand().getComponent(),
					getState(), getNukeCommand().getCommand(), getNukeCommand().getResponse());
		}
	}

	@Override
	public int handleOnUpdated(BaseNukeC data) {
		log.trace("handleOnUpdated(" + data + ")");
		switch( data.getId() ) {
			case NukeFactory.NUKE_COMMAND: {
				NukeCommand command = new NukeCommand(data);
				handleUpdate(command);
				break;
			}
			case NukeFactory.NUKE_INFO:
			default: {
				// do nothing
				break;
			}
		}
		return getState();
	}

	@Override
	public int handleOnEvicted(BaseNukeC data) {
		log.trace("handleOnEvicted(" + data + ")");
		if( getTxID() == data.getTxID() ) {
			log.error("Our command was evicted from the map, cannot continue.");
			//@TODO: Handle evicted state better by commanding a new command or similar.
			setState(ABORTED);
		}
		return getState();
	}

	@Override
	public int handleOnRemoved(Long key) {
		log.trace("handleOnRemoved(" + key + ")");
		if( getNukeCommand().getKey() == key ) {
			if( REMOVING != getState() ) {
				log.error("Someone else removed our command data " + key + ", aborting.");
				//@TODO: Handle removed in wrong state better. Renew or terminate application perhaps?
				setState(ABORTED);
			} else {
				log.info("Command was removed from map, we are done.");
				setState(COMPLETED);
			}
		}
		return getState();
	}
	
	private void handleUpdate(NukeCommand command) {
		log.trace("handleUpdate(" + command + ")");
		if( getTxID() == command.getTxID() ) {
			if( getNukeCommand().getComponent() != command.getComponent() ) {
				throw new RuntimeException("Stored command: " +  getNukeCommand() + " and incoming command: " + command + " differs after txid check.");
			} else {
				switch( command.getState() ) {
					case EXECUTE:
					case QUERY: {
						log.debug("Command is execute/query.");
						break;
					} 
					case WORKING: {
						log.debug("Command is working.");
						break;
					}
					case DONE: {
						log.debug("Command is done, lets remove it.");
						removeEntry(getNukeCommand());
						setState(REMOVING);
						break;
					}
					case ABORTED: {
						log.debug("Aborted command: " + getNukeCommand() + ".");
						setState(ABORTED);
						break;
					}
					case UNDEFINED: {
						log.error("Unimplemented command " + command.getState() + ", aborting.");
						setState(ABORTED);
						break;
					}
					case STOP: {
						log.debug("Command was stopped, expected.");
						setState(COMPLETED);
						break;
					}
					default: {
						log.error("Unknown state of command " + command.getState() + ", aborting.");
						setState(ABORTED);
						break;
					}
				} // switch( command.getState() ) {
			}
		} // if( getTxID() == command.getTxID() ) {
	}
	
//  Might be used later. Therefore we don't remove it for now.
//	/**
//	 * Method to handle update of the command data and set to aborted if not working.
//	 * @param data BaseNukeC instance to update.
//	 */
//	private void updateEntry(BaseNukeC data) {
//		log.trace("handleOnRupdateEntryemoved(" + data + ")");
//		if( true != updateEntry(data, getID()) ) {
//			log.error("Unable to update entry " + data + ", aborting.");
//			setState(ABORTED);
//		}
//	}
	
	/**
	 * @return the nukeCommand
	 */
	protected NukeCommand getNukeCommand() {
		return nukeCommand;
	}

	/**
	 * @return the command
	 */
  public CommandState getInitialCommand() {
	  return initialComand;
  }

	/**
	 * @param command the command to set
	 */
  public void setInitialCommand(CommandState command) {
	  this.initialComand = command;
  }

	/**
	 * @return the callback
	 */
  public ICommandCallback getCallback() {
	  return callback;
  }

	/**
	 * @param callback the callback to set
	 */
  public void setCallback(ICommandCallback callback) {
	  this.callback = callback;
  }

}
