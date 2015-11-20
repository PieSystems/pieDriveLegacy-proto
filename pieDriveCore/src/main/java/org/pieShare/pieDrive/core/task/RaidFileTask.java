/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCore;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.Factory;
import org.pieShare.pieDrive.core.FactoryException;
import org.pieShare.pieDrive.core.stream.HashingDoneCallback;
import org.pieShare.pieDrive.core.stream.HashingInputStream;
import org.pieShare.pieDrive.core.stream.LimitingInputStream;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RaidFileTask implements IPieTask, HashingDoneCallback {

	private File file;
	private int reportedBack = 0;
	
	private IExecutorService executorService;
	private PieDriveCore driveCoreService;
	private AdapterCoreService adapterCoreService;
	private Provider<AdapterChunk> adapterChunkProvider;
	private Provider<UploadChunkTask> uploadChunkTaskProvider;
	private Factory<FileInputStream> fileInputStreamFactory;
	private Factory<HashingInputStream> hashingInputStreamFactory;
	private Factory<LimitingInputStream> limitingInputStreamFactory;

	public void run() {
		PieRaidFile raidedFile = driveCoreService.calculateChunks(file);

		for (PhysicalChunk physicalChunk : raidedFile.getChunks()) {
			for (AdapterId id : adapterCoreService.getAdaptersKey()) {
				FileInputStream fStr = null;
				try {
					AdapterChunk chunk = adapterChunkProvider.get();
					chunk.setAdapterId(id);
					chunk.setUuid(UUID.randomUUID().toString());
					
					physicalChunk.addAdapterChunk(chunk);
					
					fStr = fileInputStreamFactory.get(file);
					fStr.skip(physicalChunk.getOffset());
					LimitingInputStream lStr = limitingInputStreamFactory.get(fStr, physicalChunk.getSize());
					HashingInputStream hStr = hashingInputStreamFactory.get(lStr, this);
					
					UploadChunkTask task = uploadChunkTaskProvider.get();
					task.setChunk(chunk);
					task.setStream(hStr);
					
					this.executorService.execute(task);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(RaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
				} catch (IOException ex) {
					Logger.getLogger(RaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
				} catch (FactoryException ex) {
					Logger.getLogger(RaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
				} finally {
					try {
						fStr.close();
					} catch (IOException ex) {
						Logger.getLogger(RaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		}

		while(this.reportedBack < 3) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Logger.getLogger(RaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		//todo: save this to the DB
	}

	@Override
	public void hashingDone(AdapterId adapterId) {
		this.reportedBack++;
	}
}
