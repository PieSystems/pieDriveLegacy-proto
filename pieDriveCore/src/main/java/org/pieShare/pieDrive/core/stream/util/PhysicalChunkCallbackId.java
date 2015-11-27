/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream.util;

import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.PhysicalChunk;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class PhysicalChunkCallbackId implements ICallbackId {
	private PhysicalChunk physicalChunk;
	private AdapterChunk chunk;

	public PhysicalChunk getPhysicalChunk() {
		return physicalChunk;
	}

	public void setPhysicalChunk(PhysicalChunk physicalChunk) {
		this.physicalChunk = physicalChunk;
	}

	public AdapterChunk getChunk() {
		return chunk;
	}

	public void setChunk(AdapterChunk chunk) {
		this.chunk = chunk;
	}
}
