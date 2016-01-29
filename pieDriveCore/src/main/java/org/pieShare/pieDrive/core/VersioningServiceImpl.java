package org.pieShare.pieDrive.core;


import java.util.List;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


public class VersioningServiceImpl implements VersioningService {

	@Override
	public List<PhysicalChunk> GetNewest(PieRaidFile file) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<PhysicalChunk> GetSpecificVersion(PieRaidFile file, long version) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<PhysicalChunk> GetAll(PieRaidFile file) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
