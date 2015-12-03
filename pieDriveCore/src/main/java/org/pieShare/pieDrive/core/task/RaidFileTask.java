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
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCore;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.stream.HashingInputStream;
import org.pieShare.pieDrive.core.stream.LimitingInputStream;
import org.pieShare.pieDrive.core.stream.util.StringCallbackId;
import org.pieShare.pieDrive.core.stream.util.HashingDoneCallback;
import org.pieShare.pieDrive.core.stream.util.ICallbackId;
import org.pieShare.pieDrive.core.stream.util.PhysicalChunkCallbackId;
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
	//todo: will need abstraction when merging into PieShare
	private Database database;

	private Provider<StreamCallbackHelper> streamCallbackHelperProvider;
	private Provider<AdapterChunk> adapterChunkProvider;
	private Provider<UploadChunkTask> uploadChunkTaskProvider;
	private Provider<PhysicalChunkCallbackId> physicalChunkCallbackIdProvider;
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

					PhysicalChunkCallbackId cbId = this.physicalChunkCallbackIdProvider.get();
					cbId.setChunk(chunk);
					cbId.setPhysicalChunk(physicalChunk);
					cb.setCallbackId(cbId);

//					StringCallbackId chunkId = this.stringCallbackIdProvider.get();
//					chunkId.setChunk(chunk.getUuid());
//					cb.setCallbackId(chunkId);
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

		database.persistPieRaidFile(raidedFile);
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
		PhysicalChunkCallbackId cbId = (PhysicalChunkCallbackId) id;

		PhysicalChunk physicalChunk = cbId.getPhysicalChunk();
		AdapterChunk chunk = cbId.getChunk();
		chunk.setHash(hash);

		//if we are the first and the physical chunk has not yet a hash value
		
		synchronized (physicalChunk) {
			if (physicalChunk.getHash() == null
					|| physicalChunk.getHash().length == 0) {
				physicalChunk.setHash(hash);
				//todo: update phyChunk in DB
			}
		}

		//otherwise do an sanity check and persist hashes
		if (Arrays.equals(physicalChunk.getHash(), hash)) {
			this.database.updateAdaptorChunk(chunk);
			return;
		}

		//this should never happen!!! if this happens two adapters
		//produced different hashes while reading the samephysical chunk
		//todo: log! eventually pass on to user
	}
}
