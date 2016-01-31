/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core.database.entities;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class VersionedPieRaidFileEntity {
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	private Map<Long, PieRaidFileEntity> versions;
	
	@Id
	protected String uid;
	
	@ManyToOne(fetch = FetchType.EAGER)
	protected VolumesEntity volumesEntity;
	
	public VersionedPieRaidFileEntity() {
		this.versions = new HashMap<>();
	}
	
	public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
	
	public Map<Long,PieRaidFileEntity> getVersions(){
		return this.versions;
	}
	
	public void setVersions(Map<Long,PieRaidFileEntity> versions){
		this.versions.clear();
		this.versions.putAll(versions);		
	}

	public VolumesEntity getVolumesEntity() {
		return volumesEntity;
	}

	public void setVolumesEntity(VolumesEntity volumesEntity) {
		this.volumesEntity = volumesEntity;
	}
}
