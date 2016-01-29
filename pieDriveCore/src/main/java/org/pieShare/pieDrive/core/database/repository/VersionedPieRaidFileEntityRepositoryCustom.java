/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core.database.repository;

import java.util.List;
import org.pieShare.pieDrive.core.database.entities.VersionedPieRaidFileEntity;
import org.pieShare.pieDrive.core.model.VersionedPieRaidFile;


public interface VersionedPieRaidFileEntityRepositoryCustom {
	void persistVersionedPieRaidFile(VersionedPieRaidFile pieRaidFile);

    VersionedPieRaidFile findVersionedPieRaidFileByUId(String id);

    List<VersionedPieRaidFile> findAllVersionedPieRaidFiles();
    
    void removeVersionedPieRaidFileWithAllChunks(String id);
}
