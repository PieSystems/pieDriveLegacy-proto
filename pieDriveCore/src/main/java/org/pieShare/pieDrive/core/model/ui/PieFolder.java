/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.model.ui;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.util.List;
import org.pieShare.pieDrive.core.model.PieRaidFile;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class PieFolder implements Serializable {
	private String id;
	private String name;
	private List<PieRaidFile> files;
	private List<PieFolder> folders;
	
	public void addFolder(PieFolder folder) {
		this.folders.add(folder);
	}
	
	public void addFile(PieRaidFile file) {
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

	public List<PieRaidFile> getFiles() {
		return files;
	}

	public void setFiles(List<PieRaidFile> files) {
		this.files = files;
	}

	public List<PieFolder> getFolders() {
		return folders;
	}

	public void setFolders(List<PieFolder> folders) {
		this.folders = folders;
	}
}
