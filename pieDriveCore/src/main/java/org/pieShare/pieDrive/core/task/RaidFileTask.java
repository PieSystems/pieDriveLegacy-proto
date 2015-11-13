/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCore;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.stream.HashingInputStream;
import org.pieShare.pieDrive.core.stream.LimitingInputStream;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RaidFileTask {

	private PieRaidFile file;
	private PieDriveCore driveCoreService;
	private AdapterCoreService adapterCoreService;

	public void run() {
		List<Adaptor> adapters = adapterCoreService.getAdapters();
		List<PhysicalChunk> chunks = driveCoreService.calculateChunks(file);
		
		//todo: move to core or else where
		long limit = 5000000;

		try {
			for (PhysicalChunk chunk : chunks) {
				for(Adaptor adapter: adapters) {
					//todo fix this
					FileInputStream fStr = new FileInputStream(new File("sfsl"));
					LimitingInputStream lStr = new LimitingInputStream(fStr, limit);
					HashingInputStream hStr = new HashingInputStream(lStr);
				}
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(RaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
