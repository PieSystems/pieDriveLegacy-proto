/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class PhysicalChunk {

    private List<AdapterChunk> chunks;
    private long offset;
    private long size;
    private byte[] hash;
    public String uuid;

    public PhysicalChunk() {
        chunks = new ArrayList<>();
        uuid = UUID.randomUUID().toString();
    }

    public PhysicalChunk addAdapterChunk(AdapterChunk chunk) {
        this.chunks.add(chunk);
        return this;
    }

    public List<AdapterChunk> getChunks() {
        return chunks;
    }
	
	public AdapterChunk getChunk(AdapterId adapterId) {
		for(AdapterChunk chunk : chunks) {
			if(chunk.getAdapterId().equals(adapterId)) {
				return chunk;
			}
		}
		
		return null;
	}

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof PhysicalChunk)) {
            return false;
        }

        PhysicalChunk rr = (PhysicalChunk) obj;

        return rr.getUuid().equals(this.getUuid());
    }

    @Override
    public int hashCode() {
        return this.getUuid().hashCode();
    }

}
