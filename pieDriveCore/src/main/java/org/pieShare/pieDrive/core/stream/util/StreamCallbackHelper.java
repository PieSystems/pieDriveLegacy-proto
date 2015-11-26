/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream.util;

import java.security.MessageDigest;
import org.pieShare.pieDrive.core.model.AdapterChunk;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class StreamCallbackHelper {
	private HashingDoneCallback callback;
	private String callbackId;
	private boolean done;
	
	public StreamCallbackHelper() {
		this.done = false;
	}

	public void setCallbackId(String callbackId) {
		this.callbackId = callbackId;
	}

	public void setCallback(HashingDoneCallback callback) {
		this.callback = callback;
	}

	public void done(byte[] hash) {
		if(this.done) {
			return;
		}
		
		this.done = true;
		callback.hashingDone(this.callbackId, hash);
	}
}
