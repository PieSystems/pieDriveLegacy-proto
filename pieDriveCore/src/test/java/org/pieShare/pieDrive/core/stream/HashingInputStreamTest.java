/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pieShare.pieDrive.core.stream.helper.StringCallbackId;
import org.pieShare.pieDrive.core.stream.util.HashingDoneCallback;
import org.pieShare.pieDrive.core.stream.util.ICallbackId;
import org.pieShare.pieDrive.core.stream.util.StreamCallbackHelper;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class HashingInputStreamTest {
	
	private final String text = "thisIsTextForTestingMyHashingInputStream";
	private final String md5 = "67a23926e2068bac731c5816e90b0cb1";
	private ByteArrayInputStream inputStream;
	private boolean hashOk;
	
	public HashingInputStreamTest() {
	}
	
	@Before
	public void setUp() throws UnsupportedEncodingException {
		byte[] bs = this.text.getBytes();
		inputStream = new ByteArrayInputStream(bs);
		hashOk = false;
	}
	
	@After
	public void tearDown() {
	}

	@Test
	public void testReadAllBytesIntoBufferAndSomeMore() throws Exception {
		StreamCallbackHelper helper = new StreamCallbackHelper();
		helper.setCallbackId((new StringCallbackId()).setChunk("test"));
		helper.setCallback(new HashingDoneCallback() {
			@Override
			public void hashingDone(ICallbackId id, byte[] hash) {
				StringCallbackId sId = (StringCallbackId)id;
				Assert.assertEquals("test", sId.getChunk());				
				String result = (new BigInteger(1, hash)).toString(16);
				Assert.assertEquals(md5, result);
				hashOk = true;
			}
		});
		
		HashingInputStream instance = new HashingInputStream(inputStream, MessageDigest.getInstance("MD5"), helper);
		
		int length = this.text.getBytes().length;
		byte[] bs = new byte[length];
		instance.read(bs, 0, length);
		byte[] next = new byte[5];
		instance.read(next, 0, next.length);
		
		Assert.assertEquals(this.text, new String(bs));
		Assert.assertTrue(hashOk);
		
		byte[] expectedNext = new byte[]{0,0,0,0,0};
		Assert.assertArrayEquals(expectedNext, next);
	}
	
	@Test
	public void testReadMoreBytesThenInInput() throws Exception {
		StreamCallbackHelper helper = new StreamCallbackHelper();
		helper.setCallbackId((new StringCallbackId()).setChunk("test"));
		helper.setCallback(new HashingDoneCallback() {
			@Override
			public void hashingDone(ICallbackId id, byte[] hash) {
				StringCallbackId sId = (StringCallbackId)id;
				Assert.assertEquals("test", sId.getChunk());			
				String result = (new BigInteger(1, hash)).toString(16);
				Assert.assertEquals(md5, result);
				hashOk = true;
			}
		});
		
		HashingInputStream instance = new HashingInputStream(inputStream, MessageDigest.getInstance("MD5"), helper);
		
		int length = this.text.getBytes().length;
		byte[] bs = new byte[length+5];
		int read = instance.read(bs, 0, length+5);
		
		Assert.assertEquals(length, read);
		Assert.assertTrue(new String(bs).startsWith(this.text));
		Assert.assertTrue(hashOk);
	}

	@Test
	public void testReadAllBytesOneByOne() throws Exception {
		StreamCallbackHelper helper = new StreamCallbackHelper();
		helper.setCallbackId((new StringCallbackId()).setChunk("test"));
		helper.setCallback(new HashingDoneCallback() {
			@Override
			public void hashingDone(ICallbackId id, byte[] hash) {
				StringCallbackId sId = (StringCallbackId)id;
				Assert.assertEquals("test", sId.getChunk());			
				String result = (new BigInteger(1, hash)).toString(16);
				Assert.assertEquals(md5, result);
				hashOk = true;
			}
		});
		
		HashingInputStream instance = new HashingInputStream(inputStream, MessageDigest.getInstance("MD5"), helper);
		
		byte[] bs = new byte[this.text.getBytes().length];
		int i = 0;
		int b = -1;
		
		while((b = instance.read()) != -1 && i < bs.length) {
			bs[i] = (byte)b;
			i++;
		}
		
 		Assert.assertEquals(this.text, new String(bs));
		Assert.assertTrue(hashOk);
	}
	
	
	@Test
	public void testNoCallbackOnError() throws Exception {
		StreamCallbackHelper helper = new StreamCallbackHelper();
		helper.setCallbackId((new StringCallbackId()).setChunk("test"));
		helper.setCallback(new HashingDoneCallback() {
			@Override
			public void hashingDone(ICallbackId id, byte[] hash) {
				hashOk = true;
			}
		});
		
		HashingInputStream instance = new HashingInputStream(inputStream, MessageDigest.getInstance("MD5"), helper);
		
		byte[] bs = new byte[this.text.getBytes().length];
		
		bs[0] = (byte)instance.read();
		//simulated error
		instance.close();
		
		Assert.assertFalse(hashOk);
	}
}
