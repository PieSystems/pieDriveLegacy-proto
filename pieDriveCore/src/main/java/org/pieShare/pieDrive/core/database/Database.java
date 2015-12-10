/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database;

import com.dropbox.core.v1.DbxEntry.Folder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

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
import org.pieShare.pieDrive.core.model.ui.PieFolder;
import org.pieShare.pieDrive.core.model.ui.Volume;
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

	public Collection<Volume> getAllVolumes() {

		List<Volume> volumes = new ArrayList<>();

		try {
			for (VolumesEntity entity : volumesEntityRepository.findAll()) {
				volumes.add(convertVolumeEntityToVolume(entity));
			}
		} catch (Exception ex) {
			//when the table does not exist
		}

		return volumes;
	}

	public Volume getVolumeById(String id) {
		VolumesEntity entity = volumesEntityRepository.findOne(id);
		return convertVolumeEntityToVolume(entity);
	}

	private Volume convertVolumeEntityToVolume(VolumesEntity entity) {
		Volume v = new Volume();
		v.setVolumeName(entity.getVolumeName());
		v.setId(entity.getId());
		v.setRaidLevel(entity.getRaidLevel());
		return v;
	}

	public Folder getFolderById(Long id) {
		return null;//folderEntityRepository.findOne(id);
	}

	public void persistVolume(Volume volume) {
		VolumesEntity entity = new VolumesEntity();

		List<FolderEntity> folderEntitys = new ArrayList<>();
		List<PieRaidFileEntity> files = new ArrayList<>();

		entity.setVolumeName(volume.getVolumeName());
		entity.setId(volume.getId());
		entity.setRaidLevel(volume.getRaidLevel());

		/*
        for (PieRaidFile raidFile : volume.getFiles()) {
            files.add(pieRaidFileEntityRepository.findOne(raidFile.getUid()));
        }
        
        FolderEntity entity1 = new FolderEntity();
        
        for(PieFolder folder : volume.getFolders())
        {
            folder.get
        }

        entity.setFiles(volume.getFiles());
        entity.setFolders(volume.getFolders());*/
		volumesEntityRepository.save(entity);

	}

}
