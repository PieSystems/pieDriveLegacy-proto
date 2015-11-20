/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import org.pieShare.pieDrive.core.PieDriveCore;
import org.pieShare.pieDrive.core.model.AdapterChunk;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class HashingInputStream extends InputStream {
	private InputStream stream;
	private MessageDigest messageDigest;
	private AdapterChunk chunk;
	private PieDriveCore core;
	
	public HashingInputStream(InputStream in) {
		stream = in;
	}

	@Override
	public int read() throws IOException {
		int res = stream.read(); 
		
		if(res == -1) {
			byte[] hash = messageDigest.digest();
			//todo: maybe directly save to DB here
			core.handleHash(chunk, hash);
		}
		
		if(res != -1) {
			messageDigest.update((byte)res);
		}
		
		return res;
	}
}
