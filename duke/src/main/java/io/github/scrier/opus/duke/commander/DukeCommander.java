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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.data.DataListener;
import io.github.scrier.opus.common.duke.DukeDataFactory;
import io.github.scrier.opus.common.duke.DukeInfo;
import io.github.scrier.opus.common.message.BaseMsgC;
import io.github.scrier.opus.common.nuke.NukeFactory;
import io.github.scrier.opus.common.nuke.NukeInfo;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapEvent;

public class DukeCommander extends DataListener implements IProcedureWait {

	private static Logger log = LogManager.getLogger(DukeCommander.class);

	private List<BaseDukeProcedure> procedures;
	private List<BaseDukeProcedure> proceduresToAdd;
	private List<BaseDukeProcedure> toRemove;
	private boolean distributorRunning;
	private final long procToWaitFor = 782452L;
	private boolean waitingForProcedure;

	public DukeCommander(HazelcastInstance instance) {
		super(instance, Shared.Hazelcast.BASE_NUKE_MAP);
		procedures = new ArrayList<BaseDukeProcedure>();
		proceduresToAdd = new ArrayList<BaseDukeProcedure>();
		toRemove = new ArrayList<BaseDukeProcedure>();
		setDistributorRunning(false);
		setWaitingForProcedure(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		log.trace("init()");
		log.debug("Size is: " + getEntries().size() );
		DukeInfo info = null;
		if( true == isAnotherDukeRunning(info) ) {
			registerProcedure(new TerminateDukeProcedure(procToWaitFor, this, info));
			setWaitingForProcedure(true);
		} else {
			initializeAsSingleDuke();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() {
		log.trace("shutDown()");
		clear(getProceduresToAdd());
		clear(getProcedures());
		clear(getProceduresToRemove());
	}
	
	/**
	 * Method to handle incoming messages for the procedures.
	 * @param message BaseMsgC with the incoming message.
	 */
	public void handleInMessage(BaseMsgC message) {
		log.trace("handleInMessage(" + message + ")");
		for( BaseDukeProcedure procedure : procedures ) {
			int result = procedure.handleMessage(message);
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
	 * Method to register a procedure to the working loop.
	 * @param procedure BaseDukeProcedure to add.
	 * @return boolean
	 */
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

	/**
	 * Method to clear a specified list of procedures.
	 * @param toClear List with the procedures to remove.
	 */
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

	/**
	 * Method to check if a specific procedures exists in the ones running or the ones about to be added to the run loop.
	 * @param procedure BaseDukeProcedure to add.
	 * @return boolean with true if it is contained in one of the 2 lists.
	 */
	private boolean contains(BaseDukeProcedure procedure) {
		log.trace("contains(" + procedure + ")");
		boolean retValue = procedures.contains(procedure);
		retValue = ( true == retValue ) ? true : proceduresToAdd.contains(procedure);
		return retValue;
	}

	/**
	 * Method to remove a specific procedure.
	 * @param procedure BaseDukeProcedure to remove.
	 */
	private void removeProcedure(BaseDukeProcedure procedure) {
		log.trace("removeProcedure(" + procedure + ")");
		toRemove.add(procedure);
	}

	/**
	 * Method to remove all procedures.
	 */
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
		log.trace("preEntry()");
		intializeProcedures();
		toRemove.clear();
	}

	/**
	 * Method to initialize all procedures before stepping into the next iteration. 
	 */
	public synchronized void intializeProcedures() {
		log.trace("intializeProcedures()");
		if( true != getProceduresToAdd().isEmpty() ) {
			log.debug("Adding " + getProceduresToAdd().size() + " procedures.");
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
	public void entryAdded(Long component, BaseDataC data) {
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
	public void entryEvicted(Long component, BaseDataC data) {
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
	public void entryRemoved(Long key) {
		log.trace("entryRemoved(" + key + ")");
		for( BaseDukeProcedure procedure : getProcedures() ) {
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
	public void entryUpdated(Long component, BaseDataC data) {
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
		log.trace("postEntry()");
		intializeProcedures();
		shutDownProcedures();
	}

	/**
	 * Method to shutdown the procedures waiting for termination.
	 */
	private void shutDownProcedures() {
		log.trace("shutDownProcedures()");
		for( BaseDukeProcedure procedure : getProceduresToRemove() ) {
			try {
				procedure.shutDown();
				procedures.remove(procedure);
			} catch (Exception e) {
				log.error("shutDown of procedure: " + procedure + " threw Exception", e);
			}
		}
		getProceduresToRemove().clear();
	}

	/**
	 * Method to handle post events for timers and similar events triggered outside the base methods.
	 */
	public void handlePostEntry() {
		for( BaseDukeProcedure procedure : getProcedures() ) {
			if( procedure.COMPLETED == procedure.getState() ) {
				log.debug("Procedure " + procedure + " completed.");
				removeProcedure(procedure);
			} else if ( procedure.ABORTED == procedure.getState() ) {
				log.debug("Procedure " + procedure + " aborted.");
				removeProcedure(procedure);
			}
		}
		shutDownProcedures();
	}

	/**
	 * Method to get a list of procedures of a specific class.
	 * @param procs the class to look for.
	 * @return List list with BaseDukeProcedures
	 * {@code
	 * List<BaseDukeProcedures> commandProcedures = getProcedurs(CommandProcedure.class);
	 * for( BaseDukeProcedures procedure : commandProcedures ) {
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
	 * @return List List with BaseDukeProcedure
	 * {@code
	 * List<BaseDukeProcedure> commandProcedures = getProcedurs(CommandProcedure.class);
	 * for( BaseDukeProcedure procedure : commandProcedures ) {
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
		if( isWaitingForProcedure() ) {
			log.debug("Waiting for procedure to kill other duke.");
		} else if( isDistributorRunning() ) {
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
	
	/**
	 * Method to check if another duke is running from the new duke.
	 * @return boolean
	 */
	public boolean isAnotherDukeRunning(DukeInfo info) {
		boolean retValue = false;
		for( BaseDataC data : getEntries() ) {
			if( DukeDataFactory.DUKE_INFO == data.getId() ) {
				retValue = true;
				info = (DukeInfo)data;
				break;
			}
		}
		return retValue;
	}
	
	/**
	 * Method to do initialization when we are sole duke available.
	 */
	private void initializeAsSingleDuke() {
		log.trace("initializeAsSingleDuke()");
		for( BaseDataC nuke : getEntries() ) {
			log.debug("Instance is: " + nuke + ".");
			if( NukeFactory.NUKE_INFO == nuke.getId() ) {
				log.info("Adding new NukeProcedure for NukeInfo: " + nuke + ".");
				registerProcedure(new NukeProcedure(new NukeInfo(nuke)));
			}
		}
		startDistributor();
		intializeProcedures();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void procedureFinished(long identity, int state) {
		// handle the callback from setting up things.
		
		setWaitingForProcedure(false);
		startDistributor();
	}

	/**
	 * @return the waitingForProcedure
	 */
	public boolean isWaitingForProcedure() {
		return waitingForProcedure;
	}

	/**
	 * @param waitingForProcedure the waitingForProcedure to set
	 */
	public void setWaitingForProcedure(boolean waitingForProcedure) {
		this.waitingForProcedure = waitingForProcedure;
	}

}
