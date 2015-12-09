/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task.help;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.adapter.model.PieDriveFile;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class FakeAdapter implements Adaptor {

	private File parent;

	public void setParent(File parent) {
		this.parent = parent;
	}
	
	@Override
	public void delete(PieDriveFile file) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void upload(PieDriveFile file, InputStream stream) {
		FileOutputStream fStr = null;
		try {
			File realFile = new File(parent, file.getUuid());
			fStr = new FileOutputStream(realFile);
			
			int b = -1;
			while((b = stream.read()) != -1) {
				fStr.write(b);
			}
			
			fStr.flush();
			fStr.close();
			stream.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(FakeAdapter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(FakeAdapter.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fStr.close();
			} catch (IOException ex) {
				Logger.getLogger(FakeAdapter.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void download(PieDriveFile file, OutputStream stream) {
		FileInputStream fStr = null;
		try {
			File realFile = new File(this.parent, file.getUuid());
			fStr = new FileInputStream(realFile);
			
			int b = -1;
			while((b = fStr.read()) != -1) {
				stream.write(b);
			}
			
			fStr.close();
			stream.flush();
			stream.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(FakeAdapter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(FakeAdapter.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fStr.close();
			} catch (IOException ex) {
				Logger.getLogger(FakeAdapter.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
}
