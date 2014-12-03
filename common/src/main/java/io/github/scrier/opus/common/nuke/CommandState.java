package io.github.scrier.opus.common.nuke;

public enum CommandState {
	UNDEFINED,	// initial state
	EXECUTE,		// command to execute, set by duke, handled by nuke.
	QUERY,			// command to query, set by duke, handled by nuke.
	WORKING,		// information from nuke, read by duke.
	STOP,				// command to stop, set by duke, handled by nuke.
	TERMINATE, 	// command to terminate, set by duke, handled by nuke.
	DONE,				// information from nuke, read by duke.
	ABORTED			// information from nuke, read by duke
}
