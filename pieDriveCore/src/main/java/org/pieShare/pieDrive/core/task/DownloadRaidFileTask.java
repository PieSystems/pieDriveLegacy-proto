/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.stream.BoundedOutputStream;
import org.pieShare.pieDrive.core.stream.HashingOutputStream;
import org.pieShare.pieDrive.core.stream.NioOutputStream;
import org.pieShare.pieDrive.core.stream.util.HashingDoneCallback;
import org.pieShare.pieDrive.core.stream.util.ICallbackId;
import org.pieShare.pieDrive.core.stream.util.PhysicalChunkCallbackId;
import org.pieShare.pieDrive.core.stream.util.HashingStreamCallbackHelper;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class DownloadRaidFileTask implements IPieTask, HashingDoneCallback {

	private AdapterCoreService adapterCoreService;
	private IExecutorService executorService;
	private File outputDir;
	
	private PieRaidFile raidFile;
	private RandomAccessFile file;

	private Provider<HashingStreamCallbackHelper> streamCallbackHelperProvider;
	private Provider<DownloadChunkTask> downloadChunkProvider;
	private Provider<PhysicalChunkCallbackId> physicalChunkCallbackIdProvider;

	@Override
	public void run() {
		try {
			ArrayList<AdapterId> adatperIds = new ArrayList<>(adapterCoreService.getAdaptersKey());
			int adapterCounter = -1;
			
			File ffile = new File(this.outputDir, raidFile.getFileName());
			this.file = new RandomAccessFile(ffile, "rw");
			this.file.setLength(raidFile.getFileSize());
			this.file.setLength(this.raidFile.getFileSize());
			
			for (PhysicalChunk physicalChunk : raidFile.getChunks()) {
				adapterCounter = (++adapterCounter) % adatperIds.size();
				AdapterId next = adatperIds.get(adapterCounter);
				AdapterChunk chunk = physicalChunk.getChunks().remove(next);
				
				this.triggerTask(physicalChunk, chunk);
			}
		} catch (IOException ex) {
			Logger.getLogger(DownloadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void triggerTask(PhysicalChunk physicalChunk, AdapterChunk chunk) {
		HashingOutputStream hStr = null;
		try {
			PhysicalChunkCallbackId id = this.physicalChunkCallbackIdProvider.get();
			id.setChunk(chunk);
			id.setPhysicalChunk(physicalChunk);
			
			HashingStreamCallbackHelper cb = this.streamCallbackHelperProvider.get();
			cb.setCallback(this);
			cb.setCallbackId(id);
			
			NioOutputStream nioStream = StreamFactory.getNioOutputStream(file, physicalChunk.getOffset());
			BufferedOutputStream bufferedStream = StreamFactory.getBufferedOutputStream(nioStream, 65536); //64kB
			BoundedOutputStream boudedStream = StreamFactory.getBoundedOutputStream(bufferedStream, physicalChunk.getSize());
			hStr = StreamFactory.getHashingOutputStream(boudedStream, MessageDigest.getInstance("MD5"), cb);
			
			DownloadChunkTask task = downloadChunkProvider.get();
			task.setChunk(chunk);
			task.setStream(hStr);
			executorService.execute(task);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(DownloadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
			try {
				hStr.close();
			} catch (IOException e) {
				Logger.getLogger(DownloadRaidFileTask.class.getName()).log(Level.SEVERE, null, e);
			}
		}
	}

	@Override
	public void hashingDone(ICallbackId id, byte[] hash) {
		PhysicalChunkCallbackId pId = (PhysicalChunkCallbackId) id;

		if(Arrays.equals(pId.getPhysicalChunk().getHash(), hash)) {
			return;
		}
		
		if (pId.getPhysicalChunk().getChunks().size() > 0) {
			AdapterId chunkId = pId.getPhysicalChunk().getChunks().keySet().iterator().next();
			triggerTask(pId.getPhysicalChunk(), pId.getPhysicalChunk().getChunks().remove(chunkId));
		}
		//todo: handle that not a single chunk was okay
	}
	
//	@Override
//	public void bufferFull(ICallbackId id, byte[] buffer, int size) throws IOException {
//		OffsetCallbackId pId = (OffsetCallbackId) id;
//		//todo write at correct position
//		long offset = pId.getOffset();
//		this.file.seek(offset);
//		this.file.write(buffer);
//		pId.setOffset(offset+size);
//	}

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setExecutorService(IExecutorService executorService) {
		this.executorService = executorService;
	}

	public void setStreamCallbackHelperProvider(Provider<HashingStreamCallbackHelper> streamCallbackHelperProvider) {
		this.streamCallbackHelperProvider = streamCallbackHelperProvider;
	}

	public void setDownloadChunkProvider(Provider<DownloadChunkTask> downloadChunkProvider) {
		this.downloadChunkProvider = downloadChunkProvider;
	}

	public void setPhysicalChunkCallbackIdProvider(Provider<PhysicalChunkCallbackId> physicalChunkCallbackIdProvider) {
		this.physicalChunkCallbackIdProvider = physicalChunkCallbackIdProvider;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public void setRaidFile(PieRaidFile raidFile) {
		this.raidFile = raidFile;
	}
}
