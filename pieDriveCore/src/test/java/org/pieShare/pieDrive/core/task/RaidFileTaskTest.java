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
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import javax.inject.Provider;
import jdk.nashorn.internal.objects.annotations.Setter;
import org.apache.commons.io.FileUtils;
import org.pieShare.pieDrive.core.IntegrationTestBase;
import org.pieShare.pieDrive.core.PieDriveCoreService;
import org.pieShare.pieDrive.core.SimpleAdapterCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.database.DatabaseFactory;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.stream.util.PhysicalChunkCallbackId;
import org.pieShare.pieDrive.core.stream.util.StreamCallbackHelper;
import org.pieShare.pieDrive.core.task.help.FakeAdapter;
import org.pieShare.pieDrive.core.task.help.FakeAdapterCallCounter;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorTaskFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RaidFileTaskTest extends IntegrationTestBase {

	private UploadRaidFileTask uploadTask;
	private DownloadRaidFileTask downloadTask;
	PieExecutorService executorService;
	SimpleAdapterCoreService aCore;
	Database db;
	private FakeAdapterCallCounter counter;
	
	private File testFolder;
	private File upload;
	private File in;
	private File out;	

	@Override
	@BeforeTest
	public void setUpIt() throws IOException {
		super.setUpIt();
		this.testFolder = new File(this.integrationTestFolder, "test");
		
		//init new DB
		File dbFile = new File(super.integrationTestFolder, "databaseTaskTest.odb");
		DatabaseFactory fac = new DatabaseFactory();
		fac.setDatabaseName(dbFile.getPath());
		fac.init();
		db = new Database();
		db.setDatabseFactory(fac);
		
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

		//create tasks
		uploadTask = new UploadRaidFileTask();
		downloadTask = new DownloadRaidFileTask();

		//create and set adapter core service
		this.counter = new FakeAdapterCallCounter(0);
		aCore = new SimpleAdapterCoreService();
		aCore.registerAdapter((new AdapterId()).setId("fakeDropBox"), new FakeAdapter(this.upload, this.counter));
		aCore.registerAdapter((new AdapterId()).setId("fakeBox"), new FakeAdapter(this.upload, this.counter));
		aCore.registerAdapter((new AdapterId()).setId("fakeS3"), new FakeAdapter(this.upload, this.counter));
		uploadTask.setAdapterCoreService(aCore);
		downloadTask.setAdapterCoreService(aCore);

		//create and set providers
		Provider<PhysicalChunkCallbackId> physicalChunkCallbackIdProvider = new Provider<PhysicalChunkCallbackId>() {
			@Override
			public PhysicalChunkCallbackId get() {
				return new PhysicalChunkCallbackId();
			}
		};

		Provider<StreamCallbackHelper> streamCallbackHelperProvider = new Provider<StreamCallbackHelper>() {
			@Override
			public StreamCallbackHelper get() {
				return new StreamCallbackHelper();
			}
		};

		uploadTask.setAdapterChunkProvider(new Provider<AdapterChunk>() {
			@Override
			public AdapterChunk get() {
				return new AdapterChunk();
			}
		});
		uploadTask.setPhysicalChunkCallbackIdProvider(physicalChunkCallbackIdProvider);
		uploadTask.setStreamCallbackHelperProvider(streamCallbackHelperProvider);
		uploadTask.setUploadChunkTaskProvider(new Provider<UploadChunkTask>() {
			@Override
			public UploadChunkTask get() {
				UploadChunkTask task = new UploadChunkTask();
				task.setAdapterCoreService(aCore);
				return task;
			}
		});

		downloadTask.setDownloadChunkProvider(new Provider<DownloadChunkTask>() {
			@Override
			public DownloadChunkTask get() {
				DownloadChunkTask task = new DownloadChunkTask();
				task.setAdapterCoreService(aCore);
				return task;
			}
		});
		downloadTask.setPhysicalChunkCallbackIdProvider(physicalChunkCallbackIdProvider);
		downloadTask.setStreamCallbackHelperProvider(streamCallbackHelperProvider);

		//create and set pie drive core service
		PieDriveCoreService pCore = new PieDriveCoreService();
		pCore.setChunkSize(20); //20 byte
		uploadTask.setDriveCoreService(pCore);

		//create and set executor service
		executorService = PieExecutorService.newCachedPieExecutorService();
		PieExecutorTaskFactory execFactory = new PieExecutorTaskFactory();
		executorService.setExecutorFactory(execFactory);
		uploadTask.setExecutorService(executorService);
		downloadTask.setExecutorService(executorService);
		
		uploadTask.setDatabase(db);
	}

	@AfterMethod
	public void tearDown() {
		executorService.shutdown();
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
	//@Test
	public void testUpAndDownLoadFileRaid1() throws Exception {
		String fileName = "testOneChunkFile";
		File expected = this.createFileHelper(this.in, fileName, 15);
		uploadTask.setFile(expected);
		uploadTask.run();
		
		Thread.sleep(2000);
		
		File[] uploadedFiles = this.upload.listFiles();
		Assert.assertEquals(3, uploadedFiles.length);
		byte[] expectedBytes = this.generateMd5(expected);
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFiles[0]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFiles[1]));
		Assert.assertEquals(expectedBytes, this.generateMd5(uploadedFiles[2]));
		
		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
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
		uploadTask.setFile(expected);
		uploadTask.run();
		
		Thread.sleep(2000);
		
		File[] uploadedFiles = this.upload.listFiles();
		Assert.assertEquals(15, uploadedFiles.length);
		
		PieRaidFile raidFile = this.db.findPieRaidFileByName(fileName);
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
