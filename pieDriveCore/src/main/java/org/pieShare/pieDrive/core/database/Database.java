/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.pieShare.pieDrive.core.database.entities.*;
import org.pieShare.pieDrive.core.database.repository.AdapterChunkEntityRepository;
import org.pieShare.pieDrive.core.database.repository.FolderEntityRepository;
import org.pieShare.pieDrive.core.database.repository.PhysicalChunkEntityRepository;
import org.pieShare.pieDrive.core.database.repository.PieRaidFileEntityRepository;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.model.ui.PieFolder;
import org.pieShare.pieDrive.core.model.ui.Volume;
import org.springframework.beans.factory.annotation.Autowired;
import org.pieShare.pieDrive.core.database.repository.VolumeEntityRepository;

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
    private VolumeEntityRepository volumesEntityRepository;
    @Autowired
    private FolderEntityRepository folderEntityRepository;

    public void removePieRaidFile(PieRaidFile file) {
        pieRaidFileEntityRepository.removePieRaidFileWithAllChunks(file.getUid());
    }

    public void persistPieRaidFile(PieRaidFile pieRaidFile) {
        pieRaidFileEntityRepository.persistPieRaidFile(pieRaidFile);
    }

    public PieRaidFile findPieRaidFileById(String id) {
        return pieRaidFileEntityRepository.findPieRaidFileByUId(id);
    }

    public List<PieRaidFile> findAllPieRaidFiles() {
        return pieRaidFileEntityRepository.findAllPieRaidFiles();
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

    public Collection<Volume> getAllVolumes() {
        return volumesEntityRepository.getAllVolumes();
    }

    public Volume getVolumeById(String id) {
        return volumesEntityRepository.getVolumeByUId(id);
    }

    public PieFolder getFolderById(String id) {
        return folderEntityRepository.findFolderByUid(id);
    }

    public List<PieFolder> findAllFolders() {
        return folderEntityRepository.findAllFolders();
    }

    public void persistVolume(Volume volume) {
        volumesEntityRepository.persistVolume(volume);
    }

    public void persistFolder(PieFolder folder) {
        folderEntityRepository.persistFolder(folder);
    }
}
