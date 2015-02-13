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

public interface IProcedureWait {
	
	/**
	 * Method is called when a procedure is finished with its identity and current state.
	 * @param identity long with the identity of the procedure, if more than one.
	 * @param state int with the state the procedure terminated at.
	 * {@code
	 * public ProcToWaitFor(long identity, IProcedureWait callback) {
	 *    ...
	 * }
	 * ...
	 * public void shutDown() {
	 *    callback.procedureFinished(identity, getState());
	 * }
	 * ...
	 * }
	 */
	public abstract void procedureFinished(long identity, int state);

}
