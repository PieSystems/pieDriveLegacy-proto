/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.repository;

import org.pieShare.pieDrive.core.database.entities.PhysicalChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author richy
 */
public interface PhysicalChunkEntityRepository  extends JpaRepository<PhysicalChunkEntity, String>{
    
}
