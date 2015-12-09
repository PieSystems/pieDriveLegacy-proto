/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public abstract class IntegrationTestBase extends AbstractTestNGSpringContextTests {
	
	protected File integrationTestFolder;
	
	@BeforeClass
	public void setUpIt() throws IOException, Exception {
		this.integrationTestFolder = new File("integrationTest");
		cleanup();
		this.integrationTestFolder.mkdir();
	}
	
	@AfterTest
	public void cleanUpIt() throws IOException {
		cleanup();
	}
	
	private void cleanup() throws IOException {
		if(this.integrationTestFolder.exists()) {
			FileUtils.deleteDirectory(integrationTestFolder);
		}
	}
	
}
