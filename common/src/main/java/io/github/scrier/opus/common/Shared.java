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

public class Shared {
	public static class Hazelcast {
		public static final String BASE_NUKE_MAP = "hazelcast-base-nuke-map";
		public static final String SETTINGS_MAP = "hazelcast-settings-map";

		public static final String COMMON_NODE_ID = "hazelcast-node-id";
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
	}
	
	public static class Commands {
		public static final String GIT_CHECKOUT = "git-checkout";
	}
}