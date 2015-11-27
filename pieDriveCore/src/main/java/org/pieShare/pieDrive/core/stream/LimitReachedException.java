package org.pieShare.pieDrive.core.stream;

import java.io.IOException;

public class LimitReachedException extends IOException {
	public LimitReachedException(String message) {
		super(message);
	}
}
