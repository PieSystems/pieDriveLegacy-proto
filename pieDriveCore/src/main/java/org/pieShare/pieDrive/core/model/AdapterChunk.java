/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.model;

import org.pieShare.pieDrive.adapter.model.PieDriveFile;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class AdapterChunk extends PieDriveFile {
	private AdapterId adapterId;
	private byte[] hash;
	private ChunkHealthState state;
	private int dataShard;
	
	public AdapterChunk() {
		this.state = ChunkHealthState.NotChecked;
		this.dataShard = 0;
	}

	public AdapterId getAdapterId() {
		return adapterId;
	}

	public AdapterChunk setAdapterId(AdapterId adapterId) {
		this.adapterId = adapterId;
		return this;
	}
	
	public byte[] getHash() {
		return hash;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public ChunkHealthState getState() {
		return state;
	}

	public void setState(ChunkHealthState state) {
		this.state = state;
	}

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof AdapterChunk)) {
            return false;
        }

        AdapterChunk rr = (AdapterChunk)obj;
        
        return rr.getUuid().equals(this.getUuid());
    }

    @Override
    public int hashCode() {
        return this.getUuid().hashCode();
    }

	public int getDataShard() {
		return dataShard;
	}

	public void setDataShard(int dataShard) {
		this.dataShard = dataShard;
	}
}
