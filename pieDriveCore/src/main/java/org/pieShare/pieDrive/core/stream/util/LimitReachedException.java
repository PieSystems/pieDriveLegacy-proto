package org.pieShare.pieDrive.core.stream.util;

import java.io.IOException;

public class LimitReachedException extends IOException {
	public LimitReachedException(String message) {
		super(message);
	}
}
