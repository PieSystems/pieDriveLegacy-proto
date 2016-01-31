/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.pieShare.pieDrive.core.database.entities.VersionedPieRaidFileEntity;
import org.pieShare.pieDrive.core.database.entities.VolumesEntity;
import org.pieShare.pieDrive.core.model.VersionedPieRaidFile;
import org.pieShare.pieDrive.core.model.RaidLevel;
import org.pieShare.pieDrive.core.model.ui.Volume;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author richy
 */
public class VolumeEntityRepositoryImpl implements VolumeEntityRepositoryCustom {

    @Autowired
    private VolumeEntityRepository volumeEntityRepository;

    @Autowired
    private VersionedPieRaidFileEntityRepository versionedPieRaidFileEntityRepository;

    @Override
    public void persistVolume(Volume volume) {
        VolumesEntity entity = new VolumesEntity();

        entity.setVolumeName(volume.getName());
        entity.setId(volume.getId());
        entity.setRaidLevel(volume.getRaidLevel().ordinal());

        List<VersionedPieRaidFileEntity> versionedPieRaidFileEntities = new ArrayList<>();

        for (VersionedPieRaidFile raidFile : volume.getFiles()) {

            VersionedPieRaidFileEntity d = versionedPieRaidFileEntityRepository.findOne(raidFile.getUid());
            if (d != null) {
                versionedPieRaidFileEntities.add(d);
            } else {
				d = versionedPieRaidFileEntityRepository.persistVersionedPieRaidFile(raidFile);
                versionedPieRaidFileEntities.add(d);
            }
			
			d.setVolumesEntity(entity);

        }
        entity.setFiles(versionedPieRaidFileEntities);

        volumeEntityRepository.save(entity);
    }
    
    private Volume convertVolumeEntityToVolume(VolumesEntity entity) {
        Volume v = new Volume();
        v.setName(entity.getVolumeName());
        v.setId(entity.getId());
        v.setRaidLevel(RaidLevel.values()[entity.getRaidLevel()]);// entity.getRaidLevel());

        List<VersionedPieRaidFile> versionedPieRaidFiles = new ArrayList<>();

        for (VersionedPieRaidFileEntity raidFileEntity : entity.getFiles()) {

            VersionedPieRaidFile ff = versionedPieRaidFileEntityRepository.findVersionedPieRaidFileByUId(raidFileEntity.getUid());

            if (ff != null) {
                versionedPieRaidFiles.add(ff);
            }
        }
        v.setFiles(versionedPieRaidFiles);

        return v;
    }

    @Override
    public Collection<Volume> getAllVolumes() {

        List<Volume> volumes = new ArrayList<>();

        try {

            for (VolumesEntity entity : volumeEntityRepository.findAll()) {
                volumes.add(convertVolumeEntityToVolume(entity));
            }
        } catch (Exception ex) {
            return volumes;
        }
        return volumes;
    }

    @Override
    public Volume getVolumeByUId(String id) {
        VolumesEntity entity = volumeEntityRepository.findOne(id);
        return convertVolumeEntityToVolume(entity);
    }
}
