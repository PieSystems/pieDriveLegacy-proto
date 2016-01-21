/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class UploadRaidFileTask implements IPieTask {

	private File file;
	private PieRaidFile raidedFile;

	private IExecutorService executorService;
	private PieDriveCore driveCoreService;
	private AdapterCoreService adapterCoreService;
	//todo: will need abstraction when merging into PieShare
	private Database database;

	private Provider<AdapterChunk> adapterChunkProvider;
	private Provider<UploadChunkTask> uploadChunkTaskProvider;

	public void run() {
		try {
			PieLogger.debug(this.getClass(), "Starting file upload for {}", this.file.getName());
			
			RandomAccessFile rFile = new RandomAccessFile(file, "r");
			
			//we need to iterate twice so we can guarante that the object 
			//will be completely initizilised before we start working on it
			for (PhysicalChunk physicalChunk : raidedFile.getChunks()) {
				for (AdapterId id : adapterCoreService.getAdaptersKey()) {
					AdapterChunk chunk = adapterChunkProvider.get();
					chunk.setAdapterId(id);
					chunk.setUuid(UUID.randomUUID().toString());
					chunk.setSize(physicalChunk.getSize());
					physicalChunk.addAdapterChunk(chunk);
				}
			}
			
			this.database.persistPieRaidFile(raidedFile);

			for (PhysicalChunk physicalChunk : raidedFile.getChunks()) {
				for (AdapterChunk chunk: physicalChunk.getChunks()) {
					UploadChunkTask task = uploadChunkTaskProvider.get();
					task.setChunk(chunk);
					task.setFile(rFile);
					task.setPhysicalChunk(physicalChunk);

					this.executorService.execute(task);
				}
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(UploadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setRaidedFile(PieRaidFile raidedFile) {
		this.raidedFile = raidedFile;
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

	public void setAdapterChunkProvider(Provider<AdapterChunk> adapterChunkProvider) {
		this.adapterChunkProvider = adapterChunkProvider;
	}

	public void setUploadChunkTaskProvider(Provider<UploadChunkTask> uploadChunkTaskProvider) {
		this.uploadChunkTaskProvider = uploadChunkTaskProvider;
	}
}
