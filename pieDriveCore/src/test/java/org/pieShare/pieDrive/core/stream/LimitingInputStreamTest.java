package org.pieShare.pieDrive.core.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import junit.framework.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pieShare.pieDrive.core.springConfig.CoreAppConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreAppConfig.class)
public class LimitingInputStreamTest {
	private int bufferLength = 10;
	private byte[] inputBuffer = new byte[bufferLength];
	
	byte inputValue = (byte)1;
	byte emptyValue = (byte)0;	

	@Before
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
		LimitingInputStream limitingStream = new LimitingInputStream(inputStream, limit);
		
		limitingStream.read(outputBuffer);
		
		verifyOutputBuffer(outputBuffer, limit);
	}
	
	@Test
	public void testRead_ValidInputStream_BufferLengthSmaller() throws IOException {
		int limit = bufferLength / 2;
		byte[] outputBuffer = new byte[bufferLength + 1];
		initializeBuffer(outputBuffer, emptyValue);
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBuffer);
		LimitingInputStream limitingStream = new LimitingInputStream(inputStream, limit);
		
		limitingStream.read(outputBuffer);
		
		verifyOutputBuffer(outputBuffer, limit);
	}
	
	@Test
	public void testRead_ValidInputStream_BufferLengthLarger() throws IOException {
		int limit = bufferLength * 2;
		byte[] outputBuffer = new byte[bufferLength + 1];
		initializeBuffer(outputBuffer, emptyValue);
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBuffer);
		LimitingInputStream limitingStream = new LimitingInputStream(inputStream, limit);
		
		limitingStream.read(outputBuffer);
		
		verifyOutputBuffer(outputBuffer, limit);
	}
}
