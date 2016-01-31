/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core.database.repository;

import org.pieShare.pieDrive.core.database.entities.VersionedPieRaidFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VersionedPieRaidFileEntityRepository extends JpaRepository<VersionedPieRaidFileEntity, String>, VersionedPieRaidFileEntityRepositoryCustom{

}
