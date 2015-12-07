/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import javax.inject.Provider;
import org.apache.commons.io.FileUtils;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.IntegrationTestBase;
import org.pieShare.pieDrive.core.PieDriveCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.database.DatabaseFactory;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.task.config.CoreTestConfig;
import org.pieShare.pieDrive.core.task.help.FakeAdapter;
import org.pieShare.pieDrive.core.task.help.FakeAdapterCallCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
@DirtiesContext
@ContextConfiguration(classes = CoreTestConfig.class)
public class RaidFileTaskTest extends IntegrationTestBase {

	@Autowired
	private Provider<UploadRaidFileTask> uploadRaidFileProvider;
	@Autowired
	private Provider<DownloadRaidFileTask> downloadRaidFileProvider;

	@Autowired
	private FakeAdapterCallCounter counter;
	@Autowired
	private Database db;
	@Autowired
	private DatabaseFactory fac;
	@Autowired
	private AdapterCoreService adapterCoreService;

	private File testFolder;
	private File uploadBase;
	private File uploadAdapter1;
	private File uploadAdapter2;
	private File uploadAdapter3;
	private File in;
	private File out;

	@Override
	@BeforeClass
	public void setUpIt() throws IOException, Exception {
		super.setUpIt();
		this.testFolder = new File(this.integrationTestFolder, "test");

		//init new DB
		File dbFile = new File(super.integrationTestFolder, "databaseTaskTest.odb");
		fac.setDatabaseName(dbFile.getPath());
		fac.init();
	}

	@BeforeMethod
	public void setUp() throws IOException {
		if (testFolder.exists()) {
			FileUtils.deleteDirectory(testFolder);
		}

		this.testFolder.mkdirs();
		this.uploadBase = new File(this.testFolder, "upload");
		this.uploadBase.mkdirs();
		this.uploadAdapter1 = new File(this.uploadBase, "adapter1");
		this.uploadAdapter1.mkdirs();
		this.uploadAdapter2 = new File(this.uploadBase, "adapter2");
		this.uploadAdapter2.mkdirs();
		this.uploadAdapter3 = new File(this.uploadBase, "adapter3");
		this.uploadAdapter3.mkdirs();
		this.in = new File(this.testFolder, "in");
		this.in.mkdirs();
		this.out = new File(this.testFolder, "out");
		this.out.mkdirs();

		Object[] adapterKeys = adapterCoreService.getAdaptersKey().toArray();
		FakeAdapter adapter = (FakeAdapter)adapterCoreService.getAdapter((AdapterId)adapterKeys[0]);
		adapter.setParent(this.uploadAdapter1);
		adapter = (FakeAdapter)adapterCoreService.getAdapter((AdapterId)adapterKeys[1]);
		adapter.setParent(this.uploadAdapter2);
		adapter = (FakeAdapter)adapterCoreService.getAdapter((AdapterId)adapterKeys[2]);
		adapter.setParent(this.uploadAdapter3);

		//reset download counter
		this.counter.setCounter(0);

		//set chunk size for tests
		PieDriveCoreService pCore = super.applicationContext.getBean(PieDriveCoreService.class);
		pCore.setChunkSize(20); //20 byte
	}

	private File createFileHelper(File parent, String fileName, int size) throws IOException {
		File file = new File(parent, fileName);
		FileOutputStream fstr = new FileOutputStream(file);
		byte[] bytes = new byte[size];
		Random r = new Random();
		r.nextBytes(bytes);
		fstr.write(bytes);
		fstr.flush();
		fstr.close();
		return file;
	}

	private void corruptFile(File file) throws IOException {
		FileOutputStream fstr = new FileOutputStream(file, false);
		byte[] bytes = new byte[(int) file.length()];
		Random r = new Random();
		r.nextBytes(bytes);
		fstr.write(bytes);
		fstr.flush();
		fstr.close();
	}

	private byte[] generateMd5(File file) throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		FileInputStream fio = new FileInputStream(file);
		DigestInputStream dio = new DigestInputStream(fio, MessageDigest.getInstance("MD5"));
		while (dio.read() != -1) {
		}
		return dio.getMessageDigest().digest();
	}

	/**
	 * Test of run method, of class RaidFileTask.
	 */
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
		
		for(File file : uploadedFilesAdapter1) {
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
