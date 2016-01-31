/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.task.config.FakeAdapterCoreTestConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
@DirtiesContext
@ContextConfiguration(classes = FakeAdapterCoreTestConfig.class)
public class RaidFileTaskTest extends FileHandlingTaskTestBase {
	@Test
	public void testUpAndDownLoadFileRaid1() throws Exception {
		String fileName = "testOneChunkFile";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 15);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();

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

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}

	@Test
	public void testUpAndDownLoadFileRaid1OneCorruptChunkOnServer() throws Exception {
		String fileName = "testOneChunkFileOneCorruptChunkOnServer";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 15);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();

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

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks());
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}

	@Test
	public void testUpAndDownLoadFileRaid1TwoCorruptChunksOnServer() throws Exception {
		String fileName = "testOneChunkFileTwoCorruptChunksOnServer";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 15);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();

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

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks());
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}

	@Test
	public void testUpAndDownLoadFileRaid1ThreeCorruptChunksOnServer() throws Exception {
		String fileName = "testOneChunkFileThreeCorruptChunksOnServer";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 15);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();

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

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		
		this.assertRaidFile(raidFile, ChunkHealthState.Broken);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Broken);
	}

	@Test
	public void testUpAndDownLoadFileRaid1WithMultiChunks() throws Exception {
		String fileName = "testMultiChunkFile";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 96);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();

		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 5);
		Assert.assertEquals(uploadedFilesAdapter2.length, 5);
		Assert.assertEquals(uploadedFilesAdapter3.length, 5);

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();

		byte[] expectedBytes = this.generateMd5(expected);
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}

	@Test
	public void testUpAndDownLoadFileRaid1WithMultiChunksRecoverableCorruption() throws Exception {
		String fileName = "testMultiChunkFileRecoverableCorruption";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 96);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();

		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 5);
		Assert.assertEquals(uploadedFilesAdapter2.length, 5);
		Assert.assertEquals(uploadedFilesAdapter3.length, 5);

		for (File file : uploadedFilesAdapter1) {
			corruptFile(file);
		}

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();

		byte[] expectedBytes = this.generateMd5(expected);
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks());
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}

	@Test
	public void testUpAndDownLoadFileRaid1WithMultiChunksIrrecoverableCorruption() throws Exception {
		String fileName = "testMultiChunkFileIrrecoverableCorruption";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 96);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();

		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 5);
		Assert.assertEquals(uploadedFilesAdapter2.length, 5);
		Assert.assertEquals(uploadedFilesAdapter3.length, 5);

		for (File file : uploadedFilesAdapter1) {
			corruptFile(file);
		}
		for (File file : uploadedFilesAdapter2) {
			corruptFile(file);
		}
		for (File file : uploadedFilesAdapter3) {
			corruptFile(file);
		}

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		byte[] expectedBytes = this.generateMd5(expected);
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));

		this.assertRaidFile(raidFile, ChunkHealthState.Broken);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Broken);
	}
	
	@Test
	public void testUpAndDelete() throws Exception {
		String fileName = "testUpAndDelete";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 15);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();

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

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DeleteRaidFileTask deleteTask = this.deleteRaidFileTaskProvider.get();
		deleteTask.setPieRaidFile(raidFile);
		deleteTask.run();

		assertFalse(this.db.findAllPieRaidFiles().contains(raidFile));
		Assert.assertNull(this.db.findPieRaidFileById(expectedRaidFile.getUid()));
	}
}
