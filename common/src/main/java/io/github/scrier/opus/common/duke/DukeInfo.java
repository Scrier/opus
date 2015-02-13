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
package io.github.scrier.opus.common.duke;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import io.github.scrier.opus.common.data.BaseDataC;

public class DukeInfo extends BaseDataC {
	
	private static Logger log = LogManager.getLogger(DukeInfo.class);
	
	private long dukeID;
	private DukeState state;
	
	public static final long DUKE_ID_MODIFIED    = 0x0000000000000001L;
	public static final long STATE_MODIFIED      = 0x0000000000000002L;
	
	private long valuesModified;

	/**
	 * Constructor
	 */
	public DukeInfo() {
		super(DukeFactory.FACTORY_ID, DukeFactory.DUKE_INFO);
		log.trace("DukeInfo()");
		setDukeID(-1L);
		setState(DukeState.UNDEFINED);
		resetValuesModified();
	}
	
	/**
	 * Copy constructor
	 * @param obj2copy DukeInfo object
	 */
	public DukeInfo(DukeInfo obj2copy) {
		super(obj2copy);
		log.trace("DukeInfo(" + obj2copy + ")");
		setDukeID(obj2copy.getDukeID());
		setState(obj2copy.getState());
	}
	
	/**
	 * Cast constructor
	 * @param input BaseNukeC object
	 * @throws ClassCastException if provided with a mismatching class.
	 */
	public DukeInfo(BaseDataC input) throws ClassCastException {
		super(input);
		log.trace("DukeInfo(" + input + ")");
		if( input instanceof DukeInfo ) {
			DukeInfo obj2copy = (DukeInfo)input;
			setDukeID(obj2copy.getDukeID());
			setState(obj2copy.getState());
		} else {
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof DukeInfo[" + DukeFactory.DUKE_INFO + "], are you using correct class?");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		log.trace("readData(" + in + ")");
		super.readData(in);
		setDukeID(in.readLong());
		setState(DukeState.valueOf(in.readUTF()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		super.writeData(out);
		out.writeLong(getDukeID());
		out.writeUTF(getState().toString());
	}
	
	/**
	 * Method to reset all values modified, but keeping their individual values.
	 */
	public void resetValuesModified() {
		setValuesModified(0L);
	}

	/**
	 * Method to check if a values has been modified since last time.
	 * @param value the constant to check against.
	 * @return boolean
	 * {@code
	 * if( true == info.isValuesModified(DukeInfo.STATE_MODIFIED) {
	 *   // handle changed state of the DukeInfo object.
	 * }
	 * }
	 */
	public boolean isValueModified(long value) {
		return (value & getValuesModified()) > 0; 
	}
	
	/**
	 * Method to check if any value has been modified since last time you checked.
	 * @return boolean
	 */
	public boolean isValuesModified() {
		return 0 != getValuesModified();
	}
	
	/**
	 * Method to get a list of items that differs between 2 DukeInfo objects.
	 * @param obj2compare DukeInfo object.
	 * @return long
	 * {@code
	 * long diff = info.compare(otherInfo);
	 * if( 0 < ( DukeInfo.DUKE_ID_MODIFIED | long ) ) {
	 *   log.info("info has different state from otherInfo.");
	 *   ...
	 * }
	 * }
	 */
	public long compare(DukeInfo obj2compare) {
		long retValue = 0L;
		retValue |= ( getDukeID() != obj2compare.getDukeID() ) ? DUKE_ID_MODIFIED : 0L;
		retValue |= ( getState() != obj2compare.getState() ) ? STATE_MODIFIED : 0L;
		return retValue;
	}

	/**
	 * Internal method that is set if a set method is called with another value than the one set.
	 * @param value long 
	 */
	private void addValueModified(long value) {
		setValuesModified(getValuesModified() | value); 
	}

	/**
	 * @return the valuesModified
	 */
	private long getValuesModified() {
		return valuesModified;
	}

	/**
	 * @param valuesModified the valuesModified to set
	 */
	private void setValuesModified(long valuesModified) {
		this.valuesModified = valuesModified;
	}

	/**
	 * @return the dukeID
	 */
	public long getDukeID() {
		return dukeID;
	}

	/**
	 * @param dukeID the dukeID to set
	 */
	public void setDukeID(long dukeID) {
		if( this.dukeID != dukeID ) {
			this.dukeID = dukeID;
			addValueModified(DUKE_ID_MODIFIED);
		}
	}

	/**
	 * @return the state
	 */
	public DukeState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(DukeState state) {
		if( this.state != state ) {
			this.state = state;
			addValueModified(STATE_MODIFIED);
		}
	}

}
