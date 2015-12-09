package org.pieShare.pieDrive.core.database.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roland on 08.12.2015.
 */
@Entity
public class VolumesEntity extends BaseEntity {

    private String volumeName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FolderEntity> folders;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PieRaidFileEntity> files;

    public VolumesEntity() {
        folders = new ArrayList<>();
        files = new ArrayList<>();
    }

    public void addFolder(FolderEntity folder) {
        if (folders == null) {
            folders = new ArrayList<>();
        }
        folders.add(folder);
    }

    public void addFile(PieRaidFileEntity file) {
        if (files == null) {
            files = new ArrayList<>();
        }
        files.add(file);
    }

    //TODO change to enum
    private String raidLevel;

    public void setId(Long id) {
        this.id = id;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public String getRaidLevel() {
        return raidLevel;
    }

    public void setRaidLevel(String raidLevel) {
        this.raidLevel = raidLevel;
    }

    public List<FolderEntity> getFolders() {
        return folders;
    }

    public void setFolders(List<FolderEntity> folders) {
        this.folders = folders;
    }

    public List<PieRaidFileEntity> getFiles() {
        return files;
    }

    public void setFiles(List<PieRaidFileEntity> files) {
        this.files = files;
    }
}
