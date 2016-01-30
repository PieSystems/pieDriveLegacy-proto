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
import org.pieShare.pieDrive.core.database.entities.FolderEntity;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.model.RaidLevel;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class Volume extends PieFolder {
	private RaidLevel raidLevel;

	public RaidLevel getRaidLevel() {
		return raidLevel;
	}

	public void setRaidLevel(RaidLevel raidLevel) {
		this.raidLevel = raidLevel;
	}

	
}
