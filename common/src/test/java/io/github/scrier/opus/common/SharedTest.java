package io.github.scrier.opus.common;

import static org.junit.Assert.*;
import io.github.scrier.opus.common.Shared.Hazelcast;

import org.junit.Test;

public class SharedTest {

	@Test
	public void testConstructor() {
		Hazelcast testObject = new Shared.Hazelcast();
		assertEquals(Shared.Hazelcast.COMMON_NODE_ID, testObject.COMMON_NODE_ID);
		assertEquals(Shared.Hazelcast.BASE_NUKE_MAP, testObject.BASE_NUKE_MAP);
		assertEquals(Shared.Hazelcast.SETTINGS_MAP, testObject.SETTINGS_MAP);
	}

}
