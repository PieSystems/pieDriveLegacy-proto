/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.pieShare.pieDrive.core.database.api.IDatabaseFactory;
import org.pieShare.pieDrive.core.database.entities.AdapterChunkEntity;
import org.pieShare.pieDrive.core.database.entities.FileEntity;
import org.pieShare.pieDrive.core.database.entities.IBaseEntity;
import org.pieShare.pieDrive.core.database.entities.PhysicalChunkEntity;
import org.pieShare.pieDrive.core.database.entities.PieRaidFileEntity;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;

/**
 *
 * @author richy
 */
public class Database {

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

    public FileEntity findFileById(long id) {
        EntityManager em = databseFactory.getEntityManger(FileEntity.class);
        return em.find(FileEntity.class, id);
    }

    public void remove(IBaseEntity entity) {
        EntityManager em = databseFactory.getEntityManger(entity.getClass());
        em.getTransaction().begin();
        em.remove(entity);
        em.getTransaction().commit();
    }

    public void removePieRadFile(PieRaidFile file) {

        EntityManager em = databseFactory.getEntityManger(PieRaidFileEntity.class);
        em.getTransaction().begin();
        PieRaidFileEntity pieRaidFileEntity = em.find(PieRaidFileEntity.class, file.getFileName());
        if (pieRaidFileEntity != null) {
            em.remove(em.merge(pieRaidFileEntity));
        }
        em.getTransaction().commit();
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

        EntityManager em = databseFactory.getEntityManger(PieRaidFileEntity.class);
        em.getTransaction().begin();
        em.persist(pieRaidFileEntity);
        em.getTransaction().commit();
    }

    public PieRaidFile findPieRaidFileByName(String name) {

        PieRaidFile piePieRaidFile = new PieRaidFile();

        EntityManager em = databseFactory.getEntityManger(PieRaidFileEntity.class);
        PieRaidFileEntity pieRaidFileEntity = em.find(PieRaidFileEntity.class, name);

        return convertPieRaidFileEntityToObject(pieRaidFileEntity);
    }

    public List<PieRaidFile> findAllPieRaidFiles() {
        List<PieRaidFile> pieRaidFiles = new ArrayList<>();

        EntityManager em = databseFactory.getEntityManger(PieRaidFileEntity.class);
        for (PieRaidFileEntity entity : (List<PieRaidFileEntity>) em.createQuery("Select t from " + PieRaidFileEntity.class.getSimpleName() + " t").getResultList()) {

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

        return piePieRaidFile;
    }
    
    public void updateAdaptorChunk(AdapterChunk chunk)
    {
        EntityManager em = databseFactory.getEntityManger(PieRaidFileEntity.class);
        AdapterChunkEntity entity = em.find(AdapterChunkEntity.class, chunk.getUuid());
        
        if(entity == null)
        {
            return;
        }
        
        entity.setAdapterId(chunk.getAdapterId().getId());
        entity.setHash(chunk.getHash());
        
        em.getTransaction().begin();
        em.merge(entity);
        em.getTransaction().commit();
    }
    
    public void updatePhysicalChunk(PhysicalChunk physicalChunk)
    {
        if(physicalChunk.getChunks().isEmpty())
            return;
        
        String id = physicalChunk.getChunks().entrySet().stream().findFirst().get().getValue().getUuid();
        
        EntityManager em = databseFactory.getEntityManger(PieRaidFileEntity.class);
        AdapterChunkEntity entity = em.find(AdapterChunkEntity.class, id);
        PhysicalChunkEntity physicalChunkEntity = entity.getPhysicalChunkEntity();
        
        physicalChunkEntity.setHashValues(physicalChunk.getHash());
    
        em.getTransaction().begin();
        em.merge(physicalChunkEntity);
        em.getTransaction().commit();
    }
    
}
