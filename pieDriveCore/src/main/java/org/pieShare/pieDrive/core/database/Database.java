/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database;

import java.util.Collection;
import java.util.List;

import org.pieShare.pieDrive.core.database.entities.*;
import org.pieShare.pieDrive.core.database.repository.AdapterChunkEntityRepository;
import org.pieShare.pieDrive.core.database.repository.PhysicalChunkEntityRepository;
import org.pieShare.pieDrive.core.database.repository.PieRaidFileEntityRepository;
import org.pieShare.pieDrive.core.database.repository.VersionedPieRaidFileEntityRepository;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.model.ui.Volume;
import org.springframework.beans.factory.annotation.Autowired;
import org.pieShare.pieDrive.core.database.repository.VolumeEntityRepository;
import org.pieShare.pieDrive.core.model.VersionedPieRaidFile;

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
	private VersionedPieRaidFileEntityRepository versionedPieRaidFileEntityRepository;

    public void removePieRaidFile(PieRaidFile file) {
        pieRaidFileEntityRepository.delete(file.getUid());
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
        String id = physicalChunk.getChunks().stream().findFirst().get().getUuid();
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

    public void persistVolume(Volume volume) {
        volumesEntityRepository.persistVolume(volume);
    }
	
	public void removeVersionedPieRaidFile(VersionedPieRaidFile file){
		versionedPieRaidFileEntityRepository.removeVersionedPieRaidFileWithAllChunks(file.getUid());
	}
	
	public void persistVersionedPieRaidFile(VersionedPieRaidFile file){
		versionedPieRaidFileEntityRepository.persistVersionedPieRaidFile(file);
	}
	
	public List<VersionedPieRaidFile> getAllVersionedPieRaidFiles(){
		return versionedPieRaidFileEntityRepository.findAllVersionedPieRaidFiles();
	}
	
	public VersionedPieRaidFile getVersionedPieRaidFileById(String id){
		return versionedPieRaidFileEntityRepository.findVersionedPieRaidFileByUId(id);
	}
}
