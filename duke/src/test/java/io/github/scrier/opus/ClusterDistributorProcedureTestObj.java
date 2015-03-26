package io.github.scrier.opus;

import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;

public class ClusterDistributorProcedureTestObj extends ClusterDistributorProcedure {
	
	public int TimeoutTime;
	public long TimeoutTimerID;
	public int TimeoutCalls;
	public boolean nukesReady;
	public boolean timeoutActive;
	public int NewState;
	public int PreviousState;

	public ClusterDistributorProcedureTestObj() {
		getStates()[ABORTED] = new StateImpl(this);
		getStates()[CREATED] = new StateImpl(this);
		getStates()[WAITING_FOR_NUKE] = new StateImpl(this);
		getStates()[RAMPING_UP] = new StateImpl(this); 
		getStates()[PEAK_DELAY] = new StateImpl(this);
		getStates()[RAMPING_DOWN] = new StateImpl(this);
		getStates()[TERMINATING] = new StateImpl(this);
		this.TimeoutCalls = 0;
		this.nukesReady = false;
		this.timeoutActive = false;
		reset();
	}
	
	
	
	public void reset() {
		this.TimeoutTime = 0;
		this.TimeoutTimerID = 0L;
		this.NewState = -1;
		this.PreviousState = -1;
	}
	
	@Override
	public void onStateChanged(int newState, int previousState) {
		NewState = newState;
		PreviousState = previousState;
	}
	
	@Override
	public void startTimeout(int time, long timerID) {
		this.TimeoutCalls++;
	  this.TimeoutTime = time;
	  this.TimeoutTimerID = timerID;
	}
	
	@Override
	public boolean isTimeoutActive(long id) {
		return timeoutActive;
	}
	
	@Override
	public boolean isNukesReady() {
	  return nukesReady;
	}
	
}
