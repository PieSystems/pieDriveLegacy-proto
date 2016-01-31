package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.task.config.FakeAdapterCoreTestConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

@DirtiesContext
@ContextConfiguration(classes = FakeAdapterCoreTestConfig.class)
public class IntegrityCheckTaskTest extends FileHandlingTaskTestBase {
	@Test
	public void testFileIntegrityOneChunkAllValid() throws Exception {
		String fileName = "testFileIntegrityOneChunkAllValid";
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
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		Assert.assertEquals(raidFile.getChunks().size(), 1);
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks());
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.NotChecked);
		
		IntegrityCheckTask checkTask = this.integrityCheckTaskProvider.get();
		checkTask.setPhysicalChunk(raidFile.getChunks().get(0));
		checkTask.setFile(new RandomAccessFile(expected, "r"));
		
		checkTask.compute();
		
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Healthy);
	}
	
	//@Test
	//TODO reactivate and figure out why it doesn't work
	public void testFileIntegrityOneChunkAllInvalid() throws Exception {
		String fileName = "testFileIntegrityOneChunkAllInvalid";
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
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);
		
		corruptFile(uploadedFilesAdapter1[0]);
		corruptFile(uploadedFilesAdapter2[0]);
		corruptFile(uploadedFilesAdapter3[0]);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		Assert.assertEquals(raidFile.getChunks().size(), 1);
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks());
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.NotChecked);
		
		IntegrityCheckTask checkTask = this.integrityCheckTaskProvider.get();
		checkTask.setPhysicalChunk(raidFile.getChunks().get(0));
		checkTask.setFile(new RandomAccessFile(expected, "r"));
		
		checkTask.compute();
		
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Broken);
	}
	
	@Test
	public void testFileIntegrityOneChunkShouldIgnoreChunksThatAreNotStateNotChecked() throws Exception {
		String fileName = "testFileIntegrityOneChunkShouldIgnoreChunksThatAreNotStateNotChecked";
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
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);
		
		corruptFile(uploadedFilesAdapter1[0]);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		Assert.assertEquals(raidFile.getChunks().size(), 1);
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks());
		getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).setState(ChunkHealthState.Healthy);
		getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).setState(ChunkHealthState.Broken);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Broken);
		
		IntegrityCheckTask checkTask = this.integrityCheckTaskProvider.get();
		checkTask.setPhysicalChunk(raidFile.getChunks().get(0));
		checkTask.setFile(new RandomAccessFile(expected, "r"));
		
		checkTask.compute();
		
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Healthy);
	}
	
	@Test
	public void testFileIntegrityOneChunkShouldRecoverBrokenChunks() throws Exception {
		String fileName = "testFileIntegrityOneChunkShouldRecoverBrokenChunks";
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
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);
		
		corruptFile(uploadedFilesAdapter1[0]);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		corruptFile(uploadedFilesAdapter2[0]);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);

		PieRaidFile raidFile = this.db.findPieRaidFileById(expectedRaidFile.getUid());
		Assert.assertEquals(raidFile.getChunks().size(), 1);
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks());
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.NotChecked);
		
		IntegrityCheckTask checkTask = this.integrityCheckTaskProvider.get();
		checkTask.setPhysicalChunk(raidFile.getChunks().get(0));
		checkTask.setFile(new RandomAccessFile(expected, "r"));
		
		checkTask.compute();
		
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(0)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(1)).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(getAdapterChunkForAdapterId(adapterChunks, adapterIds.get(2)).getState(), ChunkHealthState.Healthy);
		
		uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 1);
		Assert.assertEquals(uploadedFilesAdapter2.length, 1);
		Assert.assertEquals(uploadedFilesAdapter3.length, 1);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);
	}
}
