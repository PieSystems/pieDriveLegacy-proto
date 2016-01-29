/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.repository;

import java.util.ArrayList;
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
public class FolderEntityRepositoryImpl implements FolderEntityRepositoryCustom {

    @Autowired
    private PieRaidFileEntityRepository pieRaidFileEntityRepository;

    @Autowired
    private FolderEntityRepository folderEntityRepository;

    @Override
    public void persistFolder(PieFolder folder) {

        FolderEntity entity = new FolderEntity();

        entity.setFolderName(folder.getName());
        entity.setUid(folder.getId());

        entity.setFolders(convertFolderToFolderEntities(folder.getFolders()));

        List<PieRaidFileEntity> pieRaidFileEntities = new ArrayList<>();

        for (PieRaidFile raidFile : folder.getFiles()) {
            PieRaidFileEntity d = pieRaidFileEntityRepository.findOne(raidFile.getUid());
            if (d != null) {
                pieRaidFileEntities.add(d);
            } else {
                pieRaidFileEntities.add(pieRaidFileEntityRepository.persistPieRaidFile(raidFile));
            }
        }
        entity.setFiles(pieRaidFileEntities);

        folderEntityRepository.save(entity);
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
                PieRaidFile ff = pieRaidFileEntityRepository.findPieRaidFileByUId(raidFileEntity.getUid());

                if (ff != null) {
                    pieRaidFiles.add(ff);
                }
            }

            pieFolder.setFiles(pieRaidFiles);
            pieFolder.setName(folderEntity.getFolderName());
            pieFolder.setFolders(returnPieFolders);
            pieFolder.setId(folderEntity.getUid());
            pieFolders.add(pieFolder);

        }
        return pieFolders;
    }

    private PieFolder convertToPieFolder(FolderEntity entity) {
        PieFolder folder = new PieFolder();
        folder.setName(entity.getFolderName());
        folder.setId(entity.getUid());

        folder.setFolders(convertFolderEntityToFolder(entity.getFolders()));

        List<PieRaidFile> pieRaidFiles = new ArrayList<>();

        for (PieRaidFileEntity raidFileEntity : entity.getFiles()) {
            PieRaidFile ff = pieRaidFileEntityRepository.findPieRaidFileByUId(raidFileEntity.getUid());

            if (ff != null) {
                pieRaidFiles.add(ff);
            }
        }

        folder.setFiles(pieRaidFiles);
        return folder;
    }

    @Override
    public PieFolder findFolderByUid(String uid) {
        return convertToPieFolder(folderEntityRepository.findOne(uid));
    }

    @Override
    public List<PieFolder> findAllFolders() {

        List<PieFolder> list = new ArrayList<>();

        try {
            for (FolderEntity entity : folderEntityRepository.findAll()) {
                list.add(convertToPieFolder(entity));
            }
        } catch (Exception ex) {
            return list;
        }
        return list;
    }
}
