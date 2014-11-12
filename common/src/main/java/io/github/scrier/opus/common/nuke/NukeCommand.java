package io.github.scrier.opus.common.nuke;

import java.io.IOException;

import io.github.scrier.opus.common.aoc.BaseNukeC;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class NukeCommand extends BaseNukeC {
	
	private static Logger log = LogManager.getLogger(NukeCommand.class);
	
	private String command;
	private String response;
	private CommandState state;
	private boolean repeated;
	
	public NukeCommand() {
		super(NukeFactory.FACTORY_ID, NukeFactory.NUKE_COMMAND);
		setCommand("");
		setResponse("");
		setState(CommandState.UNDEFINED);
		setRepeated(false);
	}
	
	public NukeCommand(NukeCommand obj2copy) {
		super(obj2copy);
		setCommand(obj2copy.getCommand());
		setResponse(obj2copy.getResponse());
		setState(obj2copy.getState());
		setRepeated(obj2copy.isRepeated());
	}
	
	public NukeCommand(BaseNukeC input) throws ClassCastException {
		super(input);
		if( input instanceof NukeCommand ) {
			NukeCommand obj2copy = (NukeCommand)input;
			setCommand(obj2copy.getCommand());
			setResponse(obj2copy.getResponse());
			setState(obj2copy.getState());
			setRepeated(obj2copy.isRepeated());
		} else {
			throw new ClassCastException("Data with id " + input.getId() + " is not an instanceof NukeCommand[" + NukeFactory.NUKE_COMMAND + "], are you using correct class?");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		log.trace("readData(" + in + ")");
		setCommand(in.readUTF());
		setResponse(in.readUTF());
		setState(CommandState.valueOf(in.readUTF()));
		setRepeated(in.readBoolean());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		log.trace("writeData(" + out + ")");
		out.writeUTF(getCommand());
		out.writeUTF(getResponse());
		out.writeUTF(getState().toString());
		out.writeBoolean(isRepeated());
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(String response) {
		this.response = response;
	}

	/**
	 * @return the state
	 */
	public CommandState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(CommandState state) {
		this.state = state;
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
		this.repeated = repeated;
	}
	
}
