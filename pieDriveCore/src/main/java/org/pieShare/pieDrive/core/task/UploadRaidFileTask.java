/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
import org.pieShare.pieDrive.core.stream.BoundedInputStream;
import org.pieShare.pieDrive.core.stream.NioInputStream;
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
public class UploadRaidFileTask implements IPieTask, HashingDoneCallback {

	private File file;
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

	public void run() {
		try {
			raidedFile = driveCoreService.calculateRaidFile(file);
			RandomAccessFile rFile = new RandomAccessFile(file, "r");
			
			//we need to iterate twice so we can guarante that the object 
			//will be completely initizilised before we start working on it
			for (PhysicalChunk physicalChunk : raidedFile.getChunks()) {
				for (AdapterId id : adapterCoreService.getAdaptersKey()) {
					AdapterChunk chunk = adapterChunkProvider.get();
					chunk.setAdapterId(id);
					chunk.setUuid(UUID.randomUUID().toString());

					physicalChunk.addAdapterChunk(chunk);
				}
			}
			
			this.database.persistPieRaidFile(raidedFile);

			for (PhysicalChunk physicalChunk : raidedFile.getChunks()) {
				for (AdapterChunk chunk: physicalChunk.getChunks().values()) {
					NioInputStream nioStream = StreamFactory.getNioInputStream(rFile, physicalChunk.getOffset());
					BufferedInputStream bufferedStream = StreamFactory.getBufferedInputStream(nioStream, 65536); //64kB
					BoundedInputStream lStr = StreamFactory.getLimitingInputStream(bufferedStream, physicalChunk.getSize());

					StreamCallbackHelper cb = this.streamCallbackHelperProvider.get();
					cb.setCallback(this);

					PhysicalChunkCallbackId cbId = this.physicalChunkCallbackIdProvider.get();
					cbId.setChunk(chunk);
					cbId.setPhysicalChunk(physicalChunk);
					cb.setCallbackId(cbId);

					HashingInputStream hStr = StreamFactory.getHashingInputStream(lStr, MessageDigest.getInstance("MD5"), cb);

					UploadChunkTask task = uploadChunkTaskProvider.get();
					task.setChunk(chunk);
					task.setStream(hStr);

					this.executorService.execute(task);
				}
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(UploadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(UploadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void silentlyCloseStream(InputStream stream) {
		if (stream == null) {
			return;
		}

		try {
			stream.close();
		} catch (IOException ex) {
			Logger.getLogger(UploadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void hashingDone(ICallbackId id, byte[] hash) {
		//todo: maybe generalize callback function so we do not have to parse the callback objects!!!
		PhysicalChunkCallbackId cbId = (PhysicalChunkCallbackId) id;

		PhysicalChunk physicalChunk = cbId.getPhysicalChunk();
		AdapterChunk chunk = cbId.getChunk();
		chunk.setHash(hash);

		//if we are the first and the physical chunk has not yet a hash value
		//has to be synchronized for the adapters of the same physical chunk
		//after this point they can work in parallel again
		synchronized (physicalChunk) {
			if (physicalChunk.getHash() == null
					|| physicalChunk.getHash().length == 0) {
				physicalChunk.setHash(hash);
				synchronized (database) {
					this.database.updatePhysicalChunk(physicalChunk);
					return;
				}
			}
		}

		//todo: remove synchronizations after @richy fixes threading in DB
		//otherwise do an sanity check and persist hashes
		if (Arrays.equals(physicalChunk.getHash(), hash)) {
			synchronized (database) {
				this.database.updateAdaptorChunk(chunk);
				return;
			}
		}

		//this should never happen!!! if this happens two adapters
		//produced different hashes while reading the samephysical chunk
		//todo: log! eventually pass on to user
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setExecutorService(IExecutorService executorService) {
		this.executorService = executorService;
	}

	public void setDriveCoreService(PieDriveCore driveCoreService) {
		this.driveCoreService = driveCoreService;
	}

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public void setStreamCallbackHelperProvider(Provider<StreamCallbackHelper> streamCallbackHelperProvider) {
		this.streamCallbackHelperProvider = streamCallbackHelperProvider;
	}

	public void setAdapterChunkProvider(Provider<AdapterChunk> adapterChunkProvider) {
		this.adapterChunkProvider = adapterChunkProvider;
	}

	public void setUploadChunkTaskProvider(Provider<UploadChunkTask> uploadChunkTaskProvider) {
		this.uploadChunkTaskProvider = uploadChunkTaskProvider;
	}

	public void setPhysicalChunkCallbackIdProvider(Provider<PhysicalChunkCallbackId> physicalChunkCallbackIdProvider) {
		this.physicalChunkCallbackIdProvider = physicalChunkCallbackIdProvider;
	}
}
