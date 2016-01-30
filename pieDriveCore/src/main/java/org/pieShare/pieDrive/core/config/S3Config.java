/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class S3Config implements Config {
	private String awsAccessKeyId = "";
	private String awsSecretAccessKey = "";
	
	public void setKeys(String awsAccesKeyId, String awsSecretAccessKey){
		this.awsAccessKeyId = awsAccesKeyId;
		this.awsSecretAccessKey = awsSecretAccessKey;
	}

	@Override
	public void saveConfig() {
		String aws = "[default]\n" +
		"aws_access_key_id = %s\n" +
		"aws_secret_access_key = %s";
		
		String path = System.getProperty("user.home");
		File pieDrive = new File(path, ".pieDrive");
		pieDrive.mkdirs();
		File awsFile = new File(pieDrive, "aws");
		
		if(awsFile.exists()){
			awsFile.delete();
		}
		
		aws = String.format(aws, this.awsAccessKeyId, this.awsSecretAccessKey);
				
		try(PrintWriter out = new PrintWriter(awsFile)){
			out.print(aws);
		}catch(FileNotFoundException e){}
	}
	
	
}
