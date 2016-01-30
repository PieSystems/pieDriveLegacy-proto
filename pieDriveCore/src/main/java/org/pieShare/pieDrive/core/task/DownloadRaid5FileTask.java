package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RecursiveAction;
import javax.inject.Provider;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.Raid5Service;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

public class DownloadRaid5FileTask extends RecursiveAction {

	private AdapterCoreService adapterCoreService;
	private Raid5Service raid5Service;
	private File outputDir;

	private PieRaidFile raidFile;
	private RandomAccessFile file;

	private Provider<DownloadRaid5ChunkTask> downloadRaid5ChunkProvider;
	private Provider<UploadChunkTask> uploadChunkProvider;

	@Override
	protected void compute() {
		try {
			File fFile = new File(this.outputDir, raidFile.getFileName());
			file = new RandomAccessFile(fFile, "rw");
			file.setLength(raidFile.getFileSize());

			for (PhysicalChunk physicalChunk : raidFile.getChunks()) {
				List<DownloadRaid5ChunkTask> tasks = new ArrayList<>();
				byte[][] buffer = new byte[raid5Service.getTotalShardCount()][raid5Service.calculateRaidChunkSize(physicalChunk)];
				for (AdapterChunk adapterChunk : physicalChunk.getChunks()) {
					DownloadRaid5ChunkTask task = downloadRaid5ChunkProvider.get();
					task.setChunk(adapterChunk);
					task.setOut(StreamFactory.getOutputStream(buffer[adapterChunk.getDataShard()]));
					task.fork();
					tasks.add(task);
				}

				boolean healthy = true;
				for (DownloadRaid5ChunkTask task : tasks) {
					task.join();
					if (task.getChunk().getState() != ChunkHealthState.Healthy) {
						healthy = false;
					}
				}

				if (!healthy) {
					if (!raid5Service.repairRaidShards(buffer, physicalChunk)) {
						PieLogger.error(this.getClass(), "Raid file is corrupted beyond repair");
						return;
					}

					List<UploadChunkTask> recoveryTasks = new ArrayList<>();
					for (AdapterChunk adapterChunk : physicalChunk.getChunks()) {
						if (adapterChunk.getState() != ChunkHealthState.Healthy) {
							UploadChunkTask task = uploadChunkProvider.get();
							task.setChunk(adapterChunk);
							task.setIn(StreamFactory.getInputStream(buffer[adapterChunk.getDataShard()]));
							task.fork();
							recoveryTasks.add(task);
						}
					}

					for (UploadChunkTask task : recoveryTasks) {
						task.join();
					}
				}

				OutputStream outputStream = StreamFactory.getOutputStream(file, physicalChunk);
				for (int i = 0; i < raid5Service.getDataShardCount(); i++) {
					outputStream.write(buffer[i]);
				}
			}
		} catch (IOException ex) {
			PieLogger.error(this.getClass(), "Could not download raid file", ex);
		}
	}

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setRaid5Service(Raid5Service raid5Service) {
		this.raid5Service = raid5Service;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public void setRaidFile(PieRaidFile raidFile) {
		this.raidFile = raidFile;
	}

	public void setDownloadRaid5ChunkProvider(Provider<DownloadRaid5ChunkTask> downloadRaid5ChunkProvider) {
		this.downloadRaid5ChunkProvider = downloadRaid5ChunkProvider;
	}

	public void setUploadChunkProvider(Provider<UploadChunkTask> uploadChunkProvider) {
		this.uploadChunkProvider = uploadChunkProvider;
	}
}
