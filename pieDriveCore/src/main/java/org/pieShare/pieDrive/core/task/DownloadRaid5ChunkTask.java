/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class DownloadRaid5ChunkTask extends ADownloadChunkTask {

	private AdapterChunk chunk;
	private OutputStream out;

	public void setChunk(AdapterChunk chunk) {
		this.chunk = chunk;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	@Override
	protected void compute() {
		DigestOutputStream hStr = null;
		try {
			hStr = StreamFactory.getDigestOutputStream(out, MessageDigest.getInstance("MD5"));
			this.download(chunk, hStr);
		} catch (NoSuchAlgorithmException | AdaptorException ex) {
			PieLogger.warn(this.getClass(), "Could not download RAID5 chunk!", ex);
		} finally {
			try {
				if (hStr != null) {
					hStr.close();
				}
			} catch (IOException ex) {
				PieLogger.warn(this.getClass(), "Could not close download stream!", ex);
			}
		}
	}

}
