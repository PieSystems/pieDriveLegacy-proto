/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import org.pieShare.pieDrive.core.stream.BoundedOutputStream;
import org.pieShare.pieDrive.core.stream.HashingInputStream;
import org.pieShare.pieDrive.core.stream.HashingOutputStream;
import org.pieShare.pieDrive.core.stream.BoundedInputStream;
import org.pieShare.pieDrive.core.stream.NioInputStream;
import org.pieShare.pieDrive.core.stream.NioOutputStream;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class StreamFactory {
	
	public static BoundedInputStream getLimitingInputStream(InputStream in, long limit) {
		return new BoundedInputStream(in, limit);
	}
	
	/**
	 * Returns a new HashingInputStream as InputStream.
	 * 
	 * Note1: If you really need a HashingInputStream create another Factory method.
	 * 
	 * Note2: It is not important for the DI to know this Stream as a Bean due 
	 * to the fact that this is already a specific implementation and there is
	 * no Interface structure provided. So exchanging it is difficult. However
	 * for easier testing it could be useful to not create directly in code.
	 * 
	 * @param stream
	 * @param dig
	 * @param cb
	 * @return 
	 */
	public static HashingInputStream getHashingInputStream(InputStream stream, MessageDigest dig, StreamCallbackHelper cb) {
		return new HashingInputStream(stream, dig, cb);
	}
	
	public static HashingOutputStream getHashingOutputStream(OutputStream stream, MessageDigest dig, StreamCallbackHelper cb) {
		return new HashingOutputStream(stream, dig, cb);
	}
	
	public static BoundedOutputStream getBoundedOutputStream(OutputStream out, long limit) {
		return new BoundedOutputStream(out, limit);
	}

	public static BufferedOutputStream getBufferedOutputStream(OutputStream out, int size) {
		return new BufferedOutputStream(out, size);
	}

	public static NioOutputStream getNioOutputStream(RandomAccessFile file, long offset) {
		return new NioOutputStream(file, offset);
	}

	public static NioInputStream getNioInputStream(RandomAccessFile file, long offset) {
		return new NioInputStream(file, offset);
	}

	public static BufferedInputStream getBufferedInputStream(InputStream in, int size) {
		return new BufferedInputStream(in, size);
	}
}
