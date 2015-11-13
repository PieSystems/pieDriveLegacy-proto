/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.PieDriveCore;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RaidFileTask {

	private PieRaidFile file;
	private PieDriveCore driveCoreService;

	public void run() {
		List<Adaptor> adapters;
		List<PhysicalChunk> chunks = driveCoreService.calculateChunks(file);

		try {
			for (PhysicalChunk chunk : chunks) {

				//todo fix this
				FileInputStream fStr = new FileInputStream(new File("sfsl"));
				
				

			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(RaidFileTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
