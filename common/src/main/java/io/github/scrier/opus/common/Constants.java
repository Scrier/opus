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
package io.github.scrier.opus.common;

public class Constants {
	
	public static final int INTERVAL = 500;
	
	public static final long HC_UNDEFINED   = -1L;
	public static final long MSG_TO_ALL     = -12345L;
	
	public static final int DUKE_DATA_START = INTERVAL * 0;
	public static final int NUKE_DATA_START = INTERVAL * 1;
	public static final int DUKE_MSG_START  = INTERVAL * 2;
	public static final int NUKE_MSG_START  = INTERVAL * 3;

}
