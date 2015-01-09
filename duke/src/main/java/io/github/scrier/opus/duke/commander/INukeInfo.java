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

import io.github.scrier.opus.common.nuke.NukeState;

public interface INukeInfo {
	
	/**
	 * Method to get the component associated with the info.
	 * @return long
	 */
	public long getNukeID();
	
	/**
	 * Method to get the number of users that is executing.
	 * @return int
	 */
	public int getNoOfThreads();
	
	/**
	 * Method to get the number of users that is requested to execute.
	 * @return int
	 */
	public int getRequestedNoOfThreads();
	
	/**
	 * Method to set the number of users that is requested to execute.
	 * Need to be removed later on as this is not the correct solution for the handling.
	 * @param threads int with the issue
	 * @since 0.1.2
	 */
	public void setRequestedNoOfThreads(int threads);
	
	/**
	 * Method to get the current state of the node.
	 * @return NukeState
	 */
	public NukeState getInfoState();
	
	/**
	 * Method to get the number of active commands that is running.
	 * @return int
	 */
	public int getNoOfActiveCommands();
	
	/**
	 * Method to get the total number of requested commands.
	 * @return int
	 */
	public int getNoOfRequestedCommands();
	
	/**
	 * Method to get the total number of completed commands.
	 * @return int
	 */
	public int getNoOfCompletedCommands();
	
}
