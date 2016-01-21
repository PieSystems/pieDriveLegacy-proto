/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.stream.BoundedOutputStream;
import org.pieShare.pieDrive.core.stream.NioOutputStream;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class DownloadChunkTask extends ADownloadChunkTask implements IPieTask {

	private PieExecutorService executor;
	private IntegrityCheckTask task;
	
	private RandomAccessFile file;
	private int adapterIndex;

	public void setExecutor(PieExecutorService executor) {
		this.executor = executor;
	}

	public void setTask(IntegrityCheckTask task) {
		this.task = task;
	}
	
	public void setAdapterIndex(int adapterIndex) {
		this.adapterIndex = adapterIndex;
	}

	public void setFile(RandomAccessFile file) {
		this.file = file;
	}

	@Override
	public void run() {
		ArrayList<AdapterId> adatperIds = new ArrayList<>(this.adapterCoreService.getAdaptersKey());
		int size = adatperIds.size();

		for (int i = 0; i < size; i++) {
			DigestOutputStream hStr = null;
			try {
				NioOutputStream nioStream = StreamFactory.getNioOutputStream(file, physicalChunk.getOffset());
				BufferedOutputStream bufferedStream = StreamFactory.getBufferedOutputStream(nioStream, 65536); //64kB
				BoundedOutputStream boundedStream = StreamFactory.getBoundedOutputStream(bufferedStream, physicalChunk.getSize());
				hStr = StreamFactory.getDigestOutputStream(boundedStream, MessageDigest.getInstance("MD5"));
				
				AdapterChunk chunk = physicalChunk.getChunk(adatperIds.get(this.adapterIndex));
				
				if(this.download(chunk, hStr)) {
					PieLogger.debug(this.getClass(), "Download successfull for chunk {}", chunk.getUuid());
					this.task.setPhysicalChunk(physicalChunk);
					this.task.setFile(file);
					this.executor.execute(task);
					return;
				}
				
				this.adapterIndex = this.adapterCoreService.calculateNextAdapter(this.adapterIndex);
			} catch (NoSuchAlgorithmException | AdaptorException ex) {
				Logger.getLogger(DownloadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
				try {
					hStr.close();
				} catch (IOException e) {
					Logger.getLogger(DownloadRaidFileTask.class.getName()).log(Level.SEVERE, null, e);
				}
			}
		}
		
		PieLogger.debug(this.getClass(), "Could not find one single uncorrupted chunk!");
		
		//TODO fail task
		//throw new DownloadChunkException("All adapter chunks are corrupt");
	}

}
