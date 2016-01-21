/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core.database.entities;

import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class VersionedPieRaidFileEntity {
	@ManyToMany
	private Map<Long, PieRaidFileEntity> versions;
	
	@Id
	protected String uid;
	
	public VersionedPieRaidFileEntity() {
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
}
