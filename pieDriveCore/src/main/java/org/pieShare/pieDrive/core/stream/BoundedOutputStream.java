package org.pieShare.pieDrive.core.stream;

import org.pieShare.pieDrive.core.stream.util.LimitReachedException;
import java.io.IOException;
import java.io.OutputStream;

//todo: mention in comment why not extending the filterOutputStream!!!
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

	@Override
	public void close() throws IOException {
		this.out.close();
	}

	@Override
	public void flush() throws IOException {
		this.out.flush();
	}
}
