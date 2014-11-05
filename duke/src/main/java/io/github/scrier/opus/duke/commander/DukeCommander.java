package io.github.scrier.opus.duke.commander;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.aoc.BaseListener;
import io.github.scrier.opus.common.aoc.BaseNukeC;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

public class DukeCommander extends BaseListener {
	
	private static Logger log = LogManager.getLogger(DukeCommander.class);
	private static Long sendToAll = -123456L;
	
	private Map<Long, List<BaseProcedure>> proceduresMap;
	private Map<Long, List<BaseProcedure>> proceduresToAdd;
	private List<BaseProcedure> toRemove;
	
	public DukeCommander() {
		proceduresMap = new HashMap<Long, List<BaseProcedure>>();
		proceduresToAdd = new HashMap<Long, List<BaseProcedure>>();
		toRemove = new ArrayList<BaseProcedure>();
	}
	
	public boolean registerProcedure(BaseProcedure procedure) {
		log.trace("registerProcedure(" + procedure + ")");
		boolean retValue = true;
		if( contains(procedure) ) {
			retValue = false;
		} else {
			retValue = addProcedure(proceduresToAdd, sendToAll, procedure);
		}
		return retValue;
	}
	
	public boolean registerProcedure(BaseProcedure procedure, Long componentID) {
		log.trace("registerProcedure(" + procedure + ", " + componentID + ")");
		boolean retValue = true;
		if( contains(procedure) ) {
			retValue = false;
		} else {
			retValue = addProcedure(proceduresToAdd, componentID, procedure);
		}
		return retValue;
	}
	
	private boolean contains(BaseProcedure procedure) {
		log.trace("contains(" + procedure + ")");
		boolean retValue = contains(proceduresMap, procedure);
		retValue = ( true == retValue ) ? true : contains(proceduresToAdd, procedure);
		return retValue;
	}
	
	private boolean contains(Map<Long, List<BaseProcedure>> map, BaseProcedure procedure) {
		boolean retValue = false;
		for( List<BaseProcedure> list : map.values() ) {
			if( list.contains(procedure) ) {
				log.error("Procedure: " + procedure + " already exists in " + map + ".");
				retValue = true;
				break;
			}
		}
		return retValue;
	}
	
	private boolean addProcedure(Map<Long, List<BaseProcedure>> map, Long componentID, BaseProcedure procedure) {
		log.trace("addProcedure(" + map + ", " + componentID + ", " + procedure + ")");
		boolean retValue = true;
		if( map.containsKey(componentID) ) {
			List<BaseProcedure> toUpdate = map.get(componentID);
			toUpdate.add(procedure);
		} else {
			map.put(componentID, new ArrayList<BaseProcedure>());
			map.get(componentID).add(procedure);
		}
		return retValue;
	}
	
	private void preProcedureCall() {
		log.trace("preProcedureCall()");
		proceduresToAdd.clear();
		toRemove.clear();
	}
	
	private void postProcedureCall() {
		log.trace("postProcedureCall()");
		// Add procedures to the maps.
		for( Map.Entry<Long, List<BaseProcedure>> item : proceduresToAdd.entrySet() ) {
			for( BaseProcedure procedure : item.getValue() ) {
				addProcedure(proceduresMap, item.getKey(), procedure);
				try {
					procedure.init();
				} catch (Exception e) {
					log.error("Calling init on procedure " + procedure + " threw exception.", e);
					removeProcedure(procedure);
				}
			}
		}
		// Remove all procedures from map.
		ArrayList<Long> mapRemove = new ArrayList<Long>();
		for( BaseProcedure procedure : toRemove ) {
			for ( Map.Entry<Long, List<BaseProcedure>> item : proceduresMap.entrySet() ) {
				if( item.getValue().contains(procedure) ) {
					log.debug("Removing procedure " + procedure + " from map with key " + item.getKey() + ".");
					item.getValue().remove(procedure);
					if( item.getValue().isEmpty() ) {
						log.debug("Map with key " + item.getKey() + " is empty.");
						mapRemove.add(item.getKey());
					}
					try {
						procedure.shutDown();
					} catch (Exception e) {
						log.error("Calling shutDown on procedure " + procedure + " threw exception.", e);
					}
				}
			}
		}
		// Remove all empty map entries.
		for( Long key : mapRemove ) {
			log.debug("Removing key " + key + " from map.");
			proceduresMap.remove(key);
		}
	}
	
	private void removeProcedure(BaseProcedure procedure) {
		log.trace("removeProcedure(" + procedure + ")");
		toRemove.add(procedure);
	}
	
	private void removeProcedure(Long componentID) {
		log.trace("removeProcedure(" + componentID + ")");
		for( BaseProcedure procedure : proceduresMap.get(componentID) ) {
			log.debug("Adding procedure " + procedure + " to remove list.");
			toRemove.add(procedure);
		}
	}
	
	private void removeAllProcedures() {
		log.trace("removeAllProcedures()");
		removeAllProcedures(proceduresMap);
		removeAllProcedures(proceduresToAdd);
	}
	
	private void removeAllProcedures(Map<Long, List<BaseProcedure>> map) {
		log.trace("removeAllProcedures(" + map + ")");
		for( List<BaseProcedure> list : map.values() ) {
			for( BaseProcedure procedure : list ) {
				log.debug("Adding procedure " + procedure + " to remove list.");
				toRemove.add(procedure);
			}
		}
	}

	@Override
	public void mapCleared(MapEvent cleared) {
		log.trace("mapCleared(" + cleared + ")");
		log.error("Map was cleared, removing all.");
		removeAllProcedures();
	}

	@Override
	public void mapEvicted(MapEvent evicted) {
		log.trace("mapEvicted(" + evicted + ")");
		log.error("Map was evicted, removing all.");
		removeAllProcedures();
	}

	@Override
	public void entryAdded(Long component, BaseNukeC data) {
		log.trace("entryAdded(" + component + ", " + data + ")");
		switch( data.getId() ) {
			case NukeFactory.NUKE_INFO:
			{
				registerProcedure(new NukeProcedure(component, new NukeInfo(data));
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

	@Override
	public void entryEvicted(Long component, BaseNukeC data) {
		log.trace("entryEvicted(" + component + ", " + data + ")");
		preProcedureCall();
		for( BaseProcedure procedure : proceduresMap.get(evicted.getKey()) ) {
			int result = procedure.handleOnEvicted(evicted.getValue());
			if( procedure.COMPLETED == result ) {
				log.debug("Procedure " + procedure + " completed.");
				removeProcedure(procedure);
			} else if ( procedure.ABORTED == result ) {
				log.debug("Procedure " + procedure + " aborted.");
				removeProcedure(procedure);
			}
		}
		postProcedureCall();
	}

	@Override
	public void entryRemoved(Long component, BaseNukeC data) {
		log.trace("entryRemoved(" + component + ", " + data + ")");
		removeProcedure(removed.getKey());
	}

	@Override
	public void entryUpdated(Long component, BaseNukeC data) {
		log.trace("entryUpdated(" + component + ", " + data + ")");
		preProcedureCall();
		for( BaseProcedure procedure : proceduresMap.get(updated.getKey()) ) {
			int result = procedure.handleOnUpdated(updated.getValue());
			if( procedure.COMPLETED == result ) {
				log.debug("Procedure " + procedure + " completed.");
				removeProcedure(procedure);
			} else if ( procedure.ABORTED == result ) {
				log.debug("Procedure " + procedure + " aborted.");
				removeProcedure(procedure);
			}
		}
		postProcedureCall();
	}
	
}
