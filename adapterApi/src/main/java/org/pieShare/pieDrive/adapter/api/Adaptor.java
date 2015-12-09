/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.adapter.api;

import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.adapter.model.PieDriveFile;

public interface Adaptor {

    void delte(PieDriveFile file) throws AdaptorException;

    void upload(PieDriveFile file) throws AdaptorException;

    void download(PieDriveFile file) throws AdaptorException;
}
