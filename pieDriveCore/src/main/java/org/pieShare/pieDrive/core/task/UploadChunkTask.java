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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.stream.HashingInputStream;
import org.pieShare.pieDrive.core.stream.LimitingInputStream;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class UploadChunkTask implements IPieTask{
	
	private AdapterCoreService adapterCoreService;
	private AdapterChunk chunk;
	private InputStream stream;

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setChunk(AdapterChunk chunk) {
		this.chunk = chunk;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	@Override
	public void run() {		
		adapterCoreService.getAdapter(chunk.getAdapterId()).upload(chunk, stream);
	}
}
