/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream;

import java.io.ByteArrayOutputStream;
import java.io.NotActiveException;
import java.math.BigInteger;
import java.security.MessageDigest;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.pieShare.pieDrive.core.stream.util.HashingDoneCallback;
import org.pieShare.pieDrive.core.stream.util.ICallbackId;
import org.pieShare.pieDrive.core.stream.util.HashingStreamCallbackHelper;
import org.pieShare.pieDrive.core.stream.helper.StringCallbackId;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class HashingOutputStreamTest {
	
	private final String text = "thisIsTextForTestingMyHashingInputStream";
	private final String md5 = "67a23926e2068bac731c5816e90b0cb1";
	private boolean hashOk;
	
	public HashingOutputStreamTest() {
	}
	
	@Before
	public void setUp() {
		hashOk = false;
	}
	
	@After
	public void tearDown() {
	}

	@Test
	public void testCloseAfterWorkDone() throws Exception {
		HashingStreamCallbackHelper helper = new HashingStreamCallbackHelper();
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
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream(text.getBytes().length);
		HashingOutputStream out = new HashingOutputStream(stream, MessageDigest.getInstance("MD5"), helper);
		out.write(this.text.getBytes());
		out.flush();
		out.close();
		
		Assert.assertArrayEquals(this.text.getBytes(), stream.toByteArray());
		Assert.assertTrue(hashOk);
	}
	
	@Test
	public void testCloseDueToError() throws Exception {
		HashingStreamCallbackHelper helper = new HashingStreamCallbackHelper();
		helper.setCallbackId((new StringCallbackId()).setChunk("test"));
		helper.setCallback(new HashingDoneCallback() {
			@Override
			public void hashingDone(ICallbackId id, byte[] hash) {
				StringCallbackId sId = (StringCallbackId)id;
				Assert.assertEquals("test", sId.getChunk());			
				String result = (new BigInteger(1, hash)).toString(16);
				Assert.assertThat(md5, is(not(result)));
				hashOk = true;
			}
		});
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream(text.getBytes().length);
		HashingOutputStream out = new HashingOutputStream(stream, MessageDigest.getInstance("MD5"), helper);
		byte[] bs = this.text.getBytes();
		out.write(bs[0]);
		//simulating error
		out.flush();
		out.close();
		
		Assert.assertTrue(hashOk);
	}
	
}
