package org.pieShare.pieDrive.core.task;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;
import org.apache.commons.io.output.NullOutputStream;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

public class IntegrityCheckTask extends ADownloadChunkTask implements IPieTask {
	
	private Provider<UploadChunkTask> uploadChunkTaskProvider;
	private IExecutorService executorService;
	private RandomAccessFile file;

	public void setUploadChunkTaskProvider(Provider<UploadChunkTask> uploadChunkTaskProvider) {
		this.uploadChunkTaskProvider = uploadChunkTaskProvider;
	}
	
	public void setExecutorService(IExecutorService executorService) {
		this.executorService = executorService;
	}

	public void setFile(RandomAccessFile file) {
		this.file = file;
	}
	
	@Override
	public void run() {
		
		AdapterChunk healtyChunk = null;
		
		for (AdapterChunk adapterChunk : physicalChunk.getChunks()) {
			if(adapterChunk != null && adapterChunk.getState() == ChunkHealthState.Healthy) {
				PieLogger.debug(this.getClass(), "Found healthy chunk on {}", adapterChunk.getAdapterId().getId());
				healtyChunk = adapterChunk;
			}
		}
		
		for (AdapterChunk adapterChunk : physicalChunk.getChunks()) {
			if (adapterChunk.getState() == ChunkHealthState.NotChecked) {
				DigestOutputStream digestStream = null;
				try {
					PieLogger.debug(this.getClass(), "Checking chunk on {}", adapterChunk.getAdapterId().getId());
					NullOutputStream nullStream = new NullOutputStream();
					digestStream = StreamFactory.getDigestOutputStream(nullStream, MessageDigest.getInstance("MD5"));

					if(!this.download(adapterChunk, digestStream)) {
						adapterChunk.setState(ChunkHealthState.Broken);
					}
				} catch (NoSuchAlgorithmException | AdaptorException ex) {
					Logger.getLogger(IntegrityCheckTask.class.getName()).log(Level.SEVERE, null, ex);
					if (digestStream != null) {
						try {
							digestStream.close();
						} catch (IOException e) {
							Logger.getLogger(IntegrityCheckTask.class.getName()).log(Level.SEVERE, null, e);
						}
					}
				}
			}
			
			if(adapterChunk.getState() == ChunkHealthState.Broken) {
				PieLogger.debug(this.getClass(), "Recovering chunk {} on adapter {}", adapterChunk.getUuid(), adapterChunk.getAdapterId().getId());
				UploadChunkTask task = this.uploadChunkTaskProvider.get();
				task.setChunk(adapterChunk);
				task.setPhysicalChunk(physicalChunk);
				task.setFile(file);
				executorService.execute(task);
			}
		}
	}
}
