/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import org.pieShare.pieDrive.core.stream.HashingInputStream;
import org.pieShare.pieDrive.core.stream.HashingOutputStream;
import org.pieShare.pieDrive.core.stream.LimitingInputStream;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class StreamFactory {
	
	public static FileInputStream getFileInputStream(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}
	
	public static LimitingInputStream getLimitingInputStream(InputStream in, long limit) {
		return new LimitingInputStream(in, limit);
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
	
	public static FileOutputStream getFileOutputStream(File file) throws FileNotFoundException {
		return new FileOutputStream(file);
	}
	
	public static HashingOutputStream getHashingOutputStream(OutputStream stream, MessageDigest dig, StreamCallbackHelper cb) {
		return new HashingOutputStream(stream, dig, cb);
	}
}
