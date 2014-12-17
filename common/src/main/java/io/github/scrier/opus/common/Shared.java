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

import java.util.concurrent.TimeUnit;

public class Shared {
	public static class Hazelcast {
		public static final String BASE_NUKE_MAP = "hazelcast-base-nuke-map";
		public static final String SETTINGS_MAP = "hazelcast-settings-map";

		public static final String COMMON_MAP_UNIQUE_ID = "hazelcast-map-unique-id";
		public static final String COMMON_UNIQUE_ID = "hazelcast-unique-id";
	}
	
	public static class Settings {
		public static final String GIT_REPO = "git-repository";
		public static final String GIT_FOLDER = "git-folder";
		public static final String GIT_BRANCH = "git-branch";
		public static final String GIT_HASH = "git-hash";
		public static final String EXECUTE_MINIMUM_NODES = "execute-min-nodes";
		public static final String EXECUTE_MAX_USERS = "execute-max-users";
		public static final String EXECUTE_REPEATED = "execute-repeat";
		public static final String EXECUTE_INTERVAL = "execute-interval";
		public static final String EXECUTE_USER_INCREASE = "execute-user-inc";
		public static final String EXECUTE_PEAK_DELAY = "execute-peak-delay";
		public static final String EXECUTE_TERMINATE = "execute-terminate";
		public static final String EXECUTE_FOLDER = "execute-folder";
		public static final String EXECUTE_COMMAND = "execute-command";
		public static final String EXECUTE_GOBBLER_DIR = "execute-gobbler-dir";
		public static final String EXECUTE_GOBBLER_LEVEL = "execute-gobbler-level";
	}
	
	/**
	 * Reserved commands for the CommandState object.
	 */
	public static class Commands {
		
		public static class Execute {
			public static final String STOP_EXECUTION = "shared-commands-stop-execution";
			public static final String TERMINATE_EXECUTION = "shared-commands-terminate-execution";
		}
		
		public static class Query {
			public static final String STATUS = "shared-commands-query-status";
		}
		
	}
	
	public static class Methods {
		
		/**
		 * Method to convert seconds to hour format.
		 * @param seconds to convert
		 * @return String in format HHH:MM:SS
		 */
		public static String formatTime(int seconds) {
			return formatTime(seconds, TimeUnit.HOURS);
		}
		
		/**
		 * Method to convert seconds to a specified format.
		 * @param second to convert
		 * @param timeUnit to convert to, no bigger than days supported
		 * @return String in format according to TimeUnit
		 * {@code
		 * formatTime(70, TimeUnit.MINUTES); // <- 1:10
		 * formatTime(93915, TimeUnit.MINUTES) // <- 1565:15
		 * formatTime(Integer.MAX_VALUE, TimeUnit.DAYS); // <- 24855:03:14:07
		 * }
		 */
		public static String formatTime(int second, TimeUnit timeUnit) {
			String retValue;
			if( TimeUnit.SECONDS == timeUnit ) {
				retValue = String.format("%d", second);
			} else {
				int secondPart = second % 60;
				int minutes = second / 60;
				if( TimeUnit.MINUTES == timeUnit ) {
					retValue = String.format("%d:%02d", minutes, secondPart);
				} else {
					int minutesPart = minutes % 60;
					int hours = minutes / 60;
					if( TimeUnit.HOURS == timeUnit ) {
						retValue = String.format("%d:%02d:%02d", hours, minutesPart, secondPart);
					} else {
						int hoursPart = hours % 24;
						int days = hours / 24;
						retValue = String.format("%d:%02d:%02d:%02d", days, hoursPart, minutesPart, secondPart);
					}
				}
			}
			return retValue;
		}
	}
	
}