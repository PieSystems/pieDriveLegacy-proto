package org.pieShare.pieDrive.core.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class BoundedInputStreamTest {
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
	public void testRead_ValidInputStream_BufferLength() throws IOException {
		int limit = bufferLength;
		byte[] outputBuffer = new byte[bufferLength + 1];
		initializeBuffer(outputBuffer, emptyValue);
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBuffer);
		BoundedInputStream limitingStream = new BoundedInputStream(inputStream, limit);
		
		limitingStream.read(outputBuffer);
		
		verifyOutputBuffer(outputBuffer, limit);
	}
	
	@Test
	public void testRead_ValidInputStream_BufferLengthSmaller() throws IOException {
		int limit = bufferLength / 2;
		byte[] outputBuffer = new byte[bufferLength + 1];
		initializeBuffer(outputBuffer, emptyValue);
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBuffer);
		BoundedInputStream limitingStream = new BoundedInputStream(inputStream, limit);
		
		limitingStream.read(outputBuffer);
		
		verifyOutputBuffer(outputBuffer, limit);
	}
	
	@Test
	public void testRead_ValidInputStream_BufferLengthLarger() throws IOException {
		int limit = bufferLength * 2;
		byte[] outputBuffer = new byte[bufferLength + 1];
		initializeBuffer(outputBuffer, emptyValue);
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBuffer);
		BoundedInputStream limitingStream = new BoundedInputStream(inputStream, limit);
		
		limitingStream.read(outputBuffer);
		
		verifyOutputBuffer(outputBuffer, limit);
	}
}
