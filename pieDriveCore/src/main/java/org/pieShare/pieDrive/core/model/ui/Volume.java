/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.model.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.pieShare.pieDrive.core.model.VersionedPieRaidFile;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class Volume implements Serializable  {
	private String raidLevel;
	private String id;
    private String name;
    private List<VersionedPieRaidFile> files;
	
	public Volume(){
		files = new ArrayList<>();
	}

	public String getRaidLevel() {
		return raidLevel;
	}

	public void setRaidLevel(String raidLevel) {
		this.raidLevel = raidLevel;
	}
	
	public void addFile(VersionedPieRaidFile file) {
        this.files.add(file);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<VersionedPieRaidFile> getFiles() {
        return files;
    }

    public void setFiles(List<VersionedPieRaidFile> files) {
        this.files = files;
    }
}
