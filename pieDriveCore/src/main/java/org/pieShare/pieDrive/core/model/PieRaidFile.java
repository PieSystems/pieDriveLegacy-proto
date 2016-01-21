/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class PieRaidFile {

    private String relativeFilePath;
    private String fileName;
    private long lastModified;
    //todo: will need fixing!!! due to the map in the physical chunk it can not be easily de/serialized
    @JsonIgnore
    private Map<Long,PhysicalChunk> chunks;
    private long fileSize;
    private String uid;

    public PieRaidFile() {
        uid = UUID.randomUUID().toString();
        chunks = new HashMap<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRelativeFilePath() {
        return relativeFilePath;
    }

    public void setRelativeFilePath(String relativeFilePath) {
        this.relativeFilePath = relativeFilePath;
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

    public List<PhysicalChunk> getChunks() {
        return new ArrayList(chunks.values());
    }

    public void setChunks(List<PhysicalChunk> chunks) {
        this.chunks = chunks;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof PieRaidFile)) {
            return false;
        }

        PieRaidFile rr = (PieRaidFile)obj;
        
        return rr.getUid().equals(this.uid);
    }

    @Override
    public int hashCode() {
        return this.uid.hashCode();
    }

}
