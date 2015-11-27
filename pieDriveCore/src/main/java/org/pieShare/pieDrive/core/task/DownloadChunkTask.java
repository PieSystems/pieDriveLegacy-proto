/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.OutputStream;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class DownloadChunkTask  implements IPieTask {
	
	private AdapterChunk chunk;
	private AdapterCoreService adapterCoreService;
	private OutputStream stream;

	public void setChunk(AdapterChunk chunk) {
		this.chunk = chunk;
	}

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setStream(OutputStream stream) {
		this.stream = stream;
	}

	@Override
	public void run() {
		adapterCoreService.getAdapter(chunk.getAdapterId()).download(chunk, stream);
	}
	
}
