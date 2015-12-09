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
import org.pieShare.pieDrive.core.database.repository.BaseEntityRepository;
import org.pieShare.pieDrive.core.database.repository.FolderEntityRepository;
import org.pieShare.pieDrive.core.database.repository.PhysicalChunkEntityRepository;
import org.pieShare.pieDrive.core.database.repository.PieRaidFileEntityRepository;
import org.pieShare.pieDrive.core.database.repository.PieRaidFileRepositoryCustom;
import org.pieShare.pieDrive.core.database.repository.VolumesEntityRepository;
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
    @Autowired
    private VolumesEntityRepository volumesEntityRepository;
    @Autowired
    private FolderEntityRepository folderEntityRepository;
    @Autowired
    private BaseEntityRepository baseEntityRepository;
    
    private IDatabaseFactory databseFactory;

    public void setDatabseFactory(IDatabaseFactory databseFactory) {
        this.databseFactory = databseFactory;
    }

    public void persist(BaseEntity entity) {
        baseEntityRepository.save(entity);
    }

    public void remove(BaseEntity entity) {
        baseEntityRepository.delete(entity);
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
            return volumesEntityRepository.findAll();
            //Query query = em.createQuery("SELECT v from VolumesEntity v");
            //return (Collection<VolumesEntity>) query.getResultList();
        } catch (Exception e) {

            Collection<VolumesEntity> tmp = new ArrayList<VolumesEntity>();
            return tmp;
        }

    }

    public VolumesEntity getVolumeById(long id) {
        return volumesEntityRepository.findOne(id);
    }

    public FolderEntity getFolderById(Long id) {
        return folderEntityRepository.findOne(id);
    }
}
