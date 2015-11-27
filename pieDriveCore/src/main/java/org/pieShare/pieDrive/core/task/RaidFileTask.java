/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCore;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.stream.HashingInputStream;
import org.pieShare.pieDrive.core.stream.LimitingInputStream;
import org.pieShare.pieDrive.core.stream.util.StringCallbackId;
import org.pieShare.pieDrive.core.stream.util.HashingDoneCallback;
import org.pieShare.pieDrive.core.stream.util.ICallbackId;
import org.pieShare.pieDrive.core.stream.util.StreamCallbackHelper;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RaidFileTask implements IPieTask, HashingDoneCallback {

	private File file;
	private int reportedBack = 0;
	private PieRaidFile raidedFile;

	private IExecutorService executorService;
	private PieDriveCore driveCoreService;
	private AdapterCoreService adapterCoreService;

	private Provider<StreamCallbackHelper> streamCallbackHelperProvider;
	private Provider<AdapterChunk> adapterChunkProvider;
	private Provider<UploadChunkTask> uploadChunkTaskProvider;
	private Provider<StringCallbackId> stringCallbackIdProvider;

	public void run() {
		raidedFile = driveCoreService.calculateRaidFile(file);

		for (PhysicalChunk physicalChunk : raidedFile.getChunks()) {
			for (AdapterId id : adapterCoreService.getAdaptersKey()) {

				FileInputStream fileStr = null;
				LimitingInputStream lStr = null;
				HashingInputStream hStr = null;

				try {
					AdapterChunk chunk = adapterChunkProvider.get();
					chunk.setAdapterId(id);
					chunk.setUuid(UUID.randomUUID().toString());

					physicalChunk.addAdapterChunk(chunk);

					fileStr = StreamFactory.getFileInputStream(file);
					fileStr.skip(physicalChunk.getOffset());
					lStr = StreamFactory.getLimitingInputStream(fileStr, physicalChunk.getSize());

					StreamCallbackHelper cb = this.streamCallbackHelperProvider.get();
					cb.setCallback(this);
					StringCallbackId chunkId = this.stringCallbackIdProvider.get();
					chunkId.setChunk(chunk.getUuid());
					cb.setCallbackId(chunkId);
					//todo-pieShare: proper referencing to the provider will be needed
					hStr = StreamFactory.getHashingInputStream(lStr, MessageDigest.getInstance("MD5"), cb);

					UploadChunkTask task = uploadChunkTaskProvider.get();
					task.setChunk(chunk);
					task.setStream(hStr);

					this.executorService.execute(task);
				} catch (IOException | NoSuchAlgorithmException ex) {
					Logger.getLogger(RaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
					
					this.silentlyCloseStream(fileStr);
					this.silentlyCloseStream(lStr);
					this.silentlyCloseStream(hStr);
				}
			}
		}

		//todo: instead do incremental DB saves?
		//first save structure and then update hashes
		//todo: this is wrong!!! every physical chunk has three chunks that need to report back
		while (this.reportedBack < 3) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Logger.getLogger(RaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		//todo: save this to the DB
	}

	private void silentlyCloseStream(InputStream stream) {
		if (stream == null) {
			return;
		}
		
		try {
			stream.close();
		} catch (IOException ex) {
			Logger.getLogger(RaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void hashingDone(ICallbackId id, byte[] hash) {
		StringCallbackId cbId = (StringCallbackId) id;
		//todo: save hash to DB
		this.reportedBack--;
	}
}
