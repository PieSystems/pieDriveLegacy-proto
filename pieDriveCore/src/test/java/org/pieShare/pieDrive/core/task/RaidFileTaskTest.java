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
import java.util.Random;
import javax.inject.Provider;
import org.apache.commons.io.FileUtils;
import org.pieShare.pieDrive.core.IntegrationTestBase;
import org.pieShare.pieDrive.core.PieDriveCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.database.DatabaseFactory;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.task.config.CoreTestConfig;
import org.pieShare.pieDrive.core.task.help.FakeAdapter;
import org.pieShare.pieDrive.core.task.help.FakeAdapterCallCounter;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
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
	
	private File testFolder;
	private File upload;
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
		this.upload = new File(this.testFolder, "upload");
		this.upload.mkdirs();
		this.in = new File(this.testFolder, "in");
		this.in.mkdirs();
		this.out = new File(this.testFolder, "out");
		this.out.mkdirs();
		
		FakeAdapter adapter = super.applicationContext.getBean("s3Adapter", FakeAdapter.class);
		adapter.setParent(this.upload);
		adapter = super.applicationContext.getBean("boxAdapter", FakeAdapter.class);
		adapter.setParent(this.upload);
		adapter = super.applicationContext.getBean("dropboxAdapter", FakeAdapter.class);
		adapter.setParent(this.upload);

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
	
	private byte[] generateMd5(File file) throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		FileInputStream fio = new FileInputStream(file);
		DigestInputStream dio = new DigestInputStream(fio, MessageDigest.getInstance("MD5"));
		while(dio.read() != -1) {
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
		PieLogger.trace(RaidFileTaskTest.class, "Filename: {}", expected.getName());
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.run();
		
		Thread.sleep(2000);
		
		File[] uploadedFiles = this.upload.listFiles();
		Assert.assertEquals(uploadedFiles.length, 3);
		byte[] expectedBytes = this.generateMd5(expected);
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFiles[0]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFiles[1]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFiles[2]));
		
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
	public void testUpAndDownLoadFileRaid1WithMultiChunks() throws Exception {
		String fileName = "testMultiChunkFile";
		File expected = this.createFileHelper(this.in, fileName, 96);
		UploadRaidFileTask uploadTask = this.uploadRaidFileProvider.get();
		uploadTask.setFile(expected);
		uploadTask.run();
		
		Thread.sleep(2000);
		
		File[] uploadedFiles = this.upload.listFiles();
		Assert.assertEquals(15, uploadedFiles.length);
		
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
}
