/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.repository;

import java.util.Collection;
import org.pieShare.pieDrive.core.model.ui.Volume;

/**
 *
 * @author richy
 */
public interface VolumeEntityRepositoryCustom {

    void persistVolume(Volume volume);

    Collection<Volume> getAllVolumes();

    Volume getVolumeByUId(String id);
}
