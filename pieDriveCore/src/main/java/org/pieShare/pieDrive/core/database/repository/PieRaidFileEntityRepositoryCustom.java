/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.repository;

import java.util.List;
import org.pieShare.pieDrive.core.database.entities.PieRaidFileEntity;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;

/**
 *
 * @author richy
 */
public interface PieRaidFileEntityRepositoryCustom {

    PieRaidFileEntity persistPieRaidFile(PieRaidFile pieRaidFile);

    PieRaidFile findPieRaidFileByUId(String id);

    List<PieRaidFile> findAllPieRaidFiles();
    
    void removePieRaidFileWithAllChunks(String id);
}
