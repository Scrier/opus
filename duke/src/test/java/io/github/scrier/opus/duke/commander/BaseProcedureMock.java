package io.github.scrier.opus.duke.commander;

import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.common.data.BaseDataC;
import io.github.scrier.opus.common.message.BaseMsgC;

public class BaseProcedureMock extends BaseDukeProcedure {
	
	private boolean initCalled;
	private boolean shutDownCalled;
	private BaseDataC onUpdated;
	private int onUpdateReturn;
	private BaseDataC onEvicted;
	private int onEvictedReturn;
	private long onRemoved;
	private int onRemovedReturn;
	private BaseMsgC handleInMessage;
	private int handleInMessageReturn;
	
	BaseProcedureMock() {
		setInitCalled(false);
		setShutDownCalled(false);
		setOnUpdated(null);
		setOnUpdateReturn(CREATED);
		setOnEvicted(null);
		setOnEvictedReturn(CREATED);
		setOnRemoved(Constants.HC_UNDEFINED);
		setOnRemovedReturn(CREATED);
		setHandleInMessage(null);
		setHandleInMessageReturn(CREATED);
	}

	@Override
  public void init() throws Exception {
	  setInitCalled(true);
  }

	@Override
  public void shutDown() throws Exception {
	  setShutDownCalled(true);
  }

	@Override
  public int handleOnUpdated(BaseDataC data) {
	  setOnUpdated(data);
	  return getOnUpdateReturn();
  }

	@Override
  public int handleOnEvicted(BaseDataC data) {
		setOnEvicted(data);
		return getOnEvictedReturn();
  }

	@Override
  public int handleOnRemoved(Long key) {
		setOnRemoved(key);
		return getOnRemovedReturn();
  }
	
	@Override
  public int handleInMessage(BaseMsgC message) {
	  setHandleInMessage(message);
	  return getHandleInMessageReturn();
  }

	/**
	 * @return the initCalled
	 */
  public boolean isInitCalled() {
	  return initCalled;
  }

	/**
	 * @param initCalled the initCalled to set
	 */
  public void setInitCalled(boolean initCalled) {
	  this.initCalled = initCalled;
  }

	/**
	 * @return the shutDownCalled
	 */
  public boolean isShutDownCalled() {
	  return shutDownCalled;
  }

	/**
	 * @param shutDownCalled the shutDownCalled to set
	 */
  public void setShutDownCalled(boolean shutDownCalled) {
	  this.shutDownCalled = shutDownCalled;
  }

	/**
	 * @return the onUpdated
	 */
	public BaseDataC getOnUpdated() {
		return onUpdated;
	}

	/**
	 * @param onUpdated the onUpdated to set
	 */
	public void setOnUpdated(BaseDataC onUpdated) {
		this.onUpdated = onUpdated;
	}

	/**
	 * @return the onUpdateReturn
	 */
	public int getOnUpdateReturn() {
		return onUpdateReturn;
	}

	/**
	 * @param onUpdateReturn the onUpdateReturn to set
	 */
	public void setOnUpdateReturn(int onUpdateReturn) {
		this.onUpdateReturn = onUpdateReturn;
	}

	/**
	 * @return the onEvicted
	 */
	public BaseDataC getOnEvicted() {
		return onEvicted;
	}

	/**
	 * @param onEvicted the onEvicted to set
	 */
	public void setOnEvicted(BaseDataC onEvicted) {
		this.onEvicted = onEvicted;
	}

	/**
	 * @return the onEvictedReturn
	 */
	public int getOnEvictedReturn() {
		return onEvictedReturn;
	}

	/**
	 * @param onEvictedReturn the onEvictedReturn to set
	 */
	public void setOnEvictedReturn(int onEvictedReturn) {
		this.onEvictedReturn = onEvictedReturn;
	}

	/**
	 * @return the onRemoved
	 */
	public long getOnRemoved() {
		return onRemoved;
	}

	/**
	 * @param onRemoved the onRemoved to set
	 */
	public void setOnRemoved(long onRemoved) {
		this.onRemoved = onRemoved;
	}

	/**
	 * @return the onRemovedReturn
	 */
	public int getOnRemovedReturn() {
		return onRemovedReturn;
	}

	/**
	 * @param onRemovedReturn the onRemovedReturn to set
	 */
	public void setOnRemovedReturn(int onRemovedReturn) {
		this.onRemovedReturn = onRemovedReturn;
	}

	/**
	 * @return the handleInMessage
	 */
  public BaseMsgC getHandleInMessage() {
	  return handleInMessage;
  }

	/**
	 * @param handleInMessage the handleInMessage to set
	 */
  public void setHandleInMessage(BaseMsgC handleInMessage) {
	  this.handleInMessage = handleInMessage;
  }

	/**
	 * @return the handleInMessageReturn
	 */
  public int getHandleInMessageReturn() {
	  return handleInMessageReturn;
  }

	/**
	 * @param handleInMessageReturn the handleInMessageReturn to set
	 */
  public void setHandleInMessageReturn(int handleInMessageReturn) {
	  this.handleInMessageReturn = handleInMessageReturn;
  }

}
