package org.pieShare.pieDrive.core.database.repository;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.pieShare.pieDrive.core.database.entities.PieRaidFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author richy
 */
public interface PieRaidFileEntityRepository extends JpaRepository<PieRaidFileEntity, String>, PieRaidFileEntityRepositoryCustom {

}
