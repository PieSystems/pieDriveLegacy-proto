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
import java.util.concurrent.RecursiveAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.stream.BoundedOutputStream;
import org.pieShare.pieDrive.core.stream.NioOutputStream;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public abstract class ADownloadChunkTask extends RecursiveAction {

	protected AdapterCoreService adapterCoreService;
	protected PhysicalChunk physicalChunk;

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setPhysicalChunk(PhysicalChunk physicalChunk) {
		this.physicalChunk = physicalChunk;
	}

	protected boolean download(AdapterChunk chunk, DigestOutputStream stream) throws AdaptorException {
		PieLogger.debug(this.getClass(), "Downloading chunk {} from {}", chunk.getUuid(), chunk.getAdapterId().getId());
		try {
			adapterCoreService.getAdapter(chunk.getAdapterId()).download(chunk, stream);
		} catch (Exception e) { // catch everything because adapter might not wrap own exceptions in AdaptorExceptions
			chunk.setState(ChunkHealthState.NotChecked);
			return false;
		}
		byte[] hash = stream.getMessageDigest().digest();

		if (Arrays.equals(physicalChunk.getHash(), hash)) {
			chunk.setState(ChunkHealthState.Healthy);
			return true;
		}

		chunk.setState(ChunkHealthState.Broken);
		return false;
	}
}
