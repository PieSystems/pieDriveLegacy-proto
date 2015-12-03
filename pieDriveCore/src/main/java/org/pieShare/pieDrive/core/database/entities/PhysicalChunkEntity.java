/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.entities;

import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;

/**
 *
 * @author richy
 */
@Entity
public class PhysicalChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "physicalChunkEntity")
    private List<AdapterChunkEntity> chunks;

    private long offset;
    private long size;

    @Column(nullable = true)
    private byte[] hashValues;
    
    @ManyToOne
    private PieRaidFileEntity pieRaidFileEntity;

    public List<AdapterChunkEntity> getChunks() {
        return chunks;
    }

    public void setChunks(List<AdapterChunkEntity> chunks) {
        this.chunks = chunks;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public PieRaidFileEntity getPieRaidFileEntity() {
        return pieRaidFileEntity;
    }

    public void setPieRaidFileEntity(PieRaidFileEntity pieRaidFileEntity) {
        this.pieRaidFileEntity = pieRaidFileEntity;
    }

    public Long getId() {
        return id;
    }

    public byte[] getHashValues() {
        return hashValues;
    }

    public void setHashValues(byte[] hashValues) {
        this.hashValues = hashValues;
    }
}
