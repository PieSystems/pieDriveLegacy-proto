/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.repository;

import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.PhysicalChunk;

/**
 *
 * @author richy
 */
public interface PieRaidFileRepositoryCustom {

    void updateAdaptorChunk(AdapterChunk chunk);

    void updatePhysicalChunk(PhysicalChunk physicalChunk);
}
