/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class PhysicalChunk {

    private Map<AdapterId, AdapterChunk> chunks;
    private long offset;
    private long size;

    public PhysicalChunk() {
        chunks = new HashMap<>();
    }

    public PhysicalChunk addAdapterChunk(AdapterChunk chunk) {
        this.chunks.put(chunk.getAdapterId(), chunk);
        return this;
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

    public Map<AdapterId, AdapterChunk> getChunks() {
        return chunks;
    }

}
