/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.entities;

import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.pieShare.pieDrive.core.model.PhysicalChunk;

/**
 *
 * @author richy
 */
@Entity
public class PieRaidFileEntity {

    private String fileName;
    private long lastModified;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pieRaidFileEntity")
    private List<PhysicalChunkEntity> chunks;
    private String relativeFilePath;
    
    @Id
    private String uid;

    public PieRaidFileEntity() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public List<PhysicalChunkEntity> getChunks() {
        return chunks;
    }

    public void setChunks(List<PhysicalChunkEntity> chunks) {
        this.chunks = chunks;
    }

    public String getRelativeFilePath() {
        return relativeFilePath;
    }

    public void setRelativeFilePath(String relativeFilePath) {
        this.relativeFilePath = relativeFilePath;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    
    
}
