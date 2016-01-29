package org.pieShare.pieDrive.core.task;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.stream.BoundedInputStream;
import org.pieShare.pieDrive.core.stream.NioInputStream;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

public class UploadBufferChunkTask implements IPieTask {

	private AdapterCoreService adapterCoreService;
	private AdapterChunk chunk;
	private Database database;
	private byte[] buffer;

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setChunk(AdapterChunk chunk) {
		this.chunk = chunk;
	}

	public void setBufer(byte[] buffer) {
		this.buffer = buffer;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	@Override
	public void run() {
		PieLogger.debug(this.getClass(), "Starting chunk upload for "
				+ "{} with adapter {}", this.chunk.getUuid(), this.chunk.getAdapterId().getId());
		DigestInputStream hStr = null;
		try {
			ByteArrayInputStream byteArrayStream = new ByteArrayInputStream(buffer);
			hStr = StreamFactory.getDigestInputStream(byteArrayStream, MessageDigest.getInstance("MD5"));

			adapterCoreService.getAdapter(chunk.getAdapterId()).upload(chunk, hStr);
			byte[] hash = hStr.getMessageDigest().digest();
			chunk.setHash(hash);

			//todo: why are we doing this check?
			synchronized (database) {
				this.database.updateAdaptorChunk(chunk);
				return;
			}
		} catch (NoSuchAlgorithmException | AdaptorException ex) {
			Logger.getLogger(UploadChunkTask.class.getName()).log(Level.SEVERE, null, ex);
			try {
				hStr.close();
			} catch (IOException e) {
				Logger.getLogger(UploadChunkTask.class.getName()).log(Level.SEVERE, null, e);
			}
		}
	}

}
