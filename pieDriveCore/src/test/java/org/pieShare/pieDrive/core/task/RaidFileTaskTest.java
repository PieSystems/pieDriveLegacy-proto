/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.util.ArrayList;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.task.config.FakeAdapterCoreTestConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
@DirtiesContext
@ContextConfiguration(classes = FakeAdapterCoreTestConfig.class)
public class RaidFileTaskTest extends FileHandlingTaskTestBase {
	
	private void assertRaidFile(PieRaidFile file, ChunkHealthState state) {
		for(PhysicalChunk chunk: file.getChunks()) {
			this.assertAllState(chunk, state);
		}
        }
	
	private void assertAllState(PhysicalChunk chunk, ChunkHealthState state) {
		for(AdapterChunk aChunk: chunk.getChunks().values()) {
			Assert.assertEquals(aChunk.getState(), state);
		}
	}
	
	@Test
	public void testUpAndDownLoadFileRaid1() throws Exception {
		String fileName = "testOneChunkFile";
		File expected = this.createFileHelper(this.in, fileName, 15);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(expected);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.setRaidedFile(expectedRaidFile);
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

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}

	@Test
	public void testUpAndDownLoadFileRaid1OneCorruptChunkOnServer() throws Exception {
		String fileName = "testOneChunkFileOneCorruptChunkOnServer";
		File expected = this.createFileHelper(this.in, fileName, 15);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(expected);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.setRaidedFile(expectedRaidFile);
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

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks().values());
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Healthy);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();
		
		Thread.sleep(2000);
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}

	@Test
	public void testUpAndDownLoadFileRaid1TwoCorruptChunksOnServer() throws Exception {
		String fileName = "testOneChunkFileTwoCorruptChunksOnServer";
		File expected = this.createFileHelper(this.in, fileName, 15);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(expected);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.setRaidedFile(expectedRaidFile);
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

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks().values());
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Healthy);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();
		
		Thread.sleep(2000);
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}

	@Test
	public void testUpAndDownLoadFileRaid1ThreeCorruptChunksOnServer() throws Exception {
		String fileName = "testOneChunkFileThreeCorruptChunksOnServer";
		File expected = this.createFileHelper(this.in, fileName, 15);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(expected);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.setRaidedFile(expectedRaidFile);
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

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(2000);

		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		
		this.assertRaidFile(raidFile, ChunkHealthState.Broken);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();
		
		Thread.sleep(2000);
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Broken);
	}

	@Test
	public void testUpAndDownLoadFileRaid1WithMultiChunks() throws Exception {
		String fileName = "testMultiChunkFile";
		File expected = this.createFileHelper(this.in, fileName, 96);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(expected);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.run();

		Thread.sleep(2000);

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
		downloadTask.run();

		Thread.sleep(4000);

		byte[] expectedBytes = this.generateMd5(expected);
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}

	@Test
	public void testUpAndDownLoadFileRaid1WithMultiChunksRecoverableCorruption() throws Exception {
		String fileName = "testMultiChunkFileRecoverableCorruption";
		File expected = this.createFileHelper(this.in, fileName, 96);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(expected);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.setRaidedFile(expectedRaidFile);
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

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaidFileTask downloadTask = this.downloadRaidFileProvider.get();
		downloadTask.setOutputDir(this.out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();

		Thread.sleep(4000);

		byte[] expectedBytes = this.generateMd5(expected);
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks().values());
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Healthy);
		adapterChunks = new ArrayList<>(raidFile.getChunks().get(1).getChunks().values());
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Healthy);
		adapterChunks = new ArrayList<>(raidFile.getChunks().get(2).getChunks().values());
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Healthy);
		adapterChunks = new ArrayList<>(raidFile.getChunks().get(3).getChunks().values());
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Healthy);
		adapterChunks = new ArrayList<>(raidFile.getChunks().get(4).getChunks().values());
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Healthy);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();
		
		Thread.sleep(2000);
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}

	@Test
	public void testUpAndDownLoadFileRaid1WithMultiChunksIrrecoverableCorruption() throws Exception {
		String fileName = "testMultiChunkFileIrrecoverableCorruption";
		File expected = this.createFileHelper(this.in, fileName, 96);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(expected);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.setRaidedFile(expectedRaidFile);
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
		downloadTask.run();
		
		byte[] expectedBytes = this.generateMd5(expected);
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));

		Thread.sleep(2000);

		this.assertRaidFile(raidFile, ChunkHealthState.Broken);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.run();
		
		Thread.sleep(2000);
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(expectedBytes, this.generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Broken);
	}
}
