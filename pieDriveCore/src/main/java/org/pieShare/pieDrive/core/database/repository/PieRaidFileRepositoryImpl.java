/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.repository;

import javax.persistence.EntityManager;
import org.pieShare.pieDrive.core.database.entities.AdapterChunkEntity;
import org.pieShare.pieDrive.core.database.entities.PhysicalChunkEntity;
import org.pieShare.pieDrive.core.database.entities.PieRaidFileEntity;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author richy
 */
public class PieRaidFileRepositoryImpl implements PieRaidFileRepositoryCustom{

    @Autowired
    private PieRaidFileEntityRepository pieRaidFileEntityRepository;
    
    @Override
    public void updateAdaptorChunk(AdapterChunk chunk) {
        /*EntityManager em = databseFactory.getEntityManger(PieRaidFileEntity.class);
        AdapterChunkEntity entity = em.find(AdapterChunkEntity.class, chunk.getUuid());
        pieRaidFileEntityRepository.fin

        if (entity == null) {
            return;
        }

        entity.setAdapterId(chunk.getAdapterId().getId());
        entity.setHash(chunk.getHash());

        em.getTransaction().begin();
        em.merge(entity);
        em.getTransaction().commit();*/
    }

    @Override
    public void updatePhysicalChunk(PhysicalChunk physicalChunk) {
     /*  if (physicalChunk.getChunks().isEmpty()) {
            return;
        }

        String id = physicalChunk.getChunks().entrySet().stream().findFirst().get().getValue().getUuid();

        EntityManager em = databseFactory.getEntityManger(PieRaidFileEntity.class);
        AdapterChunkEntity entity = em.find(AdapterChunkEntity.class, id);
        PhysicalChunkEntity physicalChunkEntity = entity.getPhysicalChunkEntity();

        physicalChunkEntity.setHashValues(physicalChunk.getHash());

        em.getTransaction().begin();
        em.merge(physicalChunkEntity);
        em.getTransaction().commit();*/
    }
    
}
