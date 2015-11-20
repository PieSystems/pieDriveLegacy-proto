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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.stream.HashingInputStream;
import org.pieShare.pieDrive.core.stream.LimitingInputStream;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class UploadChunkTask {
	
	private AdapterCoreService adapterCoreService;
	private AdapterChunk chunk;

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setChunk(AdapterChunk chunk) {
		this.chunk = chunk;
	}

	public void run() {
		//todo: move to core or else where
		long limit = 5000000;
		
		FileInputStream fStr = null;
		try {
			fStr = new FileInputStream(new File("sfsl"));
			LimitingInputStream lStr = new LimitingInputStream(fStr, limit);
			HashingInputStream hStr = new HashingInputStream(lStr);
			
			adapterCoreService.getAdapter(chunk.getAdapterId()).upload(chunk, hStr);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(UploadChunkTask.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fStr.close();
			} catch (IOException ex) {
				Logger.getLogger(UploadChunkTask.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
