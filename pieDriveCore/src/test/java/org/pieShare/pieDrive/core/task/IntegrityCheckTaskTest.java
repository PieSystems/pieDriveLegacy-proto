package org.pieShare.pieDrive.core.task;

import java.io.File;
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
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter1[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter2[0]), expectedBytes);
		Assert.assertEquals(this.generateMd5(uploadedFilesAdapter3[0]), expectedBytes);

		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
		Assert.assertEquals(raidFile.getChunks().size(), 1);
		//Assert.assertEquals(raidFile.getChunks().get(0).getChunks()., expected);
		
		IntegrityCheckTask checkTask = this.integrityCheckTaskProvider.get();
		checkTask.setPhysicalChunk(raidFile.getChunks().get(0));
		
	}
}
