/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.pieShare.pieDrive.core.database.api.IDatabaseFactory;
import org.pieShare.pieDrive.core.database.entities.*;
import org.pieShare.pieDrive.core.database.repository.AdapterChunkEntityRepository;
import org.pieShare.pieDrive.core.database.repository.PhysicalChunkEntityRepository;
import org.pieShare.pieDrive.core.database.repository.PieRaidFileEntityRepository;
import org.pieShare.pieDrive.core.database.repository.PieRaidFileRepositoryCustom;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author richy
 */
public class Database {

    @Autowired
    private PieRaidFileEntityRepository pieRaidFileEntityRepository;
    @Autowired
    private AdapterChunkEntityRepository adapterChunkEntityRepository;
    @Autowired
    private PhysicalChunkEntityRepository physicalChunkEntityRepository;
    @Autowired
    private PieRaidFileRepositoryCustom pieRaidFileRepositoryCustom;

    private IDatabaseFactory databseFactory;

    public void setDatabseFactory(IDatabaseFactory databseFactory) {
        this.databseFactory = databseFactory;
    }

    public void persist(IBaseEntity entity) {
        EntityManager em = databseFactory.getEntityManger(entity.getClass());
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
    }

    public void remove(IBaseEntity entity) {
        EntityManager em = databseFactory.getEntityManger(entity.getClass());
        em.getTransaction().begin();
        em.remove(entity);
        em.getTransaction().commit();
    }

    public void removePieRaidFile(PieRaidFile file) {
        pieRaidFileEntityRepository.delete(file.getUid());
    }

    public void persistPieRaidFile(PieRaidFile pieRaidFile) {
      pieRaidFileRepositoryCustom.persistPieRaidFile(pieRaidFile);
    }

    public PieRaidFile findPieRaidFileById(String id) {
       return pieRaidFileRepositoryCustom.findPieRaidFileById(id);
    }

    public List<PieRaidFile> findAllPieRaidFiles() {
       return pieRaidFileRepositoryCustom.findAllPieRaidFiles();
    }

    public void updateAdaptorChunk(AdapterChunk chunk) {
        AdapterChunkEntity entity = adapterChunkEntityRepository.findOne(chunk.getUuid());

        if (entity == null) {
            return;
        }

        entity.setAdapterId(chunk.getAdapterId().getId());
        entity.setHash(chunk.getHash());

        adapterChunkEntityRepository.save(entity);
    }

    public void updatePhysicalChunk(PhysicalChunk physicalChunk) {
        if (physicalChunk.getChunks().isEmpty()) {
            return;
        }
        String id = physicalChunk.getChunks().entrySet().stream().findFirst().get().getValue().getUuid();
        AdapterChunkEntity entity = adapterChunkEntityRepository.findOne(id);

        PhysicalChunkEntity physicalChunkEntity = entity.getPhysicalChunkEntity();

        physicalChunkEntity.setHashValues(physicalChunk.getHash());
        physicalChunkEntityRepository.save(physicalChunkEntity);
    }

    public Collection<VolumesEntity> getAllVolumes() {

        //TODO find solution for empty objectDB problem
        try {
            EntityManager em = databseFactory.getEntityManger(VolumesEntity.class);

            Query query = em.createQuery("SELECT v from VolumesEntity v");
            return (Collection<VolumesEntity>) query.getResultList();
        } catch (Exception e) {

            Collection<VolumesEntity> tmp = new ArrayList<VolumesEntity>();
            return tmp;
        }

    }

    public VolumesEntity getVolumeById(long id) {
        EntityManager em = databseFactory.getEntityManger(VolumesEntity.class);
        return em.find(VolumesEntity.class, id);
    }

    public FolderEntity getFolderById(Long id) {
        EntityManager em = databseFactory.getEntityManger(FolderEntity.class);
        return em.find(FolderEntity.class, id);
    }
}
