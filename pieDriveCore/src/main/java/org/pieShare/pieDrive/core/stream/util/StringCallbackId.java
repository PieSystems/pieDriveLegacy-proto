/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream.util;

import org.pieShare.pieDrive.core.model.AdapterChunk;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class StringCallbackId implements ICallbackId {
	private String chunkId;

	public String getChunk() {
		return chunkId;
	}

	public StringCallbackId setChunk(String chunk) {
		this.chunkId = chunk;
		return this;
	}
}
