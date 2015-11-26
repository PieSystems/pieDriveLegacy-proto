/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream;

import org.pieShare.pieDrive.core.stream.util.StreamCallbackHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

/**
 *This class extends {@link DigestOutputStream} to make a callback when done
 * with hashing.
 * 
 * Note: The difference to the {@link HashingInputStream} is that this class will only
 * trigger the callback when {@link #close() close} is called.
 * 
 * The difference is that in case of writing to the file system we can still
 * compare the hash value with the DB and distinguish between error and 
 * normal mode.
 * 
 * Important: {@link OutputStream} does not in generally support something like
 * -1 if a more general solution is needed this class has to be adapted to work
 * similar to the {@link HashingInputStream} class.
 * 
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class HashingOutputStream extends DigestOutputStream {
	private StreamCallbackHelper callback;

	public HashingOutputStream(OutputStream stream, MessageDigest digest, StreamCallbackHelper cb) {
		super(stream, digest);
		this.callback = cb;
	}

	@Override
	public void close() throws IOException {
		super.close();
		this.callback.done(this.digest.digest());
	}
}
