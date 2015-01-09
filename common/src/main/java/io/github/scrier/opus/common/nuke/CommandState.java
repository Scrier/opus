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
