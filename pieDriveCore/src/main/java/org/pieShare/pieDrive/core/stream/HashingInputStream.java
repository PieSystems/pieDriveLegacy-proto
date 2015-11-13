/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class HashingInputStream extends InputStream {
	private InputStream stream;
	private MessageDigest messageDigest;
	
	public HashingInputStream(InputStream in) {
		stream = in;
	}

	@Override
	public int read() throws IOException {
		int res = stream.read(); 
		
		if(res == -1) {
			byte[] hash = messageDigest.digest();
			//todo call core
		}
		
		if(res != -1) {
			messageDigest.update((byte)res);
		}
		
		return res;
	}
}
