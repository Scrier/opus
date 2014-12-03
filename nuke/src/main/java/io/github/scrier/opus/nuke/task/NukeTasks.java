package io.github.scrier.opus.nuke.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseListener;
import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.exception.InvalidOperationException;
import io.github.scrier.opus.common.nuke.CommandState;
import io.github.scrier.opus.common.nuke.NukeCommand;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;
import io.github.scrier.opus.nuke.task.procedures.ExecuteTaskProcedure;
import io.github.scrier.opus.nuke.task.procedures.QueryTaskProcedure;
import io.github.scrier.opus.nuke.task.procedures.RepeatedExecuteTaskProcedure;
import io.github.scrier.opus.nuke.task.procedures.NukeProcedure;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapEvent;

public class NukeTasks extends BaseListener {
	
	private static Logger log = LogManager.getLogger(NukeTasks.class);
	
	private Context theContext;
	private Long identity;
	
	private NukeInfo nukeInfo;
	
	private List<BaseTaskProcedure> procedures;
	private List<BaseTaskProcedure> proceduresToAdd;
	private List<BaseTaskProcedure> toRemove;
	
	public NukeTasks(HazelcastInstance instance) {
	  super(instance, Shared.Hazelcast.BASE_NUKE_MAP);
	  log.trace("NukeTasks(" + instance + ")");
	  theContext = Context.INSTANCE;
	  procedures = new ArrayList<BaseTaskProcedure>();
	  proceduresToAdd = new ArrayList<BaseTaskProcedure>();
	  toRemove = new ArrayList<BaseTaskProcedure>();
	  setNukeInfo(new NukeInfo());
  }
	
	public void init() {
		log.trace("init()");
		try {
		  setIdentity(theContext.getIdentity());
		  registerProcedure(new NukeProcedure(getIdentity()));
		} catch(InvalidOperationException e) {
	    log.error("Received InvalidOperationException when calling NukeTasks init.", e);
		}
		intializeProcedures();
	}
	
	public void shutDown() {
		log.trace("shutDown()");
		clear(getProceduresToAdd());
		clear(getProcedures());
		clear(getProceduresToRemove());
		// Remove the info about this nuke from the map.
		removeEntry(getNukeInfo());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void preEntry() {
		log.trace("preEntry()");
		// put this first so that any init method comes with the updates to nuke info.
	  getNukeInfo().resetValuesModified();
	  intializeProcedures();
	  toRemove.clear();
  }

	private synchronized void intializeProcedures() {
		log.trace("intializeProcedures()");
		if( true != getProceduresToAdd().isEmpty() ) {
			log.debug("Adding " + getProceduresToAdd().size() + " procedures.");
			for( BaseTaskProcedure procedure : getProceduresToAdd() ) {
				try {
					procedure.init();
					procedures.add(procedure);
				} catch (Exception e) {
					log.error("init of procedure: " + procedure + " threw Exception", e);
				}
			}
			proceduresToAdd.clear();
		}
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void entryAdded(Long component, BaseNukeC data) {
		log.trace("entryAdded(" + component + ", " + data + ")");
		switch( data.getId() ) {
			case NukeFactory.NUKE_INFO:
			{
				// do nothing
				break;
			}
			case NukeFactory.NUKE_COMMAND: 
			{
				NukeCommand command = new NukeCommand(data);
				handleCommand(command);
				break;
			}
			default:
			{
				log.error("Unknown id of data handler with id: " + data.getId() + ".");
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void entryEvicted(Long component, BaseNukeC data) {
		log.trace("entryEvicted(" + component + ", " + data + ")");
		for( BaseTaskProcedure procedure : getProcedures() ) {
			int result = procedure.handleOnEvicted(data);
			if( procedure.COMPLETED == result ) {
				log.debug("Procedure " + procedure + " completed.");
				removeProcedure(procedure);
			} else if ( procedure.ABORTED == result ) {
				log.debug("Procedure " + procedure + " aborted.");
				removeProcedure(procedure);
			}
		}
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void entryRemoved(Long key) {
		log.trace("entryRemoved(" + key + ")");
		for( BaseTaskProcedure procedure : getProcedures() ) {
			int result = procedure.handleOnRemoved(key);
			if( procedure.COMPLETED == result ) {
				log.debug("Procedure " + procedure + " completed.");
				removeProcedure(procedure);
			} else if ( procedure.ABORTED == result ) {
				log.debug("Procedure " + procedure + " aborted.");
				removeProcedure(procedure);
			}
		}
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void entryUpdated(Long component, BaseNukeC data) {
		log.trace("entryUpdated(" + component + ", " + data + ")");
		for( BaseTaskProcedure procedure : getProcedures() ) {
			int result = procedure.handleOnUpdated(data);
			if( procedure.COMPLETED == result ) {
				log.debug("Procedure " + procedure + " completed.");
				removeProcedure(procedure);
			} else if ( procedure.ABORTED == result ) {
				log.debug("Procedure " + procedure + " aborted.");
				removeProcedure(procedure);
			}
		}
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void postEntry() {
		log.trace("postEntry()");
	  intializeProcedures();
	  for( BaseTaskProcedure procedure : getProceduresToRemove() ) {
	  	try {
	      procedure.shutDown();
	      procedures.remove(procedure);
      } catch (Exception e) {
      	log.error("shutDown of procedure: " + procedure + " threw Exception", e);
      }
	  }
	  // Update entry in global map if change is made, put this last if shutdown method is calling them.
	  if( true == getNukeInfo().isValuesModified() ) {
	  	log.info("Updating NukeInfo with: " + getNukeInfo() + ".");
	  	updateEntry(getNukeInfo());
	  }
  }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
  public void mapCleared(MapEvent cleared) {
		log.trace("mapCleared(" + cleared + ")");
		log.error("Map was cleared, removing all.");
		removeAllProcedures();
  }

	/**
	 * {@inheritDoc}
	 */
	@Override
  public void mapEvicted(MapEvent evicted) {
		log.trace("mapEvicted(" + evicted + ")");
		log.error("Map was evicted, removing all.");
		removeAllProcedures();
  }
	
	/**
	 * Method to handle commands.
	 * @param command
	 */
	private void handleCommand(NukeCommand command) {
		log.trace("handleCommand(" + command + ")");
	  switch( command.getState() ) {
	  	case EXECUTE: {
	  		if( Shared.Commands.Execute.STOP_EXECUTION.equals(command.getCommand()) ) {
	  			log.info("Received command to stop all executions.");
	  			distributeExecuteUpdateCommands(CommandState.STOP);
	  			command.setState(CommandState.DONE);
	  			updateEntry(command);
	  		} else if ( Shared.Commands.Execute.TERMINATE_EXECUTION.equals(command.getCommand()) ) {
	  			log.info("Received command to terminate all executions.");
	  			distributeExecuteUpdateCommands(CommandState.TERMINATE);
	  			command.setState(CommandState.DONE);
	  			updateEntry(command);
	  		} else {
	  			log.info("Received common command: " + command + ".");
	  			if( command.isRepeated() ) {
	  				registerProcedure(new RepeatedExecuteTaskProcedure(command));
	  			} else {
	  				registerProcedure(new ExecuteTaskProcedure(command));
	  			}
	  		}
	  		break;
	  	}
	  	case QUERY: {
	  		registerProcedure(new QueryTaskProcedure(command));
	  		break;
	  	}
	  	case ABORTED: 
	  	case DONE: 
	  	case UNDEFINED: 
	  	case WORKING: {
	  		log.error("Unhandled data: " + command.getState() + ", from NukeCommand: " + command + ".");
	  		break;
	  	}
	  	default: {
	  		throw new RuntimeException("Unimplemented state " + command.getState() + " from class NukeCommand in class NukeTasks.");
	  	}
	  }
	}
	
	public void distributeExecuteUpdateCommands(CommandState state) {
		log.trace("distributeExecuteUpdateCommands(" + state + ")");
		for( BaseTaskProcedure proc : getProcedures(ExecuteTaskProcedure.class) ) {
			ExecuteTaskProcedure procEx = (ExecuteTaskProcedure)proc;
			NukeCommand command = procEx.getCommand();
			command.setState(state);
			procEx.updateEntry(command);
		}
		for( BaseTaskProcedure proc : getProcedures(RepeatedExecuteTaskProcedure.class) ) {
			RepeatedExecuteTaskProcedure procEx = (RepeatedExecuteTaskProcedure)proc;
			NukeCommand command = procEx.getCommand();
			command.setState(state);
			procEx.updateEntry(command);
		}
	}
	
	/**
	 * Method to get a list of procedures of a specific class.
	 * @param procs the class to look for.
	 * @return List<BaseProcedure>
	 * {@code
	 * List<BaseProcedure> commandProcedures = getProcedurs(CommandProcedure.class);
	 * for( BaseProcedure procedure : commandProcedures ) {
	 *   ...
	 * }
	 * }
	 */
	public List<BaseTaskProcedure> getProcedures(Class<?> procs) {
		List<BaseTaskProcedure> retVal = new ArrayList<BaseTaskProcedure>();
		for( BaseTaskProcedure procedure : getProcedures() ) {
			if( procs.getName() == procedure.getClass().getName() ) {
				retVal.add(procedure);
			}
		}
		return retVal;
	}
	
	/**
	 * @return the identity
	 */
	protected Long getIdentity() {
		return identity;
	}

	/**
	 * @param identity the identity to set
	 */
	private void setIdentity(Long identity) {
		this.identity = identity;
	}
	
	/**
	 * @return the procedures
	 */
	protected List<BaseTaskProcedure> getProcedures() {
		return procedures;
	}
	
	/**
	 * @return the proceduresToAdd
	 */
	protected List<BaseTaskProcedure> getProceduresToAdd() {
		return proceduresToAdd;
	}
	
	/**
	 * @return the toRemove
	 */
	protected List<BaseTaskProcedure> getProceduresToRemove() {
		return toRemove;
	}
	
	private void removeProcedure(BaseTaskProcedure procedure) {
		log.trace("removeProcedure(" + procedure + ")");
		toRemove.add(procedure);
	}
	
	public boolean registerProcedure(BaseTaskProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		boolean retValue = true;
		if( contains(procedure) ) {
			retValue = false;
		} else {
			retValue = proceduresToAdd.add(procedure);
		}
		return retValue;
	}
	
	private boolean contains(BaseTaskProcedure procedure) {
		log.trace("contains(" + procedure + ")");
		boolean retValue = procedures.contains(procedure);
		retValue = ( true == retValue ) ? true : proceduresToAdd.contains(procedure);
		return retValue;
	}
	
	private void removeAllProcedures() {
		log.trace("removeAllProcedures()");
		clear(getProcedures());
	}
	
	public void clear(List<BaseTaskProcedure> toClear) {
		log.trace("clear(" + toClear + ")");
		for( BaseTaskProcedure procedure : toClear ) {
			try {
	      procedure.shutDown();
      } catch (Exception e) {
      	log.error("shutDown of procedure: " + procedure + " threw Exception", e);
      }
		}
		toClear.clear();
	}

	/**
	 * @return the nukeInfo
	 */
  public NukeInfo getNukeInfo() {
	  return nukeInfo;
  }

	/**
	 * @param nukeInfo the nukeInfo to set
	 */
  public void setNukeInfo(NukeInfo nukeInfo) {
	  this.nukeInfo = nukeInfo;
  }

}
