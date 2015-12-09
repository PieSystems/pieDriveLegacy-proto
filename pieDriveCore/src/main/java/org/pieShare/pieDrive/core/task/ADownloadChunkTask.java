/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.stream.BoundedOutputStream;
import org.pieShare.pieDrive.core.stream.NioOutputStream;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public abstract class ADownloadChunkTask implements IPieTask {

	protected AdapterCoreService adapterCoreService;
	protected PhysicalChunk physicalChunk;
	
	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setPhysicalChunk(PhysicalChunk physicalChunk) {
		this.physicalChunk = physicalChunk;
	}

	protected boolean download(AdapterChunk chunk, DigestOutputStream stream) {
		adapterCoreService.getAdapter(chunk.getAdapterId()).download(chunk, stream);
		byte[] hash = stream.getMessageDigest().digest();

		if (Arrays.equals(physicalChunk.getHash(), hash)) {
			chunk.setState(ChunkHealthState.Healthy);
			return true;
		}

		chunk.setState(ChunkHealthState.Broken);
		return false;
	}
}
