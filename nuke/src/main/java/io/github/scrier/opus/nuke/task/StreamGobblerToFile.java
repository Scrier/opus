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
package io.github.scrier.opus.nuke.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamGobblerToFile extends StreamGobbler {

	private static Logger log = LogManager.getLogger(StreamGobblerToFile.class);
	
	private PrintWriter out;
	
	public StreamGobblerToFile(File target) throws IOException {
		super(null);
		// FileWriter is targeting the buffer to append (second argument)
		// BufferedWriter is because FileWriter is expensive.
		// PrintWriter is for ease of printing information to BufferedWriter
		out = new PrintWriter(new BufferedWriter(new FileWriter(target, true)));
	}

	public StreamGobblerToFile(InputStream is, File target) throws FileNotFoundException {
		super(is);
		out = new PrintWriter(target);
	}

	@Override
	public void handleLine(String line) {
		out.write(line);
	}

	@Override
  public void onExit() {
		log.trace("onExit()");
	  out.close();
  }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "StreamGobblerToFile{is: " + getInputStream() + ", out:" + out + "}";
	}

}
