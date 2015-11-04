/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.adapter.api;

import java.util.UUID;
import org.pieShare.pieDrive.adapter.model.InMemoryFile;
import org.pieShare.pieDrive.adapter.model.LocalFile;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public interface Adaptor {	
	void upload(InMemoryFile file);
	void upload(LocalFile file);
}
