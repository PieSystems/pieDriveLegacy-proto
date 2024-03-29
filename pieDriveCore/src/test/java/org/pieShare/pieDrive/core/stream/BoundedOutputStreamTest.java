package org.pieShare.pieDrive.core.stream;

import org.pieShare.pieDrive.core.stream.util.LimitReachedException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import junit.framework.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class BoundedOutputStreamTest {
	private int bufferLength = 4096;
	private byte[] inputBuffer = new byte[bufferLength];
	
	byte inputValue = (byte)1;
	byte emptyValue = (byte)0;	

	@BeforeTest
	public void setUp() {
		initializeBuffer(inputBuffer, inputValue);
	}
	
	private void initializeBuffer(byte[] buffer, byte value) {
		for(int i = 0; i < buffer.length; i++) {
			buffer[i] = value;
		}
	}
	
	private void verifyOutputBuffer(byte[] outputBuffer, int limit) {
		if(limit > outputBuffer.length) {
			limit = outputBuffer.length;
		}
		
		if(limit > bufferLength) {
			limit = bufferLength;
		}
		
		for(int i = 0; i < limit; i++) {
			Assert.assertEquals(outputBuffer[i], inputValue);
		}
		
		for(int i = limit; i < outputBuffer.length; i++) {
			Assert.assertEquals(outputBuffer[i], emptyValue);
		}
	}
	
	@Test
	public void testWrite_ValidOutputStream_BufferLength() throws IOException {
		int limit = bufferLength;
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputBuffer.length);
		BoundedOutputStream boundedStream = new BoundedOutputStream(outputStream, limit);
		
		boundedStream.write(inputBuffer);
		byte[] outputBuffer = outputStream.toByteArray();
		
		verifyOutputBuffer(outputBuffer, limit);
	}
	
	@Test(expectedExceptions = LimitReachedException.class)
	public void testRead_ValidOutputStream_BufferLengthSmaller() throws IOException {
		int limit = bufferLength / 2;
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputBuffer.length);
		BoundedOutputStream boundedStream = new BoundedOutputStream(outputStream, limit);
		
		boundedStream.write(inputBuffer);
		
		Assert.fail("Expected exception");
	}
	
	@Test
	public void testRead_ValidOutputStream_BufferLengthLarger() throws IOException {
		int limit = bufferLength * 2;
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputBuffer.length);
		BoundedOutputStream boundedStream = new BoundedOutputStream(outputStream, limit);
		
		boundedStream.write(inputBuffer);
		byte[] outputBuffer = outputStream.toByteArray();
		
		verifyOutputBuffer(outputBuffer, limit);
	}
}
