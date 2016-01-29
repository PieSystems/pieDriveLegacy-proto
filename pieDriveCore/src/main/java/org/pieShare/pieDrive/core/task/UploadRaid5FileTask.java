package org.pieShare.pieDrive.core.task;

import com.backblaze.erasure.ReedSolomon;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCore;
import org.pieShare.pieDrive.core.Raid5Service;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.stream.BoundedInputStream;
import org.pieShare.pieDrive.core.stream.NioInputStream;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

public class UploadRaid5FileTask implements IPieTask {
	private final int PARITY_SHARD_COUNT = 1;
	private File file;
	private PieRaidFile raidedFile;

	private IExecutorService executorService;
	private PieDriveCore driveCoreService;
	private AdapterCoreService adapterCoreService;
	private Raid5Service raid5Service;
	//todo: will need abstraction when merging into PieShare
	private Database database;

	private Provider<AdapterChunk> adapterChunkProvider;
	private Provider<UploadBufferChunkTask> uploadBufferChunkTaskProvider;

	@Override
	public void run() {
		try {
			PieLogger.debug(this.getClass(), "Starting file upload for {}", this.file.getName());
			
			RandomAccessFile rFile = new RandomAccessFile(file, "r");
			
			//we need to iterate twice so we can guarante that the object 
			//will be completely initizilised before we start working on it
			for (PhysicalChunk physicalChunk : raidedFile.getChunks()) {
				for (AdapterId id : adapterCoreService.getAdaptersKey()) {
					//TODO what if chunk size is not dividable by 2?
					long raidChunkSize = raid5Service.calculateRaidChunkSize(physicalChunk);
					AdapterChunk chunk = adapterChunkProvider.get();
					chunk.setAdapterId(id);
					chunk.setUuid(UUID.randomUUID().toString());
					chunk.setSize(raidChunkSize);
					physicalChunk.addAdapterChunk(chunk);
				}
			}
			
			this.database.persistPieRaidFile(raidedFile);
			
			for (PhysicalChunk physicalChunk : raidedFile.getChunks()) {
				//TODO calculate raid5 chunks
				NioInputStream nioStream = StreamFactory.getNioInputStream(rFile, physicalChunk.getOffset());
				byte[][] raidBuffers = raid5Service.generateRaidShards(nioStream, physicalChunk);
				
				for (AdapterChunk chunk: physicalChunk.getChunks()) {
					UploadBufferChunkTask task = uploadBufferChunkTaskProvider.get();
					task.setChunk(chunk);
					//task.setBuffer(rFile);
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
	
	public void setRaid5Service(Raid5Service raid5Service) {
		this.raid5Service = raid5Service;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public void setAdapterChunkProvider(Provider<AdapterChunk> adapterChunkProvider) {
		this.adapterChunkProvider = adapterChunkProvider;
	}

	public void setUploadBufferChunkTaskProvider(Provider<UploadBufferChunkTask> uploadBufferChunkTaskProvider) {
		this.uploadBufferChunkTaskProvider = uploadBufferChunkTaskProvider;
	}
}
