/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.pieShare.pieDrive.core.database.entities.FolderEntity;
import org.pieShare.pieDrive.core.database.entities.PieRaidFileEntity;
import org.pieShare.pieDrive.core.database.entities.VolumesEntity;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.model.ui.PieFolder;
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
    private PieRaidFileEntityRepository pieRaidFileEntityRepository;

    @Override
    public void persistVolume(Volume volume) {
        VolumesEntity entity = new VolumesEntity();

        entity.setVolumeName(volume.getName());
        entity.setId(volume.getId());
        entity.setRaidLevel(volume.getRaidLevel());

        entity.setFolders(convertFolderToFolderEntities(volume.getFolders()));

        List<PieRaidFileEntity> pieRaidFileEntities = new ArrayList<>();

        for (PieRaidFile raidFile : volume.getFiles()) {

            PieRaidFileEntity d = pieRaidFileEntityRepository.findOne(raidFile.getUid());
            if (d != null) {
                pieRaidFileEntities.add(d);
            } else {
                pieRaidFileEntities.add(pieRaidFileEntityRepository.persistPieRaidFile(raidFile));
            }

        }
        entity.setFiles(pieRaidFileEntities);

        volumeEntityRepository.save(entity);
    }

    private List<FolderEntity> convertFolderToFolderEntities(List<PieFolder> pieFoldes) {

        List<FolderEntity> folderEntities = new ArrayList<>();

        for (PieFolder folder : pieFoldes) {

            FolderEntity folderEntity = new FolderEntity();
            List<FolderEntity> returnFolderEntitys = convertFolderToFolderEntities(folder.getFolders());

            List<PieRaidFileEntity> pieRaidFileEntities = new ArrayList<>();

            for (PieRaidFile raidFile : folder.getFiles()) {
                PieRaidFileEntity d = pieRaidFileEntityRepository.findOne(raidFile.getUid());
                if (d != null) {
                    pieRaidFileEntities.add(d);
                } else {
                    pieRaidFileEntities.add(pieRaidFileEntityRepository.persistPieRaidFile(raidFile));
                }
            }

            folderEntity.setFiles(pieRaidFileEntities);
            folderEntity.setFolderName(folder.getName());
            folderEntity.setFolders(returnFolderEntitys);
            folderEntity.setUid(folder.getId());

            folderEntities.add(folderEntity);

        }
        return folderEntities;
    }

    private List<PieFolder> convertFolderEntityToFolder(List<FolderEntity> folderEntitys) {

        List<PieFolder> pieFolders = new ArrayList<>();

        for (FolderEntity folderEntity : folderEntitys) {

            PieFolder pieFolder = new PieFolder();
            List<PieFolder> returnPieFolders = convertFolderEntityToFolder(folderEntity.getFolders());

            List<PieRaidFile> pieRaidFiles = new ArrayList<>();

            for (PieRaidFileEntity raidFileEntity : folderEntity.getFiles()) {
                pieRaidFiles.add(pieRaidFileEntityRepository.findPieRaidFileByUId(raidFileEntity.getUid()));
            }

            pieFolder.setFiles(pieRaidFiles);
            pieFolder.setName(folderEntity.getFolderName());
            pieFolder.setFolders(returnPieFolders);
            pieFolder.setId(folderEntity.getUid());
            pieFolders.add(pieFolder);

        }
        return pieFolders;
    }

    private Volume convertVolumeEntityToVolume(VolumesEntity entity) {
        Volume v = new Volume();
        v.setName(entity.getVolumeName());
        v.setId(entity.getId());
        v.setRaidLevel(entity.getRaidLevel());

        v.setFolders(convertFolderEntityToFolder(entity.getFolders()));

        List<PieRaidFile> pieRaidFiles = new ArrayList<>();

        for (PieRaidFileEntity raidFileEntity : entity.getFiles()) {
            pieRaidFiles.add(pieRaidFileEntityRepository.findPieRaidFileByUId(raidFileEntity.getUid()));
        }
        v.setFiles(pieRaidFiles);

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
