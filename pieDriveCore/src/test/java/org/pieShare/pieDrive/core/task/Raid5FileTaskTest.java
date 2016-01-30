package org.pieShare.pieDrive.core.task;

import com.backblaze.erasure.ReedSolomon;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.pieShare.pieDrive.core.model.AdapterChunk;
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
		File expected = createFileHelper(in, fileName, 15);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(expected);
		UploadRaid5FileTask uploadTask = this.uploadRaid5FileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();
		
		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 1);
		Assert.assertEquals(uploadedFilesAdapter2.length, 1);
		Assert.assertEquals(uploadedFilesAdapter3.length, 1);
		
		assertHashCodes(expectedRaidFile);
		
		//TODO download
	}
	
	@Test
	public void testUpAndDownLoadFileRaid5WithMultiChunks() throws Exception {
		String fileName = "testUpAndDownLoadFileRaid5WithMultiChunks";
		File expected = createFileHelper(in, fileName, 96);
		PieRaidFile expectedRaidFile = pieDriveCore.calculateRaidFile(expected);
		UploadRaid5FileTask uploadTask = this.uploadRaid5FileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.setRaidedFile(expectedRaidFile);
		uploadTask.compute();
		
		File[] uploadedFilesAdapter1 = this.uploadAdapter1.listFiles();
		File[] uploadedFilesAdapter2 = this.uploadAdapter2.listFiles();
		File[] uploadedFilesAdapter3 = this.uploadAdapter3.listFiles();
		Assert.assertEquals(uploadedFilesAdapter1.length, 5);
		Assert.assertEquals(uploadedFilesAdapter2.length, 5);
		Assert.assertEquals(uploadedFilesAdapter3.length, 5);
		
		assertHashCodes(expectedRaidFile);
		
		//TODO download
	}
}
