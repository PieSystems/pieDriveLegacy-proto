/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.adapter.api;

import org.pieShare.pieDrive.adapter.model.PieDriveFile;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public interface Adaptor {

    void delte(PieDriveFile file);

    void upload(PieDriveFile file);

    void download(PieDriveFile file);
}
