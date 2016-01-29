/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core.database.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.pieShare.pieDrive.core.database.entities.AdapterChunkEntity;
import org.pieShare.pieDrive.core.database.entities.PhysicalChunkEntity;
import org.pieShare.pieDrive.core.database.entities.PieRaidFileEntity;
import org.pieShare.pieDrive.core.database.entities.VersionedPieRaidFileEntity;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.model.VersionedPieRaidFile;
import org.springframework.beans.factory.annotation.Autowired;


public class VersionedPieRaidFileEntityRepositoryImpl implements VersionedPieRaidFileEntityRepositoryCustom {
	@Autowired
	private VersionedPieRaidFileEntityRepository versionedPieRaidFileEntityRepository;
	
	@Autowired
    private PieRaidFileEntityRepository pieRaidFileEntityRepository;

	@Override
	public VersionedPieRaidFileEntity persistVersionedPieRaidFile(VersionedPieRaidFile versionedPieRaidFile) {
		VersionedPieRaidFileEntity versionedRaidFileEntity = new VersionedPieRaidFileEntity();
				
		for(Entry<Long, PieRaidFile> entry : versionedPieRaidFile.getVersions().entrySet()){
			
			List<PhysicalChunkEntity> physicalChunkEntities = new ArrayList<>();
			PieRaidFileEntity pieRaidFileEntity = new PieRaidFileEntity();
			
			for (PhysicalChunk chunk : entry.getValue().getChunks()) {

				PhysicalChunkEntity physicalChunkEntity = new PhysicalChunkEntity();

				List<AdapterChunkEntity> ace = new ArrayList<>();

				for (AdapterChunk adapterChunk : chunk.getChunks()) {
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
			pieRaidFileEntity.setFileName(entry.getValue().getFileName());
			pieRaidFileEntity.setLastModified(entry.getValue().getLastModified());
			pieRaidFileEntity.setRelativeFilePath(entry.getValue().getRelativeFilePath());
			pieRaidFileEntity.setUid(entry.getValue().getUid());
		}
		
		return versionedPieRaidFileEntityRepository.save(versionedRaidFileEntity);
	}

	private VersionedPieRaidFile convertEntityToObject(VersionedPieRaidFileEntity versionedPieRaidFileEntity){
		VersionedPieRaidFile versionedPieRaidFile = new VersionedPieRaidFile();
		
		if(versionedPieRaidFileEntity == null){
			return null;
		}
				
		for(Entry<Long, PieRaidFileEntity> entry : versionedPieRaidFileEntity.getVersions().entrySet()){
			PieRaidFile piePieRaidFile = new PieRaidFile();
			
			List<PhysicalChunk> physicalChunks = new ArrayList<>();

			for (PhysicalChunkEntity physicalChunkEntity : entry.getValue().getChunks()) {

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
			piePieRaidFile.setFileName(entry.getValue().getFileName());
			piePieRaidFile.setLastModified(entry.getValue().getLastModified());
			piePieRaidFile.setRelativeFilePath(entry.getValue().getRelativeFilePath());
			piePieRaidFile.setUid(entry.getValue().getUid());
			
			versionedPieRaidFile.add(entry.getKey(), piePieRaidFile);
		}
			
		return versionedPieRaidFile;
	}
	
	@Override
	public VersionedPieRaidFile findVersionedPieRaidFileByUId(String id) {
		VersionedPieRaidFileEntity ent = versionedPieRaidFileEntityRepository.findOne(id);
        if (ent != null) {
            return convertEntityToObject(ent);
        } else {
            return null;
        }
	}

	@Override
	public List<VersionedPieRaidFile> findAllVersionedPieRaidFiles() {
		List<VersionedPieRaidFile> files = new ArrayList();
		List<VersionedPieRaidFileEntity> fEntities = versionedPieRaidFileEntityRepository.findAll();
		
		for (VersionedPieRaidFileEntity entity : fEntities){
			VersionedPieRaidFile file = convertEntityToObject(entity);
			if(file != null){
				files.add(file);
			}
		}
		
		return files;
	}

	@Override
	public void removeVersionedPieRaidFileWithAllChunks(String id) {
		VersionedPieRaidFileEntity entity = versionedPieRaidFileEntityRepository.findOne(id);
		
		for (PieRaidFileEntity fileEntity : entity.getVersions().values()) {
			pieRaidFileEntityRepository.removePieRaidFileWithAllChunks(fileEntity.getId());
		}
		versionedPieRaidFileEntityRepository.delete(id);
	}
}
