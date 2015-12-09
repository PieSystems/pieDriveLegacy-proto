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
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
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
        PieRaidFileEntity pieRaidFileEntity = new PieRaidFileEntity();

        List<PhysicalChunkEntity> physicalChunkEntities = new ArrayList<>();

        for (PhysicalChunk chunk : pieRaidFile.getChunks()) {

            PhysicalChunkEntity physicalChunkEntity = new PhysicalChunkEntity();

            List<AdapterChunkEntity> ace = new ArrayList<>();

            for (AdapterChunk adapterChunk : chunk.getChunks().values()) {
                AdapterChunkEntity adc = new AdapterChunkEntity();
                adc.setAdapterId(adapterChunk.getAdapterId().getId());
                adc.setHash(adapterChunk.getHash());
                adc.setPhysicalChunkEntity(physicalChunkEntity);
                adc.setUUID(adapterChunk.getUuid());

                ace.add(adc);
            }

            physicalChunkEntity.setChunks(ace);
            physicalChunkEntity.setOffset(chunk.getOffset());
            physicalChunkEntity.setSize(chunk.getSize());
            physicalChunkEntity.setPieRaidFileEntity(pieRaidFileEntity);
            physicalChunkEntity.setHashValues(chunk.getHash());
            physicalChunkEntities.add(physicalChunkEntity);
        }

        pieRaidFileEntity.setChunks(physicalChunkEntities);
        pieRaidFileEntity.setFileName(pieRaidFile.getFileName());
        pieRaidFileEntity.setLastModified(pieRaidFile.getLastModified());
        pieRaidFileEntity.setRelativeFilePath(pieRaidFile.getRelativeFilePath());
        pieRaidFileEntity.setUid(pieRaidFile.getUid());

        pieRaidFileEntityRepository.save(pieRaidFileEntity);
        /*
        EntityManager em = databseFactory.getEntityManger(PieRaidFileEntity.class);
        em.getTransaction().begin();
        em.persist(pieRaidFileEntity);
        em.getTransaction().commit();*/
    }

    public PieRaidFile findPieRaidFileById(String name) {
        return convertPieRaidFileEntityToObject(pieRaidFileEntityRepository.findOne(name));
    }

    public List<PieRaidFile> findAllPieRaidFiles() {
        List<PieRaidFile> pieRaidFiles = new ArrayList<>();
        for (PieRaidFileEntity entity : pieRaidFileEntityRepository.findAll())//(List<PieRaidFileEntity>) em.createQuery("Select t from " + PieRaidFileEntity.class.getSimpleName() + " t").getResultList()) {
        {
            PieRaidFile file = convertPieRaidFileEntityToObject(entity);
            if (file != null) {
                pieRaidFiles.add(file);
            }
        }
        return pieRaidFiles;
    }

    private PieRaidFile convertPieRaidFileEntityToObject(PieRaidFileEntity pieRaidFileEntity) {
        PieRaidFile piePieRaidFile = new PieRaidFile();

        if (pieRaidFileEntity == null) {
            return null;
        }

        List<PhysicalChunk> physicalChunks = new ArrayList<>();

        for (PhysicalChunkEntity physicalChunkEntity : pieRaidFileEntity.getChunks()) {
            PhysicalChunk physicalChunk = new PhysicalChunk();

            for (AdapterChunkEntity adapterChunkEntity : physicalChunkEntity.getChunks()) {
                AdapterChunk adapterChunk = new AdapterChunk();
                AdapterId id = new AdapterId();
                id.setId(adapterChunkEntity.getAdapterId());
                adapterChunk.setAdapterId(id);
                adapterChunk.setHash(adapterChunkEntity.getHash());
                adapterChunk.setUuid(adapterChunkEntity.getUUID());

                physicalChunk.addAdapterChunk(adapterChunk);
            }

            physicalChunk.setOffset(physicalChunkEntity.getOffset());
            physicalChunk.setSize(physicalChunkEntity.getSize());
            physicalChunk.setHash(physicalChunkEntity.getHashValues());

            physicalChunks.add(physicalChunk);
        }

        piePieRaidFile.setChunks(physicalChunks);
        piePieRaidFile.setFileName(pieRaidFileEntity.getFileName());
        piePieRaidFile.setLastModified(pieRaidFileEntity.getLastModified());
        piePieRaidFile.setRelativeFilePath(pieRaidFileEntity.getRelativeFilePath());
        piePieRaidFile.setUid(pieRaidFileEntity.getUid());

        return piePieRaidFile;
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
