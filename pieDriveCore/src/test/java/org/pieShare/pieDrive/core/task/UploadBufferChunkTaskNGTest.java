/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.task.config.FakeAdapterCoreTestConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
@DirtiesContext
@ContextConfiguration(classes = FakeAdapterCoreTestConfig.class)
public class UploadBufferChunkTaskNGTest extends FileHandlingTaskTestBase {
	
	public UploadBufferChunkTaskNGTest() {
	}

	@BeforeMethod
	public void setUpMethod() throws Exception {
	}

	@AfterMethod
	public void tearDownMethod() throws Exception {
	}

	@Test
	public void testSimpleBufferUpload() throws NoSuchAlgorithmException, IOException {
		AdapterId adapterId = this.adapterIds.get(0);
		String uid = UUID.randomUUID().toString();
		
		byte[] bufferExpected = new byte[20];
		new Random().nextBytes(bufferExpected);
		MessageDigest digest = MessageDigest.getInstance("MD5");
		byte[] hashExpected = digest.digest(bufferExpected);
		
		AdapterChunk adapterChunk = new AdapterChunk();
		adapterChunk.setAdapterId(adapterId);
		adapterChunk.setSize(bufferExpected.length);
		adapterChunk.setUuid(uid);
		
		UploadBufferChunkTask task = this.uploadBuffereChunkTaskProvider.get();
		task.setBufer(bufferExpected);
		task.setChunk(adapterChunk);
		
		task.compute();
		
		Assert.assertEquals(1, this.uploadAdapter1.listFiles().length);
		Assert.assertEquals(adapterChunk.getHash(), hashExpected);
		Assert.assertEquals(this.generateMd5(new File(this.uploadAdapter1, uid)), hashExpected);
	}
	
}
