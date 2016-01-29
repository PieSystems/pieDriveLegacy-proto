/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//all versionedfiles for volume
//

public class VersionedPieRaidFile {
	private Map<Long, PieRaidFile> versions;
	private String uid;
	
	public VersionedPieRaidFile(){
		this.uid = UUID.randomUUID().toString();
		this.versions = new HashMap();
	}
	
	public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
	
	public void add(Long version, PieRaidFile file){
		versions.put(version, file);
	}
	
	@JsonAnyGetter
	public Map<Long,PieRaidFile> getVersions(){
		return this.versions;
	}
	
	public void setVersions(Map<Long,PieRaidFile> versions){
		this.versions.clear();
		this.versions.putAll(versions);		
	}
	
	public PieRaidFile getLatestVersion(){
		long max = Collections.max(this.versions.keySet());
		return this.versions.get(max);
	}
	
	@Override
    public boolean equals(Object obj) {

        if (!(obj instanceof VersionedPieRaidFile)) {
            return false;
        }

        VersionedPieRaidFile rr = (VersionedPieRaidFile)obj;
        
        return rr.getUid().equals(this.uid);
    }

    @Override
    public int hashCode() {
        return this.uid.hashCode();
    }
}
