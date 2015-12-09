package org.pieShare.pieDrive.core.database.entities;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Roland on 09.12.2015.
 */

@Entity
public class FolderEntity {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FolderEntity> folders;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PieRaidFileEntity> files;

    private String folderName;


    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
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
