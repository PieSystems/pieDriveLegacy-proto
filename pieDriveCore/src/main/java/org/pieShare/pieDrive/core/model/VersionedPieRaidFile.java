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
	
	public void add(PieRaidFile file){
		versions.put((long)this.versions.size(), file);
	}
	
	public void setVersions(Map<Long, PieRaidFile> versions){
		this.versions.clear();
		this.versions.putAll(versions);
	}
	
	@JsonAnyGetter
	public Map<Long,PieRaidFile> getVersions(){
		return this.versions;
	}
	
	public PieRaidFile getLatestVersion(){
		long max = Collections.max(this.versions.keySet());
		if(!(this.versions.isEmpty())){
			return this.versions.get(max-1);
		} else {
			return null;
		}
	}
	
	public PieRaidFile getSpecificVersion(long version){
		return this.versions.get(version);
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
