package org.pieShare.pieDrive.core.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

//TODO rename to BoundedInputStream
public class BoundedInputStream extends InputStream {
	private final InputStream in;
	private final long limit;
	private long readBytes = 0;
	
	public BoundedInputStream(InputStream in, long limit) {
		this.in = in;
		this.limit = limit;
	}

	@Override
	public int read() throws IOException {
		readBytes++;
		
		if(readBytes > limit) {
			return -1;
		}
		
		return in.read();
	}
}
