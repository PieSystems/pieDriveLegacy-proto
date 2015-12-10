/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.util.HashMap;
import java.util.Iterator;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class DeleteRaidFileTask implements IPieTask {
	private PieRaidFile pieRaidFile;
	private AdapterCoreService adapterCoreService;
	private Database database;
		
	public void setPieRaidFile(PieRaidFile pieRaidFile){
		this.pieRaidFile = pieRaidFile;
	}

	public void run() {
		PieLogger.trace(DeleteRaidFileTask.class, "DeleteRaidFileTask on file {} started", pieRaidFile.toString());
		
		//ToDo: Converting to array becuase i want to delete during iteration of the list. 
		//This is slow. .. Maybe there is something better.
		for (PhysicalChunk physicalChunk : pieRaidFile.getChunks().toArray(new PhysicalChunk[pieRaidFile.getChunks().size()])) {
			for(Iterator<HashMap.Entry<AdapterId,AdapterChunk>> it = physicalChunk.getChunks().entrySet().iterator(); it.hasNext(); ){
				try {
					HashMap.Entry<AdapterId,AdapterChunk> chunk = it.next();
					PieLogger.debug(DeleteRaidFileTask.class, "Starting chunk delete for {} with adapter {}", chunk.getValue().getUuid(), chunk.getKey());
					adapterCoreService.getAdapter(chunk.getKey()).delete(chunk.getValue());
					it.remove();
				} catch (AdaptorException ex) {
					PieLogger.error(DeleteRaidFileTask.class, "AdaptorException on deletion of AdapterChunk: {}", ex);
				}
			}
			
			
			if (physicalChunk.getChunks().isEmpty()) {
				pieRaidFile.getChunks().remove(physicalChunk);
			}
		}

		if (!pieRaidFile.getChunks().isEmpty()) {
			PieLogger.error(DeleteRaidFileTask.class, "Not all files could be deleted.");
			throw new Error("Not all files could be deleted.");
		}
		
		database.removePieRaidFile(pieRaidFile);
	}

	public void setDatabase(Database database) {
		this.database = database;
	}
	
	public void setAdapterCoreService(AdapterCoreService coreService){
		this.adapterCoreService = coreService;
	}
}
