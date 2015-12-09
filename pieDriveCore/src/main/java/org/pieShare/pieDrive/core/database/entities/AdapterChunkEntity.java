/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author richy
 */
@Entity
public class AdapterChunkEntity {

    private String adapterId;
    
    @Column(nullable = true)
    private byte[] hashValue;
    
    public AdapterChunkEntity()
    {
        //values = new HashMap<>();
    }
    
   // @ElementCollection
    //public Map<Integer, Byte> values;
    
    @Id
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
        
        return hashValue;
        /*
        byte[] retValue = new byte[values.size()];
        
        for(Entry<Integer, Byte> entry : values.entrySet())
        {
            retValue[entry.getKey()] = (byte)entry.getValue();
        }
        
        return retValue;*/
    }

    public void setHash(byte[] hash) {
        
        hashValue = hash;
        /*
        values = new HashMap<>();
        if(hash == null) return;
        
        for(int i = 0; i < hash.length; i++)
        {
            values.put(i, (Byte)hash[i]);
        }*/
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
