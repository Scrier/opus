package io.github.scrier.opus;

import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;

public class ClusterDistributorProcedureTestObj extends ClusterDistributorProcedure {
	
	public int TimeoutTime;
	public long TimeoutTimerID;
	public int TimeoutCalls;

	public ClusterDistributorProcedureTestObj() {
		getStates()[ABORTED] = new StateImpl(this);
		getStates()[CREATED] = new StateImpl(this);
		getStates()[WAITING_FOR_NUKE] = new StateImpl(this);
		getStates()[RAMPING_UP] = new StateImpl(this); 
		getStates()[PEAK_DELAY] = new StateImpl(this);
		getStates()[RAMPING_DOWN] = new StateImpl(this);
		getStates()[TERMINATING] = new StateImpl(this);
		this.TimeoutCalls = 0;
		reset();
	}
	
	public void reset() {
		this.TimeoutTime = 0;
		this.TimeoutTimerID = 0L;
	}
	
	@Override
	public void startTimeout(int time, long timerID) {
		this.TimeoutCalls++;
	  this.TimeoutTime = time;
	  this.TimeoutTimerID = timerID;
	}
	
}
