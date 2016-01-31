package org.pieShare.pieDrive.core.database.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roland on 08.12.2015.
 */
@Entity
public class VolumesEntity {

    @Id
    private String id;
    private String volumeName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FolderEntity> folders;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VersionedPieRaidFileEntity> files;

    //TODO change to enum
    private int raidLevel;

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

    public void addFile(VersionedPieRaidFileEntity file) {
        if (files == null) {
            files = new ArrayList<>();
        }
        files.add(file);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public int getRaidLevel() {
        return raidLevel;
    }

    public void setRaidLevel(int raidLevel) {
        this.raidLevel = raidLevel;
    }

    public List<FolderEntity> getFolders() {
        return folders;
    }

    public void setFolders(List<FolderEntity> folders) {
        this.folders = folders;
    }

    public List<VersionedPieRaidFileEntity> getFiles() {
        return files;
    }

    public void setFiles(List<VersionedPieRaidFileEntity> files) {
        this.files = files;
    }
}
