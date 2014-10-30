package io.github.scrier.puqe.common.exception;

import org.junit.Test;

public class InvalidOperationExceptionTest {

	@Test(expected=InvalidOperationException.class)
	public void testException() throws InvalidOperationException {
		throw new InvalidOperationException();
	}
	
	@Test(expected=InvalidOperationException.class)
	public void testExceptionMessage() throws InvalidOperationException {
		throw new InvalidOperationException("this is the message");
	}

}
