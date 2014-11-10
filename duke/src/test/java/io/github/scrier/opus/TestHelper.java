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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.mockito.Mockito;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;

import io.github.scrier.opus.common.Shared;
import io.github.scrier.opus.common.aoc.BaseNukeC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public enum TestHelper {
	INSTANCE;

	private TestHelper() {

	}

	public void setLogLevel(Level level) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
		loggerConfig.setLevel(level);
		ctx.updateLoggers();
	}
	
	public HazelcastInstance mockHazelcast() {
		return Mockito.mock(HazelcastInstance.class);
	}
	
	public IdGenerator mockIdGen(HazelcastInstance instance, String key, long values) {
		IdGenerator idGen = Mockito.mock(IdGenerator.class);
		Mockito.when(idGen.newId()).thenReturn(values).thenReturn(-1L);
		Mockito.when(instance.getIdGenerator(key)).thenReturn(idGen);
		return idGen;
	}
	
	public IMap mockMap(HazelcastInstance instance, String key) {
		IMap map = Mockito.mock(IMap.class);
		Mockito.when(instance.getMap(key)).thenReturn(map);
		return map;
	}

	@SuppressWarnings("rawtypes")
	/**
	 * Method to invoke private method calls for testing. Example:
	 * 
	 * @code
	 * class Foo {
	 *    private int counter;
	 *    private void setCounter(int counter);
	 * }
	 * ...
	 * Foo f = new Foo();
	 * TestHelper.INSTANCE.insertSingleArgumentPrivateMethod(Foo.class, "setCounter", Integer.class, f, 12);
	 * @endcode
	 * @param methodClass the class holding the method you want to call (usually <Class>.class)
	 * @param methodName the name of the method you want to invoke 
	 * @param parameterType the type of parameter that you want to set, Integer.class, Foo.class f.e.
	 * @param instance the initiated class that you want to perform the action on.
	 * @param parameter the parameter to pass to the private method.
	 * @return Object with the return value, if any, from the method invoked.
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object invokeSingleArg(Class methodClass, String methodName, Class parameterType, 
			Object instance, Object parameter) throws Exception {
		@SuppressWarnings("unchecked")
		Method privateInvoke = null;
		privateInvoke = methodClass.getDeclaredMethod(methodName, parameterType);
		privateInvoke.setAccessible(true);
		return privateInvoke.invoke(instance, parameter);
	}

	@SuppressWarnings("rawtypes")
	/**
	 * Method to invoke private method calls for testing. Example:
	 * 
	 * @code
	 * class Foo {
	 *    private int counter;
	 *    private void setCounter(int counter);
	 *    private int getCounter();
	 * }
	 * ...
	 * Foo f = new Foo();
	 * TestHelper.INSTANCE.invokeMethod(Foo.class, "getCounter", f);
	 * @endcode
	 * @param methodClass the class holding the method you want to call (usually <Class>.class)
	 * @param methodName the name of the method you want to invoke 
	 * @param instance the initiated class that you want to perform the action on.
	 * @return Object with the return value, if any, from the method invoked.
	 */
	public Object invokeMethod(Class methodClass, String methodName,Object instance) throws Exception {
		@SuppressWarnings("unchecked")
		Method privateInvoke = null;
		privateInvoke = methodClass.getDeclaredMethod(methodName);
		privateInvoke.setAccessible(true);
		return privateInvoke.invoke(instance);
	}

}

