/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class DropboxConfig implements Config {
	private String accessToken = "xH3xc5-r9gAAAAAAAAAABdOYq3F9CGn0DvpdXYkLrj0Fa4zggF34i3prqVmM5qfV";
	
	public void setAccessToken(String token) {
		this.accessToken = token;
	}

	@Override
	public void saveConfig() {
		String path = System.getProperty("user.home");
		File pieDrive = new File(path, ".pieDrive");
		pieDrive.mkdirs();
		File dropboxtoken = new File(pieDrive, "dropboxtoken");
		
		if(dropboxtoken.exists()){
			dropboxtoken.delete();
		}
				
		try(PrintWriter out = new PrintWriter(dropboxtoken)){
			out.print(this.accessToken);
		}catch(FileNotFoundException e){}
	}
}
