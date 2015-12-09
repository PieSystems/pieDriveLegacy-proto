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
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.IntegrationTestBase;
import org.pieShare.pieDrive.core.PieDriveCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.database.DatabaseFactory;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.task.help.FakeAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public abstract class FileHandlingTaskTestBase extends IntegrationTestBase {
	@Autowired
	protected Provider<UploadRaidFileTask> uploadRaidFileProvider;
	@Autowired
	protected Provider<DownloadRaidFileTask> downloadRaidFileProvider;
	@Autowired
	protected Provider<IntegrityCheckTask> integrityCheckTaskProvider;

	@Autowired
	protected Database db;
	@Autowired
	protected DatabaseFactory fac;
	@Autowired
	protected AdapterCoreService adapterCoreService;

	protected File testFolder;
	protected File uploadBase;
	protected File uploadAdapter1;
	protected File uploadAdapter2;
	protected File uploadAdapter3;
	protected File in;
	protected File out;

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
		FakeAdapter adapter = (FakeAdapter) adapterCoreService.getAdapter((AdapterId) adapterKeys[0]);
		adapter.setParent(this.uploadAdapter1);
		adapter = (FakeAdapter) adapterCoreService.getAdapter((AdapterId) adapterKeys[1]);
		adapter.setParent(this.uploadAdapter2);
		adapter = (FakeAdapter) adapterCoreService.getAdapter((AdapterId) adapterKeys[2]);
		adapter.setParent(this.uploadAdapter3);

		//set chunk size for tests
		PieDriveCoreService pCore = super.applicationContext.getBean(PieDriveCoreService.class);
		pCore.setChunkSize(20); //20 byte
	}

	protected File createFileHelper(File parent, String fileName, int size) throws IOException {
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

	protected void corruptFile(File file) throws IOException {
		FileOutputStream fstr = new FileOutputStream(file, false);
		byte[] bytes = new byte[(int) file.length()];
		Random r = new Random();
		r.nextBytes(bytes);
		fstr.write(bytes);
		fstr.flush();
		fstr.close();
	}

	protected byte[] generateMd5(File file) throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		FileInputStream fio = new FileInputStream(file);
		DigestInputStream dio = new DigestInputStream(fio, MessageDigest.getInstance("MD5"));
		while (dio.read() != -1) {
		}
		return dio.getMessageDigest().digest();
	}
}