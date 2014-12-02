package io.github.scrier.opus.duke.commander;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseListener;
import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapEvent;

public class DukeCommander extends BaseListener {

	private static Logger log = LogManager.getLogger(DukeCommander.class);

	private List<BaseDukeProcedure> procedures;
	private List<BaseDukeProcedure> proceduresToAdd;
	private List<BaseDukeProcedure> toRemove;
	private boolean distributorRunning;

	public DukeCommander(HazelcastInstance instance) {
		super(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		procedures = new ArrayList<BaseDukeProcedure>();
		proceduresToAdd = new ArrayList<BaseDukeProcedure>();
		toRemove = new ArrayList<BaseDukeProcedure>();
		setDistributorRunning(false);
	}
	
	public void init() {
		log.trace("init()");
		log.debug("Size is: " + getEntries().size() );
		for( BaseNukeC nuke : getEntries() ) {
			log.debug("Instance is: " + nuke + ".");
			if( NukeFactory.NUKE_INFO == nuke.getId() ) {
				log.info("Adding new NukeProcedure for NukeInfo: " + nuke + ".");
				registerProcedure(new NukeProcedure(new NukeInfo(nuke)));
			}
		}
		startDistributor();
		intializeProcedures();
	}
	
	public void shutDown() {
		log.trace("shutDown()");
		clear(getProceduresToAdd());
		clear(getProcedures());
		clear(getProceduresToRemove());
	}
	
	public boolean registerProcedure(BaseDukeProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		boolean retValue = true;
		if( contains(procedure) ) {
			retValue = false;
		} else {
			retValue = proceduresToAdd.add(procedure);
		}
		return retValue;
	}
	
	public void clear(List<BaseDukeProcedure> toClear) {
		log.trace("clear(" + toClear + ")");
		for( BaseDukeProcedure procedure : toClear ) {
			try {
	      procedure.shutDown();
      } catch (Exception e) {
      	log.error("shutDown of procedure: " + procedure + " threw Exception", e);
      }
		}
		toClear.clear();
	}

	private boolean contains(BaseDukeProcedure procedure) {
		log.trace("contains(" + procedure + ")");
		boolean retValue = procedures.contains(procedure);
		retValue = ( true == retValue ) ? true : proceduresToAdd.contains(procedure);
		return retValue;
	}

	private void removeProcedure(BaseDukeProcedure procedure) {
		log.trace("removeProcedure(" + procedure + ")");
		toRemove.add(procedure);
	}

	private void removeAllProcedures() {
		log.trace("removeAllProcedures()");
		clear(getProcedures());
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

	@Override
  public void preEntry() {
		intializeProcedures();
	  toRemove.clear();
  }

	public synchronized void intializeProcedures() {
		log.trace("intializeProcedures()");
		if( true != getProceduresToAdd().isEmpty() ) {
			log.info("Adding " + getProceduresToAdd().size() + " procedures.");
			for( BaseDukeProcedure procedure : getProceduresToAdd() ) {
				try {
					procedure.init();
					procedures.add(procedure);
				} catch (Exception e) {
					log.error("init of procedure: " + procedure + " threw Exception", e);
				}
			}
			getProceduresToAdd().clear();
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
				log.info("Adding new NukeProcedure for NukeInfo: " + data + ".");
				registerProcedure(new NukeProcedure(new NukeInfo(data)));
				break;
			}
			case NukeFactory.NUKE_COMMAND: 
			{
				// do nothing
				break;
			}
			default:
			{
				log.error("Unknown id of data handler with id: " + data.getId() + ".");
				break;
			}
		}
		startDistributor();
	}	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void entryEvicted(Long component, BaseNukeC data) {
		log.trace("entryEvicted(" + component + ", " + data + ")");
		for( BaseDukeProcedure procedure : getProcedures() ) {
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
	public void entryRemoved(Long component, BaseNukeC data) {
		log.trace("entryRemoved(" + component + ", " + data + ")");
		for( BaseDukeProcedure procedure : getProcedures() ) {
			int result = procedure.handleOnRemoved(data);
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
		for( BaseDukeProcedure procedure : getProcedures() ) {
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
		intializeProcedures();
	  for( BaseDukeProcedure procedure : getProceduresToRemove() ) {
	  	try {
	      procedure.shutDown();
	      procedures.remove(procedure);
      } catch (Exception e) {
      	log.error("shutDown of procedure: " + procedure + " threw Exception", e);
      }
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
	public List<BaseDukeProcedure> getProcedures(Class<?> procs) {
		List<BaseDukeProcedure> retVal = new ArrayList<BaseDukeProcedure>();
		for( BaseDukeProcedure procedure : getProcedures() ) {
			if( procs.getName() == procedure.getClass().getName() ) {
				retVal.add(procedure);
			}
		}
		return retVal;
	}
	
	/**
	 * @return the procedures
	 */
	protected List<BaseDukeProcedure> getProcedures() {
		return procedures;
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
	public List<BaseDukeProcedure> getProceduresToAdd(Class<?> procs) {
		List<BaseDukeProcedure> retVal = new ArrayList<BaseDukeProcedure>();
		for( BaseDukeProcedure procedure : getProceduresToAdd() ) {
			if( procs.getName() == procedure.getClass().getName() ) {
				retVal.add(procedure);
			}
		}
		return retVal;
	}
	
	/**
	 * @return the proceduresToAdd
	 */
	protected List<BaseDukeProcedure> getProceduresToAdd() {
		return proceduresToAdd;
	}
	
	/**
	 * @return the toRemove
	 */
	protected List<BaseDukeProcedure> getProceduresToRemove() {
		return toRemove;
	}
	
	/**
	 * Method to check and start distributor procedure when initialized.
	 */
	private void startDistributor() {
		log.trace("startDistributor()");
		if( isDistributorRunning() ) {
			log.debug("Distributor already running");
		} else if ( getProcedures(NukeProcedure.class).isEmpty() &&
				        getProceduresToAdd(NukeProcedure.class).isEmpty() ) {
			log.info("No nuke procedures running, waiting for first nuke to connect.");
		} else {
			log.info("Starting cluster distribution.");
			setDistributorRunning(true);
			registerProcedure(new ClusterDistributorProcedure());
		}
	}

	/**
	 * @return the distributorRunning
	 */
  public boolean isDistributorRunning() {
	  return distributorRunning;
  }

	/**
	 * @param distributorRunning the distributorRunning to set
	 */
  public void setDistributorRunning(boolean distributorRunning) {
	  this.distributorRunning = distributorRunning;
  }

}
