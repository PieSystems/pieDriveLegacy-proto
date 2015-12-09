/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.stream.BoundedOutputStream;
import org.pieShare.pieDrive.core.stream.NioOutputStream;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class DownloadChunkTask implements IPieTask {

	private AdapterChunk chunk;
	private AdapterCoreService adapterCoreService;
	private PhysicalChunk physicalChunk;
	private RandomAccessFile file;

	public void setChunk(AdapterChunk chunk) {
		this.chunk = chunk;
	}

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setPhysicalChunk(PhysicalChunk physicalChunk) {
		this.physicalChunk = physicalChunk;
	}

	public void setFile(RandomAccessFile file) {
		this.file = file;
	}

	@Override
	public void run() {

		while (this.chunk != null) {

			DigestOutputStream hStr = null;
			try {
				NioOutputStream nioStream = StreamFactory.getNioOutputStream(file, physicalChunk.getOffset());
				BufferedOutputStream bufferedStream = StreamFactory.getBufferedOutputStream(nioStream, 65536); //64kB
				BoundedOutputStream boudedStream = StreamFactory.getBoundedOutputStream(bufferedStream, physicalChunk.getSize());
				hStr = StreamFactory.getDigestOutputStream(boudedStream, MessageDigest.getInstance("MD5"));

				adapterCoreService.getAdapter(chunk.getAdapterId()).download(chunk, hStr);
				byte[] hash = hStr.getMessageDigest().digest();

				if (Arrays.equals(physicalChunk.getHash(), hash)) {
					return;
				}

				this.chunk = null;

				if (physicalChunk.getChunks().size() > 0) {
					AdapterId chunkId = physicalChunk.getChunks().keySet().iterator().next();
					this.chunk = physicalChunk.getChunks().remove(chunkId);
				}

			} catch (NoSuchAlgorithmException ex) {
				Logger.getLogger(DownloadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
				try {
					hStr.close();
				} catch (IOException e) {
					Logger.getLogger(DownloadRaidFileTask.class.getName()).log(Level.SEVERE, null, e);
				}
			}
		}
	}

}
