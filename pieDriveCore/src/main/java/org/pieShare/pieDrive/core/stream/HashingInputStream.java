/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream;

import java.io.FilterInputStream;
import org.pieShare.pieDrive.core.stream.util.HashingStreamCallbackHelper;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 *
 * This class extends {@link DigestInputStream} to make a callback when done
 * with hashing.
 * <p>
 * Important: Only when end of stream is detected this callback will be triggered.
 * {@link #close() close} will not trigger the callback because {@link #close() close} is
 * also called in error cases!
 * 
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class HashingInputStream extends DigestInputStream {
	private HashingStreamCallbackHelper callback;
	
	public HashingInputStream(InputStream stream, MessageDigest dig, HashingStreamCallbackHelper cb) {
		super(stream, dig);
		this.callback = cb;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int number = super.read(b, off, len);
		
		//if the amount of returned bytes is smaller the requested
		//or we get -1 we reached the end of the allowed block and hashing is
		//done
		if(number < len || (number == 1 && b[0] == -1)) {
			this.callback.done(this.digest.digest());
		}
		
		return number;
	}

	@Override
	public int read() throws IOException {
		int in = super.read();
		
		if(in == -1) {
			this.callback.done(this.digest.digest());
		}
		
		return in;
	}
}
