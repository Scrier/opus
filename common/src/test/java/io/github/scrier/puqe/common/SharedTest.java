package io.github.scrier.opus.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class SharedTest {

	@Test
	public void testConstructor() {
		Shared testObject = new Shared();
		assertEquals(Shared.COMMON_NODE_ID, testObject.COMMON_NODE_ID);
		assertEquals(Shared.NUKE_INFO_MAP, testObject.NUKE_INFO_MAP);
		assertEquals(Shared.SETTINGS_MAP, testObject.SETTINGS_MAP);
	}

}
