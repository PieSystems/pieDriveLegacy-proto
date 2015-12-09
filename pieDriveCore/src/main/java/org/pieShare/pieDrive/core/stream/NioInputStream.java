/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class NioInputStream extends InputStream {

	private RandomAccessFile file;
	private long fileOffset;
	
	public NioInputStream(RandomAccessFile file, long fileOffset) {
		this.file = file;
		this.fileOffset = fileOffset;
	}

	@Override
	public int read() throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		synchronized (file) {
			file.seek(fileOffset);
			int n = file.read(b, off, len);
			this.fileOffset += len;
			return n;
		}
	}

}
