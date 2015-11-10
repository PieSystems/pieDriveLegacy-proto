/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.adapter.model;

import java.io.InputStream;
import java.util.UUID;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class PieDriveFile {
	private String uuid;
	private InputStream fileData;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public InputStream getFileData() {
		return fileData;
	}

	public void setFileData(InputStream fileData) {
		this.fileData = fileData;
	}
}
