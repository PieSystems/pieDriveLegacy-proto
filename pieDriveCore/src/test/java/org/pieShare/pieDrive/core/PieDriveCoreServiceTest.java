package org.pieShare.pieDrive.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.pieShare.pieDrive.core.commonTestTools.TestFileUtils;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;

public class PieDriveCoreServiceTest {
	
	private final static long testFileSizeInMB = 17;
	private final long testFileSizeInBytes = testFileSizeInMB * 1024 * 1024;
	private final static String testFileName = "test.file";
	private final static File testFile = new File(testFileName).getAbsoluteFile();
	
	@BeforeClass
	public static void setUpClass() throws InterruptedException, IOException {
		TestFileUtils.createFile(testFile, testFileSizeInMB);
	}
	
	@AfterClass
	public static void tearDownClass() {
		testFile.delete();
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}

	@Test
	public void testCalculateChunks_ChunkSizeBiggerThanFile() {
		long chunkSize = testFileSizeInBytes + 1024;
		createAndVerifyChunks(chunkSize);
	}
	
	@Test
	public void testCalculateChunks_ChunkSizeSmallerThanFile() {
		long chunkSize = 5 * 1024 * 1024;
		createAndVerifyChunks(chunkSize);
	}
	
	@Test
	public void testCalculateChunks_ChunkSizeSameAsFile() {
		long chunkSize = testFileSizeInBytes;
		createAndVerifyChunks(chunkSize);
	}
	
	@Test
	public void testCalculateChunks_ChunkSizeExactDividerOfFileSize() {
		long chunkSize = testFileSizeInBytes / 1024;
		Assert.assertEquals(0, testFileSizeInBytes % chunkSize);
		createAndVerifyChunks(chunkSize);
	}
	
	@Test
	public void testCalculateRaidFile() {
		long chunkSize = 3 * 1024 * 1024;
		PieDriveCoreService service = new PieDriveCoreService();
		service.setChunkSize(chunkSize);
		
		PieRaidFile raidFile = service.calculateRaidFile(testFile);
		Assert.assertEquals(testFileName, raidFile.getFileName());
		Assert.assertEquals(testFile.lastModified(), raidFile.getLastModified());
		Assert.assertEquals(testFile.toPath().toString(), raidFile.getRelativeFilePath());
		verifyChunks(raidFile.getChunks(), chunkSize);
	}
	
	private void createAndVerifyChunks(long chunkSize) {
		PieDriveCoreService service = new PieDriveCoreService();
		service.setChunkSize(chunkSize);
		
		List<PhysicalChunk> chunks = service.calculateChunks(testFile);
		verifyChunks(chunks, chunkSize);
	}
	
	private void verifyChunks(List<PhysicalChunk> chunks, long chunkSize) {
		long chunkCount = testFileSizeInBytes / chunkSize;
		
		long leftoverSize = testFileSizeInBytes % chunkSize;
		if(leftoverSize > 0) {
			Assert.assertEquals(chunkCount + 1, chunks.size());
			Assert.assertEquals(leftoverSize, chunks.get((int)chunkCount).getSize());
			Assert.assertEquals(chunkCount * chunkSize, chunks.get((int)chunkCount).getOffset());
		}
		else {
			Assert.assertEquals(chunkCount, chunks.size());
		}
		
		for(long i = 0; i < chunkCount; i++) {
			Assert.assertEquals(chunkSize, chunks.get((int)i).getSize());
			Assert.assertEquals(i * chunkSize, chunks.get((int)i).getOffset());
		}
	}
}
