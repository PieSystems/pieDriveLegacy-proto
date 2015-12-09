/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.task.config.CoreTestConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
@DirtiesContext
@ContextConfiguration(classes = CoreTestConfig.class)
public class RaidFileTaskTest extends FileHandlingTaskTestBase {
	@Test
	public void testUpAndDownLoadFileRaid1() throws Exception {
		String fileName = "testOneChunkFile";
		File expected = this.createFileHelper(this.in, fileName, 15);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.run();

		Thread.sleep(2000);

		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 1);
		Assert.assertEquals(uploadedFilesAdapter2.length, 1);
		Assert.assertEquals(uploadedFilesAdapter3.length, 1);
		byte[] expectedBytes = this.generateMd5(expected);
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter1[0]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter2[0]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter3[0]));

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		Assert.assertEquals(1, this.counter.getCount());
	}

	@Test
	public void testUpAndDownLoadFileRaid1OneCorruptChunkOnServer() throws Exception {
		String fileName = "testOneChunkFileOneCorruptChunkOnServer";
		File expected = this.createFileHelper(this.in, fileName, 15);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.run();

		Thread.sleep(2000);

		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 1);
		Assert.assertEquals(uploadedFilesAdapter2.length, 1);
		Assert.assertEquals(uploadedFilesAdapter3.length, 1);
		byte[] expectedBytes = this.generateMd5(expected);
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter1[0]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter2[0]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter3[0]));

		corruptFile(uploadedFilesAdapter1[0]);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter1[0]));

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		Assert.assertEquals(2, this.counter.getCount());
	}

	@Test
	public void testUpAndDownLoadFileRaid1TwoCorruptChunksOnServer() throws Exception {
		String fileName = "testOneChunkFileTwoCorruptChunksOnServer";
		File expected = this.createFileHelper(this.in, fileName, 15);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.run();

		Thread.sleep(2000);

		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 1);
		Assert.assertEquals(uploadedFilesAdapter2.length, 1);
		Assert.assertEquals(uploadedFilesAdapter3.length, 1);
		byte[] expectedBytes = this.generateMd5(expected);
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter1[0]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter2[0]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter3[0]));

		corruptFile(uploadedFilesAdapter1[0]);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter1[0]));
		corruptFile(uploadedFilesAdapter2[0]);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter2[0]));

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		Assert.assertEquals(3, this.counter.getCount());
	}

	//@Test
	public void testUpAndDownLoadFileRaid1ThreeCorruptChunksOnServer() throws Exception {
		String fileName = "testOneChunkFileThreeCorruptChunksOnServer";
		File expected = this.createFileHelper(this.in, fileName, 15);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.run();

		Thread.sleep(2000);

		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 1);
		Assert.assertEquals(uploadedFilesAdapter2.length, 1);
		Assert.assertEquals(uploadedFilesAdapter3.length, 1);
		byte[] expectedBytes = this.generateMd5(expected);
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter1[0]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter2[0]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter3[0]));

		corruptFile(uploadedFilesAdapter1[0]);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter1[0]));
		corruptFile(uploadedFilesAdapter2[0]);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter2[0]));
		corruptFile(uploadedFilesAdapter3[0]);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(uploadedFilesAdapter3[0]));

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		Assert.fail("Download task should fail");
	}

	@Test
	public void testUpAndDownLoadFileRaid1WithMultiChunks() throws Exception {
		String fileName = "testMultiChunkFile";
		File expected = this.createFileHelper(this.in, fileName, 96);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.run();

		Thread.sleep(2000);

		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 5);
		Assert.assertEquals(uploadedFilesAdapter2.length, 5);
		Assert.assertEquals(uploadedFilesAdapter3.length, 5);

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		byte[] expectedBytes = this.generateMd5(expected);
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		Assert.assertEquals(5, this.counter.getCount());
	}

	@Test
	public void testUpAndDownLoadFileRaid1WithMultiChunksRecoverableCorruption() throws Exception {
		String fileName = "testMultiChunkFileRecoverableCorruption";
		File expected = this.createFileHelper(this.in, fileName, 96);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.run();

		Thread.sleep(2000);

		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 5);
		Assert.assertEquals(uploadedFilesAdapter2.length, 5);
		Assert.assertEquals(uploadedFilesAdapter3.length, 5);

		for (File file : uploadedFilesAdapter1) {
			corruptFile(file);
		}

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		byte[] expectedBytes = this.generateMd5(expected);
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		Assert.assertEquals(7, this.counter.getCount());
	}

	//@Test
	public void testUpAndDownLoadFileRaid1WithMultiChunksIrrecoverableCorruption() throws Exception {
		String fileName = "testMultiChunkFileIrrecoverableCorruption";
		File expected = this.createFileHelper(this.in, fileName, 96);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.run();

		Thread.sleep(2000);

		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 5);
		Assert.assertEquals(uploadedFilesAdapter2.length, 5);
		Assert.assertEquals(uploadedFilesAdapter3.length, 5);

		corruptFile(uploadedFilesAdapter1[3]);
		corruptFile(uploadedFilesAdapter2[3]);
		corruptFile(uploadedFilesAdapter3[3]);

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		Assert.fail("Download task should fail");
	}
}
