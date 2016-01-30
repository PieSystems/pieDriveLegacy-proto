package org.pieShare.pieDrive.core.task;

import com.backblaze.erasure.ReedSolomon;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.RecursiveAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCore;
import org.pieShare.pieDrive.core.Raid5Service;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.stream.BoundedInputStream;
import org.pieShare.pieDrive.core.stream.NioInputStream;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

public class UploadRaid5FileTask extends RecursiveAction {

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
	//private Provider<UploadBufferChunkTask> uploadBufferChunkTaskProvider;
	private Provider<UploadChunkTask> uploadChunkTaskProvider;

	private Iterator<PhysicalChunk> physicalChunksIterator;
	private RandomAccessFile rFile;
	private ListeningExecutorService listeningExecutorService;

	@Override
	public void compute() {
		try {
			PieLogger.debug(this.getClass(), "Starting file upload for {}", this.file.getName());

			rFile = new RandomAccessFile(file, "r");

			//we need to iterate twice so we can guarante that the object 
			//will be completely initizilised before we start working on it
			
			//we initialize randomly the shared
			int shard = this.adapterCoreService
					.calculateNextAdapter(new Random().nextInt(adapterCoreService.getAdaptersKey().size()));
			
			for (PhysicalChunk physicalChunk : raidedFile.getChunks()) {
				//attention this works only due to the fact that we iterate over the keys!
				for (AdapterId id : adapterCoreService.getAdaptersKey()) {
					//TODO what if chunk size is not dividable by 2?
					long raidChunkSize = raid5Service.calculateRaidChunkSize(physicalChunk);
					AdapterChunk chunk = adapterChunkProvider.get();
					chunk.setAdapterId(id);
					chunk.setUuid(UUID.randomUUID().toString());
					chunk.setSize(raidChunkSize);
					chunk.setDataShard(shard);
					physicalChunk.addAdapterChunk(chunk);
					
					//we here do make sure to take the next shared for this physical chunk
					shard = this.adapterCoreService.calculateNextAdapter(shard);
				}
				//and here we make sure that not all parities are stored on the same adapter
				shard = this.adapterCoreService.calculateNextAdapter(shard);
			}

			this.database.persistPieRaidFile(raidedFile);

			this.physicalChunksIterator = this.raidedFile.getChunks().iterator();

//			if (!raidedFile.getChunks().iterator().hasNext()) {
//				return;
//			}
			//PhysicalChunk physicalChunk = this.physicalChunksIterator.next();
			
			for (PhysicalChunk physicalChunk : raidedFile.getChunks()) {
				//TODO calculate raid5 chunks
				NioInputStream nioStream = StreamFactory.getNioInputStream(rFile, physicalChunk.getOffset());
				BufferedInputStream bufferedStream = StreamFactory.getBufferedInputStream(nioStream, 65536); //64kB
				byte[][] raidBuffers = raid5Service.generateRaidShards(bufferedStream, physicalChunk);
				
				//List<ListenableFuture<Void>> futures = new ArrayList<>();
				List<UploadChunkTask> tasks = new ArrayList<>();

				for (AdapterChunk chunk : physicalChunk.getChunks()) {
					//UploadBufferChunkTask task = uploadBufferChunkTaskProvider.get();
					UploadChunkTask task = uploadChunkTaskProvider.get();
					task.setChunk(chunk);
					//task.setBufer(raidBuffers[chunk.getDataShard()]);
					task.setIn(StreamFactory.getInputStream(raidBuffers[chunk.getDataShard()]));
					
					task.fork();
					tasks.add(task);
				}
				
				for(UploadChunkTask task: tasks) {
					task.join();
					
					if(task.isCompletedNormally()) {
						task.getChunk().setState(ChunkHealthState.Healthy);
					} else {
						task.getChunk().setState(ChunkHealthState.Broken);
					}
				}
				
				synchronized(this.database) {
					for(AdapterChunk chunk : physicalChunk.getChunks()) {
						this.database.updateAdaptorChunk(chunk);	
					}
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

	public void setUploadChunkTaskProvider(Provider<UploadChunkTask> uploadChunkTaskProvider) {
		this.uploadChunkTaskProvider = uploadChunkTaskProvider;
	}
}
