/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.adapter.api;

import java.io.InputStream;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import java.io.OutputStream;
import org.pieShare.pieDrive.adapter.model.PieDriveFile;

public interface Adaptor {

    void delete(PieDriveFile file) throws AdaptorException;

    void upload(PieDriveFile file, InputStream stream) throws AdaptorException;

    void download(PieDriveFile file, OutputStream stream) throws AdaptorException;
	
	boolean authenticate();
}
