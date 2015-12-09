/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class DownloadRaidFileTask implements IPieTask {

	private AdapterCoreService adapterCoreService;
	private IExecutorService executorService;
	private File outputDir;

	private PieRaidFile raidFile;
	private RandomAccessFile file;

	private Provider<DownloadChunkTask> downloadChunkProvider;

	@Override
	public void run() {
		try {
			PieLogger.debug(this.getClass(), "Starting download for {}", this.raidFile.getFileName());
			int adapterCounter = -1;

			File ffile = new File(this.outputDir, raidFile.getFileName());
			this.file = new RandomAccessFile(ffile, "rw");
			this.file.setLength(raidFile.getFileSize());
			this.file.setLength(this.raidFile.getFileSize());

			for (PhysicalChunk physicalChunk : raidFile.getChunks()) {
				adapterCounter = this.adapterCoreService.calculateNextAdapter(adapterCounter);

				DownloadChunkTask task = downloadChunkProvider.get();
				task.setAdapterIndex(adapterCounter);
				task.setPhysicalChunk(physicalChunk);
				task.setFile(file);
				executorService.execute(task);
			}
		} catch (IOException ex) {
			Logger.getLogger(DownloadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setExecutorService(IExecutorService executorService) {
		this.executorService = executorService;
	}

	public void setDownloadChunkProvider(Provider<DownloadChunkTask> downloadChunkProvider) {
		this.downloadChunkProvider = downloadChunkProvider;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public void setRaidFile(PieRaidFile raidFile) {
		this.raidFile = raidFile;
	}
}
