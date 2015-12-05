/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * This class is meant to be used together with {@link BufferedOutputStream}
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class NioOutputStream extends OutputStream {

	private RandomAccessFile file;
	private long fileOffset;
	
	public NioOutputStream(RandomAccessFile file, long fileOffset) {
		this.file = file;
		this.fileOffset = fileOffset;
	}

	@Override
	public void write(int b) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		synchronized (file) {
			this.file.seek(fileOffset);
			this.file.write(b, off, len);
			this.fileOffset += len;
		}
	}

}
