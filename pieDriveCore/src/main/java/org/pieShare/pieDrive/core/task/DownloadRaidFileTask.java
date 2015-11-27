/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.stream.HashingOutputStream;
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
public class DownloadRaidFileTask implements IPieTask, HashingDoneCallback {

	private AdapterCoreService adapterCoreService;
	private IExecutorService executorService;
	private File file;

	private Provider<StreamCallbackHelper> streamCallbackHelperProvider;
	private Provider<DownloadChunkTask> downloadChunkProvider;
	private Provider<PhysicalChunkCallbackId> physicalChunkCallbackIdProvider;

	@Override
	public void run() {
		//todo: maybe use DB to retrieve object
		PieRaidFile raidFile = null;
		ArrayList<AdapterId> adatperIds = new ArrayList<>(adapterCoreService.getAdaptersKey());
		int adapterCounter = -1;
		
		//todo: create File for output stream

		for (PhysicalChunk physicalChunk : raidFile.getChunks()) {
			adapterCounter = (adapterCounter++) % adatperIds.size();
			AdapterId next = adatperIds.get(adapterCounter);
			AdapterChunk chunk = physicalChunk.getChunks().remove(next);
			
			this.triggerTask(physicalChunk, chunk);
		}
	}

	private void triggerTask(PhysicalChunk physicalChunk, AdapterChunk chunk) {
		HashingOutputStream hStr = null;
		try {
			FileOutputStream fileStream = StreamFactory.getFileOutputStream(this.file);
			//todo: limitintOutputStream
			StreamCallbackHelper cb = this.streamCallbackHelperProvider.get();
			cb.setCallback(this);
			PhysicalChunkCallbackId id = this.physicalChunkCallbackIdProvider.get();
			id.setChunk(chunk);
			id.setPhysicalChunk(physicalChunk);
			cb.setCallbackId(id);
			hStr = StreamFactory.getHashingOutputStream(fileStream, MessageDigest.getInstance("MD5"), cb);
			DownloadChunkTask task = downloadChunkProvider.get();
			task.setChunk(chunk);
			task.setStream(hStr);
			executorService.execute(task);
		} catch (NoSuchAlgorithmException | FileNotFoundException ex) {
			Logger.getLogger(DownloadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				hStr.close();
			} catch (IOException ex) {
				Logger.getLogger(DownloadRaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void hashingDone(ICallbackId id, byte[] hash) {
		PhysicalChunkCallbackId pId = (PhysicalChunkCallbackId) id;

		//todo compare hash with pId.getChunk().getHash()
		//if equals return
		//else
		if (pId.getPhysicalChunk().getChunks().size() > 0) {
			AdapterId chunkId = pId.getPhysicalChunk().getChunks().keySet().iterator().next();
			triggerTask(pId.getPhysicalChunk(), pId.getPhysicalChunk().getChunks().remove(chunkId));
		}
		//handle that no a single chunk was okay
	}

}
