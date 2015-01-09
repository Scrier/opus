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
package io.github.scrier.opus.common.nuke;

import io.github.scrier.opus.common.aoc.BaseNukeC;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class NukeInfo extends BaseNukeC {

	private static Logger log = LogManager.getLogger(NukeInfo.class);

	private long nukeID;
	private int numberOfThreads;
	private int requestedThreads;
	private boolean repeated;
	private NukeState state;
	private int activeCommands;
	private int requestedCommands;
	private int completedCommands;

	public static final long NUKE_ID_MODIFIED            = 0x0000000000000001L;
	public static final long NUMBER_OF_THREADS_MODIFIED  = 0x0000000000000002L;
	public static final long REQUESTED_THREADS_MODIFIED  = 0x0000000000000004L;
	public static final long REPEATED_MODIFIED           = 0x0000000000000008L;
	public static final long STATE_MODIFIED              = 0x0000000000000010L;
	public static final long ACTIVE_COMMANDS_MODIFIED    = 0x0000000000000020L;
	public static final long REQUESTED_COMMANDS_MODIFIED = 0x0000000000000040L;
	public static final long COMPLETED_COMMANDS_MODIFIED = 0x0000000000000080L;

	private long valuesModified;

	public NukeInfo() {
		super(NukeFactory.FACTORY_ID, NukeFactory.NUKE_INFO);
		log.trace("NukeInfo()");
		nukeID = 0L;
		numberOfThreads = 0;
		requestedThreads = 0;
		repeated = false;
		state = NukeState.UNDEFINED;
		setValuesModified(0L);
	}

	public NukeInfo(NukeInfo obj2copy) {
		super(obj2copy);
		log.trace("NukeInfo(" + obj2copy + ")");
		setNukeID(obj2copy.getNukeID());
		setNumberOfThreads(obj2copy.getNumberOfThreads());
		setRequestedThreads(obj2copy.getRequestedThreads());
		setRepeated(obj2copy.isRepeated());
		setState(obj2copy.getState());
		setActiveCommands(obj2copy.getActiveCommands());
		setRequestedCommands(obj2copy.getRequestedCommands());
		setCompletedCommands(obj2copy.getCompletedCommands());
	}

	public NukeInfo(BaseNukeC input) throws ClassCastException {
		super(input);
		if( input instanceof NukeInfo ) {
			NukeInfo obj2copy = (NukeInfo)input;
			setNukeID(obj2copy.getNukeID());
			setNumberOfThreads(obj2copy.getNumberOfThreads());
			setRequestedThreads(obj2copy.getRequestedThreads());
			setRepeated(obj2copy.isRepeated());
			setState(obj2copy.getState());
			setActiveCommands(obj2copy.getActiveCommands());
			setRequestedCommands(obj2copy.getRequestedCommands());
			setCompletedCommands(obj2copy.getCompletedCommands());
		} else {
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof NukeInfo[" + NukeFactory.NUKE_INFO + "], are you using correct class?");
		}
	}

	public void resetValuesModified() {
		setValuesModified(0L);
	}

	public boolean isValueModified(long value) {
		return (value & getValuesModified()) > 0; 
	}
	
	public boolean isValuesModified() {
		return 0 != getValuesModified();
	}
	
	public long compare(NukeInfo obj2compare) {
		long retValue = 0L;
		retValue |= ( getNukeID() != obj2compare.getNukeID() ) ? NUKE_ID_MODIFIED : 0L;
		retValue |= ( getNumberOfThreads() != obj2compare.getNumberOfThreads() ) ? NUMBER_OF_THREADS_MODIFIED : 0L;
		retValue |= ( getRequestedThreads() != obj2compare.getRequestedThreads() ) ? REQUESTED_THREADS_MODIFIED : 0L;
		retValue |= ( isRepeated() != obj2compare.isRepeated() ) ? REPEATED_MODIFIED : 0L;
		retValue |= ( getState() != obj2compare.getState() ) ? STATE_MODIFIED : 0L;
		retValue |= ( getActiveCommands() != obj2compare.getActiveCommands() ) ? ACTIVE_COMMANDS_MODIFIED : 0L;
		retValue |= ( getRequestedCommands() != obj2compare.getRequestedCommands() ) ? REQUESTED_COMMANDS_MODIFIED : 0L;
		retValue |= ( getCompletedCommands() != obj2compare.getCompletedCommands() ) ? COMPLETED_COMMANDS_MODIFIED : 0L;
		return retValue;
	}

	private void addValueModified(long value) {
		setValuesModified(getValuesModified() | value); 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		log.trace("readData(" + in + ")");
		super.readData(in);
		setNukeID(in.readLong());
		setNumberOfThreads(in.readInt());
		setRequestedThreads(in.readInt());
		setRepeated(in.readBoolean());
		setState(NukeState.valueOf(in.readUTF()));
		setActiveCommands(in.readInt());
		setRequestedCommands(in.readInt());
		setCompletedCommands(in.readInt());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		log.trace("writeData(" + out + ")");
		super.writeData(out);
		out.writeLong(getNukeID());
		out.writeInt(getNumberOfThreads());
		out.writeInt(getRequestedThreads());
		out.writeBoolean(isRepeated());
		out.writeUTF(getState().toString());
		out.writeInt(getActiveCommands());
		out.writeInt(getRequestedCommands());
		out.writeInt(getCompletedCommands());
	}

	/**
	 * @return the nukeID
	 */
	public long getNukeID() {
		return nukeID;
	}

	/**
	 * @param nukeID the nukeID to set
	 */
	public void setNukeID(long nukeID) {
		if( this.nukeID != nukeID ) {
			this.nukeID = nukeID;
			addValueModified(NUKE_ID_MODIFIED);
		}
	}

	/**
	 * @return the numberOfUsers
	 */
	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	/**
	 * @param numberOfUsers the numberOfUsers to set
	 */
	public void setNumberOfThreads(int numberOfUsers) {
		if( this.numberOfThreads != numberOfUsers ) {
			this.numberOfThreads = numberOfUsers;
			addValueModified(NUMBER_OF_THREADS_MODIFIED);
		}
	}

	/**
	 * @return the requestedUsers
	 */
	public int getRequestedThreads() {
		return requestedThreads;
	}

	/**
	 * @param requestedUsers the requestedUsers to set
	 */
	public void setRequestedThreads(int requestedUsers) {
		if( this.requestedThreads != requestedUsers ) {
			this.requestedThreads = requestedUsers;
			addValueModified(REQUESTED_THREADS_MODIFIED);
		}
	}

	/**
	 * @return the repeated
	 */
	public boolean isRepeated() {
		return repeated;
	}

	/**
	 * @param repeated the repeated to set
	 */
	public void setRepeated(boolean repeated) {
		if( this.repeated != repeated ) {
			this.repeated = repeated;
			addValueModified(REPEATED_MODIFIED);
		}
	}

	/**
	 * @return the state
	 */
	public NukeState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(NukeState state) {
		if( this.state != state ) {
			this.state = state;
			addValueModified(STATE_MODIFIED);
		}
	}

	/**
	 * @return the activeCommands
	 */
	public int getActiveCommands() {
		return activeCommands;
	}

	/**
	 * @param activeCommands the activeCommands to set
	 */
	public void setActiveCommands(int activeCommands) {
		if( this.activeCommands != activeCommands ) {
			this.activeCommands = activeCommands;
			addValueModified(ACTIVE_COMMANDS_MODIFIED);
		}
	}

	/**
	 * @return the requestedCommands
	 */
	public int getRequestedCommands() {
		return requestedCommands;
	}

	/**
	 * @param requestedCommands the requestedCommands to set
	 */
	public void setRequestedCommands(int requestedCommands) {
		if( this.requestedCommands != requestedCommands ) {
			this.requestedCommands = requestedCommands;
			addValueModified(REQUESTED_COMMANDS_MODIFIED);
		}
	}

	/**
	 * @return the completedCommands
	 */
	public int getCompletedCommands() {
		return completedCommands;
	}

	/**
	 * @param completedCommands the completedCommands to set
	 */
	public void setCompletedCommands(int completedCommands) {
		if( this.completedCommands != completedCommands ) {
			this.completedCommands = completedCommands;
			addValueModified(COMPLETED_COMMANDS_MODIFIED);
		}
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

	@Override
	public String toString() {
		return "NukeInfo: {nukeID:"+nukeID+", numberOfThreads:"+numberOfThreads+ ", requestedThreads:"+requestedThreads+
				", repeated:"+repeated+", state:"+state+", activeCommands:"+activeCommands+", requestedCommands:"+
				requestedCommands+", completedCommands:"+completedCommands+"}";
	}

}
