package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.task.config.FakeAdapterCoreTestConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

@DirtiesContext
@ContextConfiguration(classes = FakeAdapterCoreTestConfig.class)
public class Raid5FileTaskTest extends FileHandlingTaskTestBase {
	private void assertHashCodes(PieRaidFile pieRaidFile) throws Exception {
		for(PhysicalChunk physicalChunk : pieRaidFile.getChunks()) {
			ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(physicalChunk.getChunks());
			AdapterChunk chunk1 = getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0));
			AdapterChunk chunk2 = getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1));
			AdapterChunk chunk3 = getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2));
			Assert.assertEquals(chunk1.getHash(), generateMd5(new File(uploadAdapter1, chunk1.getUuid())));
			Assert.assertEquals(chunk2.getHash(), generateMd5(new File(uploadAdapter2, chunk2.getUuid())));
			Assert.assertEquals(chunk3.getHash(), generateMd5(new File(uploadAdapter3, chunk3.getUuid())));
		}
	}
	
	@Test
	public void testUpAndDownloadFileRaid5() throws Exception {
		String fileName = "testUpAndDownloadFileRaid5";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 15);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaid5FileTask uploadTask = this.uploadRaid5FileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();
		
		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 1);
		Assert.assertEquals(uploadedFilesAdapter2.length, 1);
		Assert.assertEquals(uploadedFilesAdapter3.length, 1);
		
		assertHashCodes(expectedRaidFile);
		
		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaid5FileTask downloadTask = downloadRaid5FileProvider.get();
		downloadTask.setOutputDir(out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(generateMd5(expected), generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}
	
	@Test
	public void testUpAndDownloadFileRaid5RecoverableCorruption() throws Exception {
		String fileName = "testUpAndDownloadFileRaid5RecoverableCorruption";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 15);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaid5FileTask uploadTask = this.uploadRaid5FileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();
		
		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 1);
		Assert.assertEquals(uploadedFilesAdapter2.length, 1);
		Assert.assertEquals(uploadedFilesAdapter3.length, 1);
		
		assertHashCodes(expectedRaidFile);
		
		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		corruptFile(uploadedFilesAdapter2[0]);
		
		DownloadRaid5FileTask downloadTask = downloadRaid5FileProvider.get();
		downloadTask.setOutputDir(out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(generateMd5(expected), generateMd5(downloadedFiles[0]));
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks());
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Healthy);
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(generateMd5(expected), generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}
	
	@Test
	public void testUpAndDownloadFileRaid5IrrecoverableCorruption() throws Exception {
		String fileName = "testUpAndDownloadFileRaid5IrrecoverableCorruption";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 15);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaid5FileTask uploadTask = this.uploadRaid5FileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();
		
		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 1);
		Assert.assertEquals(uploadedFilesAdapter2.length, 1);
		Assert.assertEquals(uploadedFilesAdapter3.length, 1);
		
		assertHashCodes(expectedRaidFile);
		
		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		corruptFile(uploadedFilesAdapter2[0]);
		corruptFile(uploadedFilesAdapter3[0]);
		
		DownloadRaid5FileTask downloadTask = downloadRaid5FileProvider.get();
		downloadTask.setOutputDir(out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(generateMd5(expected), generateMd5(downloadedFiles[0]));
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks());
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Broken);
		
		//check that the program did not try to recover the broken file and created some sort of false positive
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(generateMd5(expected), generateMd5(downloadedFiles[0]));
		adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks());
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Broken);
	}
	
	@Test
	public void testUpAndDownLoadFileRaid5WithMultiChunks() throws Exception {
		String fileName = "testUpAndDownLoadFileRaid5WithMultiChunks";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 96);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaid5FileTask uploadTask = this.uploadRaid5FileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();
		
		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 5);
		Assert.assertEquals(uploadedFilesAdapter2.length, 5);
		Assert.assertEquals(uploadedFilesAdapter3.length, 5);
		
		assertHashCodes(expectedRaidFile);
		
		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		DownloadRaid5FileTask downloadTask = downloadRaid5FileProvider.get();
		downloadTask.setOutputDir(out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(generateMd5(expected), generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}
	
	@Test
	public void testUpAndDownLoadFileRaid5WithMultiChunksRecoverableCorruption() throws Exception {
		String fileName = "testUpAndDownLoadFileRaid5WithMultiChunks";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 96);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaid5FileTask uploadTask = this.uploadRaid5FileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();
		
		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 5);
		Assert.assertEquals(uploadedFilesAdapter2.length, 5);
		Assert.assertEquals(uploadedFilesAdapter3.length, 5);
		
		assertHashCodes(expectedRaidFile);
		
		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		for (File file : uploadedFilesAdapter1) {
			corruptFile(file);
		}
		
		DownloadRaid5FileTask downloadTask = downloadRaid5FileProvider.get();
		downloadTask.setOutputDir(out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(generateMd5(expected), generateMd5(downloadedFiles[0]));
		for(PhysicalChunk physicalChunk : raidFile.getChunks()) {
			List<AdapterChunk> adapterChunks = physicalChunk.getChunks();
			
			Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Healthy);
			Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.Healthy);
			Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Healthy);
		}
		
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertEquals(generateMd5(expected), generateMd5(downloadedFiles[0]));
		this.assertRaidFile(raidFile, ChunkHealthState.Healthy);
	}
	
	@Test
	public void testUpAndDownLoadFileRaid5WithMultiChunksIrrecoverableCorruption() throws Exception {
		String fileName = "testUpAndDownLoadFileRaid5WithMultiChunks";
		String fileNameExpected = fileName + "Expected";
		File source = this.createFileHelper(this.in, fileName, 96);
		File expected = new File(this.in, fileNameExpected);
		FileUtils.copyFile(source, expected);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(source);
		UploadRaid5FileTask uploadTask = this.uploadRaid5FileProvider.get();
		uploadTask.setFile(source);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();
		
		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 5);
		Assert.assertEquals(uploadedFilesAdapter2.length, 5);
		Assert.assertEquals(uploadedFilesAdapter3.length, 5);
		
		assertHashCodes(expectedRaidFile);
		
		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		
		for (File file : uploadedFilesAdapter1) {
			corruptFile(file);
		}
		for (File file : uploadedFilesAdapter3) {
			corruptFile(file);
		}
		
		DownloadRaid5FileTask downloadTask = downloadRaid5FileProvider.get();
		downloadTask.setOutputDir(out);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		File[] downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(generateMd5(expected), generateMd5(downloadedFiles[0]));
		for(PhysicalChunk physicalChunk : raidFile.getChunks()) {
			List<AdapterChunk> adapterChunks = physicalChunk.getChunks();
			
			Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Broken);
			Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.Healthy);
			Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Broken);
		}
		
		//check that the program did not try to recover the broken file and created some sort of false positive
		raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		this.assertRaidFile(raidFile, ChunkHealthState.NotChecked);
		downloadTask.setRaidFile(raidFile);
		downloadTask.compute();
		
		downloadedFiles = this.out.listFiles();
		Assert.assertEquals(1, downloadedFiles.length);
		Assert.assertNotEquals(generateMd5(expected), generateMd5(downloadedFiles[0]));
		for(PhysicalChunk physicalChunk : raidFile.getChunks()) {
			List<AdapterChunk> adapterChunks = physicalChunk.getChunks();
			
			Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Broken);
			Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.Healthy);
			Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Broken);
		}
	}
}
