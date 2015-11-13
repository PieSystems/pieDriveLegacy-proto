package org.pieShare.pieDrive.core.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitingInputStream extends FilterInputStream {
	private final long limit;
	private long readBytes = 0;
	
	public LimitingInputStream(InputStream in, long limit) {
		super(in);
		this.limit = limit;
	}

	@Override
	public int read() throws IOException {
		readBytes++;
		
		if(readBytes > limit) {
			return -1;
		}
		
		return super.read();
	}
}
