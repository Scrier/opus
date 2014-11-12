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

	private List<BaseProcedure> procedures;
	private List<BaseProcedure> proceduresToAdd;
	private List<BaseProcedure> toRemove;

	public DukeCommander(HazelcastInstance instance) {
		super(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		procedures = new ArrayList<BaseProcedure>();
		proceduresToAdd = new ArrayList<BaseProcedure>();
		toRemove = new ArrayList<BaseProcedure>();
	}
	
	public void init() {
		log.trace("init()");
		log.debug("Size is: " + getEntries().size() );
		for( BaseNukeC nuke : getEntries() ) {
			log.debug("Instance is: " + nuke + ".");
			if( NukeFactory.NUKE_INFO == nuke.getId() ) {
				log.debug("Register Procedure");
				registerProcedure(new NukeProcedure(new NukeInfo(nuke)));
			}
		}
	}
	
	public void shutDown() {
		log.trace("shutDown()");
		clear(getProceduresToAdd());
		clear(getProcedures());
		clear(getProceduresToRemove());
	}
	
	public boolean registerProcedure(BaseProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		boolean retValue = true;
		if( contains(procedure) ) {
			retValue = false;
		} else {
			retValue = proceduresToAdd.add(procedure);
		}
		return retValue;
	}
	
	public void clear(List<BaseProcedure> toClear) {
		log.trace("clear(" + toClear + ")");
		for( BaseProcedure procedure : toClear ) {
			try {
	      procedure.shutDown();
      } catch (Exception e) {
      	log.error("shutDown of procedure: " + procedure + " threw Exception", e);
      }
		}
		toClear.clear();
	}

	private boolean contains(BaseProcedure procedure) {
		log.trace("contains(" + procedure + ")");
		boolean retValue = procedures.contains(procedure);
		retValue = ( true == retValue ) ? true : proceduresToAdd.contains(procedure);
		return retValue;
	}

	private void removeProcedure(BaseProcedure procedure) {
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
	  for( BaseProcedure procedure : getProceduresToAdd() ) {
	  	try {
	      procedure.init();
	      procedures.add(procedure);
      } catch (Exception e) {
	      log.error("init of procedure: " + procedure + " threw Exception", e);
      }
	  }
	  proceduresToAdd.clear();
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
	}	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void entryEvicted(Long component, BaseNukeC data) {
		log.trace("entryEvicted(" + component + ", " + data + ")");
		for( BaseProcedure procedure : getProcedures() ) {
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
		for( BaseProcedure procedure : getProcedures() ) {
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
		for( BaseProcedure procedure : getProcedures() ) {
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
	  for( BaseProcedure procedure : getProceduresToRemove() ) {
	  	try {
	      procedure.shutDown();
	      procedures.remove(procedure);
      } catch (Exception e) {
      	log.error("shutDown of procedure: " + procedure + " threw Exception", e);
      }
	  }
	  toRemove.clear();
  }
	
	/**
	 * @return the procedures
	 */
	protected List<BaseProcedure> getProcedures() {
		return procedures;
	}
	
	/**
	 * @return the proceduresToAdd
	 */
	protected List<BaseProcedure> getProceduresToAdd() {
		return proceduresToAdd;
	}
	
	/**
	 * @return the toRemove
	 */
	protected List<BaseProcedure> getProceduresToRemove() {
		return toRemove;
	}

}
