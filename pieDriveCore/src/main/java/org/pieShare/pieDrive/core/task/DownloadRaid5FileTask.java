package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RecursiveAction;
import javax.inject.Provider;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.Raid5Service;
import org.pieShare.pieDrive.core.model.AdapterChunk;
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

	private Provider<DownloadChunkTask> downloadChunkProvider;
	
	@Override
	protected void compute() {
		try {
			File fFile = new File(this.outputDir, raidFile.getFileName());
			file = new RandomAccessFile(fFile, "rw");
			file.setLength(raidFile.getFileSize());
			
			for(PhysicalChunk physicalChunk : raidFile.getChunks()) {
				List<DownloadChunkTask> tasks = new ArrayList<>();
				byte[][] buffer = new byte[raid5Service.getTotalShardCount()][raid5Service.calculateRaidChunkSize(physicalChunk)];
				for(AdapterChunk adapterChunk : physicalChunk.getChunks()) {
					DownloadChunkTask task = downloadChunkProvider.get();
					task.setChunk(adapterChunk);
					task.setStream(StreamFactory.getOutputStream(buffer[adapterChunk.getDataShard()]));
					task.fork();
					tasks.add(task);
				}
				
				for(DownloadChunkTask task : tasks) {
					task.join();
				}
				
				//TODO implement integrity check, recovery and write to file
			}
		} catch(IOException ex) {
			PieLogger.debug(this.getClass(), "Could not download raid file", ex);
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

	public void setDownloadChunkProvider(Provider<DownloadChunkTask> downloadChunkProvider) {
		this.downloadChunkProvider = downloadChunkProvider;
	}
	
}
