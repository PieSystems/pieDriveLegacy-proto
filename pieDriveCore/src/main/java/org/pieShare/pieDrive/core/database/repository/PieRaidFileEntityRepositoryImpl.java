/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.repository;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.pieShare.pieDrive.core.database.entities.AdapterChunkEntity;
import org.pieShare.pieDrive.core.database.entities.PhysicalChunkEntity;
import org.pieShare.pieDrive.core.database.entities.PieRaidFileEntity;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author richy
 */
public class PieRaidFileEntityRepositoryImpl implements PieRaidFileEntityRepositoryCustom {

    @Autowired
    private PieRaidFileEntityRepository pieRaidFileEntityRepository;

    @Autowired
    private PhysicalChunkEntityRepository physicalChunkEntityRepository;

    @Autowired
    private AdapterChunkEntityRepository adapterChunkEntityRepository;

    @Override
    public PieRaidFileEntity persistPieRaidFile(PieRaidFile pieRaidFile) {

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
            physicalChunkEntity.setUUId(chunk.getUuid());
            physicalChunkEntities.add(physicalChunkEntity);
        }

        pieRaidFileEntity.setChunks(physicalChunkEntities);
        pieRaidFileEntity.setFileName(pieRaidFile.getFileName());
        pieRaidFileEntity.setLastModified(pieRaidFile.getLastModified());
        pieRaidFileEntity.setRelativeFilePath(pieRaidFile.getRelativeFilePath());
        pieRaidFileEntity.setUid(pieRaidFile.getUid());

        return pieRaidFileEntityRepository.save(pieRaidFileEntity);
        /*
        EntityManager em = databseFactory.getEntityManger(PieRaidFileEntity.class);
        em.getTransaction().begin();
        em.persist(pieRaidFileEntity);
        em.getTransaction().commit();*/
    }

    protected PieRaidFile convertPieRaidFileEntityToObject(PieRaidFileEntity pieRaidFileEntity) {
        PieRaidFile piePieRaidFile = new PieRaidFile();

        if (pieRaidFileEntity == null) {
            return null;
        }

        List<PhysicalChunk> physicalChunks = new ArrayList<>();

        for (PhysicalChunkEntity physicalChunkEntity : pieRaidFileEntity.getChunks()) {

            physicalChunkEntity.getChunks().size();
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
            physicalChunk.setUuid(physicalChunkEntity.getUUId());
            physicalChunks.add(physicalChunk);
        }

        piePieRaidFile.setChunks(physicalChunks);
        piePieRaidFile.setFileName(pieRaidFileEntity.getFileName());
        piePieRaidFile.setLastModified(pieRaidFileEntity.getLastModified());
        piePieRaidFile.setRelativeFilePath(pieRaidFileEntity.getRelativeFilePath());
        piePieRaidFile.setUid(pieRaidFileEntity.getUid());

        return piePieRaidFile;
    }

    @Override
    public PieRaidFile findPieRaidFileByUId(String id) {
        PieRaidFileEntity ent = pieRaidFileEntityRepository.findOne(id);
        if (ent != null) {
            return convertPieRaidFileEntityToObject(ent);
        } else {
            return null;
        }
    }

    @Override
    public List<PieRaidFile> findAllPieRaidFiles() {
        List<PieRaidFile> pieRaidFiles = new ArrayList<>();
        List<PieRaidFileEntity> ff = pieRaidFileEntityRepository.findAll();

        for (PieRaidFileEntity entity : ff)//(List<PieRaidFileEntity>) em.createQuery("Select t from " + PieRaidFileEntity.class.getSimpleName() + " t").getResultList()) {
        {
            entity.getChunks().size();
            PieRaidFile file = convertPieRaidFileEntityToObject(entity);
            if (file != null) {
                pieRaidFiles.add(file);
            }
        }
        return pieRaidFiles;
    }

    @Override
    public void removePieRaidFileWithAllChunks(String id) {
        PieRaidFileEntity entity = pieRaidFileEntityRepository.findOne(id);

        for (PhysicalChunkEntity pp : entity.getChunks()) {
            for (AdapterChunkEntity aa : pp.getChunks()) {
                adapterChunkEntityRepository.delete(aa.getUUID());
            }

            physicalChunkEntityRepository.delete(pp.getUUId());
        }
        pieRaidFileEntityRepository.delete(id);
    }
}
