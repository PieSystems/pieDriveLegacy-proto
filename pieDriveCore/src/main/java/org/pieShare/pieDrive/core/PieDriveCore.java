/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core;

import java.io.File;
import org.pieShare.pieDrive.core.model.PieRaidFile;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public interface PieDriveCore {
	PieRaidFile calculateChunks(File file);
}
