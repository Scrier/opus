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
package io.github.scrier.opus;

import io.github.scrier.opus.common.Constants;
import io.github.scrier.opus.duke.commander.ICommandCallback;

public class ICommandCallbackImpl implements ICommandCallback {
	
	public long NukeID;
	public long ProcessID;
	public int State;
	public String Query;
	public String Result;
	
	public ICommandCallbackImpl() {
		resetValues();
	} 
	
	public void resetValues() {
		NukeID = Constants.HC_UNDEFINED;
		ProcessID = Constants.HC_UNDEFINED;
		State = -1;
		Query = "";
		Result = "";
	}

	@Override
  public void finished(long nukeID, long processID, int state, String query, String result) {
	  NukeID = nukeID;
	  ProcessID = processID;
	  State = state;
	  Query = query;
	  Result = result;
  }

}
