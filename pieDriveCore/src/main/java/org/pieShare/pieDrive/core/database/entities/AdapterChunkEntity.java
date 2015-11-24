/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author richy
 */
@Entity
public class AdapterChunkEntity {

    @Id
    private String adapterId;
    private byte[] hash;
    private String UUID;
    
    @ManyToOne
    private PhysicalChunkEntity physicalChunkEntity;
    
    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public PhysicalChunkEntity getPhysicalChunkEntity() {
        return physicalChunkEntity;
    }

    public void setPhysicalChunkEntity(PhysicalChunkEntity physicalChunkEntity) {
        this.physicalChunkEntity = physicalChunkEntity;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }
}
