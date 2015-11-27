package org.pieShare.pieDrive.core.stream;

import java.io.IOException;
import java.io.OutputStream;

public class BoundedOutputStream extends OutputStream {
	private final OutputStream out;
	private final long limit;
	private long writtenBytes = 0;
	
	public BoundedOutputStream(OutputStream out, long limit) {
		this.out = out;
		this.limit = limit;
	}

	@Override
	public void write(int b) throws IOException {
		writtenBytes++;
		
		if(writtenBytes > limit) {
			throw new LimitReachedException("Stream end should have been reached.");
		}
		
		out.write(b);
	}
}
