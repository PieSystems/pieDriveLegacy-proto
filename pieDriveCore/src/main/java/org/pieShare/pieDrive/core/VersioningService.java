/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core;

import java.util.List;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;


public interface VersioningService {
	List<PhysicalChunk> GetNewest(PieRaidFile file);
	
	List<PhysicalChunk> GetSpecificVersion(PieRaidFile file, long version);
	
	List<PhysicalChunk> GetAll(PieRaidFile file);
}
