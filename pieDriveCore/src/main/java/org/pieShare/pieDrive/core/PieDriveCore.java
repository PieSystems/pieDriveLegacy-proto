/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core;

import java.io.File;
import java.util.List;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public interface PieDriveCore {
	PieRaidFile calculateRaidFile(File file);
	List<PhysicalChunk> calculateChunks(File file);
}
