/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
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
public class DownloadRaid5ChunkTaskNGTest extends FileHandlingTaskTestBase {
	
	public DownloadRaid5ChunkTaskNGTest() {
	}

	@BeforeMethod
	public void setUpMethod() throws Exception {
	}

	@AfterMethod
	public void tearDownMethod() throws Exception {
	}

	@Test
	public void testSimpleDownload() throws IOException, FileNotFoundException, NoSuchAlgorithmException {
		int length = 20;
		String uid = UUID.randomUUID().toString();
		File expectedFile = this.createFileHelper(this.uploadAdapter1, uid, length);
		byte[] hash = this.generateMd5(expectedFile);
		
		AdapterChunk chunk = new AdapterChunk();
		chunk.setAdapterId(this.adapterIds.get(0));
		chunk.setDataShard(0);
		chunk.setHash(hash);
		chunk.setSize(expectedFile.length());
		chunk.setUuid(uid);
		
		byte[] buffer = new byte[length];
		
		DownloadRaid5ChunkTask task = this.downloadRaid5ChunkTaskProvider.get();
		task.setChunk(chunk);
		task.setOut(StreamFactory.getOutputStream(buffer));
		task.compute();
		
		Assert.assertEquals(this.generateMd5(buffer), hash);
	}
	
}
