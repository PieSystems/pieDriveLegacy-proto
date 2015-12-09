package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.task.config.CoreTestConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

@DirtiesContext
@ContextConfiguration(classes = CoreTestConfig.class)
public class IntegrityCheckTaskTest extends FileHandlingTaskTestBase {
	@Test
	public void testFileIntegrityOneChunkAllValid() throws Exception {
		String fileName = "testFileIntegrityOneChunkAllValid";
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
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		Assert.assertEquals(raidFile.getChunks().size(), 1);
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks().values());
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.NotChecked);
		
		IntegrityCheckTask checkTask = this.integrityCheckTaskProvider.get();
		checkTask.setPhysicalChunk(raidFile.getChunks().get(0));
		checkTask.run();
		
		Thread.sleep(2000);
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Healthy);
	}
	
	@Test
	public void testFileIntegrityOneChunkAllInvalid() throws Exception {
		String fileName = "testFileIntegrityOneChunkAllInvalid";
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
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);
		
		corruptFile(uploadedFilesAdapter1[0]);
		corruptFile(uploadedFilesAdapter2[0]);
		corruptFile(uploadedFilesAdapter3[0]);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		Assert.assertEquals(raidFile.getChunks().size(), 1);
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks().values());
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.NotChecked);
		
		IntegrityCheckTask checkTask = this.integrityCheckTaskProvider.get();
		checkTask.setPhysicalChunk(raidFile.getChunks().get(0));
		checkTask.run();
		
		Thread.sleep(2000);
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Broken);
	}
	
	@Test
	public void testFileIntegrityOneChunkShouldIgnoreChunksThatAreNotStateNotChecked() throws Exception {
		String fileName = "testFileIntegrityOneChunkShouldIgnoreChunksThatAreNotStateNotChecked";
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
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);
		
		corruptFile(uploadedFilesAdapter1[0]);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		Assert.assertEquals(raidFile.getChunks().size(), 1);
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks().values());
		adapterChunks.get(0).setState(ChunkHealthState.Healthy);
		adapterChunks.get(2).setState(ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Broken);
		
		IntegrityCheckTask checkTask = this.integrityCheckTaskProvider.get();
		checkTask.setPhysicalChunk(raidFile.getChunks().get(0));
		checkTask.run();
		
		Thread.sleep(2000);
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Healthy);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Broken);
	}
	
	@Test
	public void testFileIntegrityOneChunkShouldRecoverBrokenChunks() throws Exception {
		String fileName = "testFileIntegrityOneChunkShouldRecoverBrokenChunks";
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
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);
		
		corruptFile(uploadedFilesAdapter1[0]);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		corruptFile(uploadedFilesAdapter2[0]);
		Assert.assertNotEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		Assert.assertEquals(raidFile.getChunks().size(), 1);
		ArrayList<AdapterChunk> adapterChunks = new ArrayList<>(raidFile.getChunks().get(0).getChunks().values());
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.NotChecked);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.NotChecked);
		
		IntegrityCheckTask checkTask = this.integrityCheckTaskProvider.get();
		checkTask.setPhysicalChunk(raidFile.getChunks().get(0));
		checkTask.run();
		
		Thread.sleep(10000);
		
		Assert.assertEquals(adapterChunks.get(0).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(1).getState(), ChunkHealthState.Broken);
		Assert.assertEquals(adapterChunks.get(2).getState(), ChunkHealthState.Healthy);
		
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
