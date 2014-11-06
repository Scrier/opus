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

	private Context theContext;

	public DukeCommander(HazelcastInstance instance) {
		super(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		procedures = new ArrayList<BaseProcedure>();
		proceduresToAdd = new ArrayList<BaseProcedure>();
		toRemove = new ArrayList<BaseProcedure>();
		theContext = Context.INSTANCE;
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
	
	public void init() {
		log.trace("init()");
		for( BaseNukeC nuke : getEntries() ) {
			if( NukeFactory.NUKE_INFO == nuke.getId() ) {
				registerProcedure(new NukeProcedure(new NukeInfo(nuke)));
			}
		}
	}
	
	public void shutDown() {
		log.trace("shutDown()");
		clear(proceduresToAdd);
		clear(procedures);
		clear(toRemove);
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
		procedures.remove(procedure);
	}

	private void removeAllProcedures() {
		log.trace("removeAllProcedures()");
		clear(procedures);
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
	  for( BaseProcedure procedure : proceduresToAdd ) {
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
		for( BaseProcedure procedure : procedures ) {
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
		for( BaseProcedure procedure : procedures ) {
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
		for( BaseProcedure procedure : procedures ) {
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

	@Override
  public void postEntry() {
	  for( BaseProcedure procedure : toRemove ) {
	  	try {
	      procedure.shutDown();
      } catch (Exception e) {
      	log.error("shutDown of procedure: " + procedure + " threw Exception", e);
      }
	  }
	  toRemove.clear();
  }

}
