/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ConfigService {

	File pieDrive;
	
	public ConfigService(){
		pieDrive = new File(System.getProperty("user.home"), ".pieDrive");
		if(!(pieDrive.exists())){
			pieDrive.mkdirs();
		}
	}
	
	public boolean saveAWSConfig(String access, String secret){
		String aws = "[default]\n" +
		"aws_access_key_id = %s\n" +
		"aws_secret_access_key = %s";
		
		File awsFile = new File(pieDrive, "aws");
		
		if(awsFile.exists()){
			awsFile.delete();
		}
		
		aws = String.format(aws, access, secret);
				
		return print(awsFile, aws);
	}
	
	public boolean saveDropboxConfig(String token){
		File dropboxtoken = new File(pieDrive, "dropboxtoken");
		
		if(dropboxtoken.exists()){
			dropboxtoken.delete();
		}
		
		return print(dropboxtoken, token);
	}
	
	private boolean print(File f, String s){
		try(PrintWriter out = new PrintWriter(f)){
			out.print(s);
		}catch(FileNotFoundException e){
			return false;
		}
		
		return true;
	}
}
