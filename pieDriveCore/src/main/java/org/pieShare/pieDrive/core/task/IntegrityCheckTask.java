package org.pieShare.pieDrive.core.task;

import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.output.NullOutputStream;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;

public class IntegrityCheckTask extends ADownloadChunkTask implements IPieTask {
	@Override
	public void run() {
		for (AdapterChunk adapterChunk : physicalChunk.getChunks().values()) {
			if (adapterChunk.getState() == ChunkHealthState.NotChecked) {
				DigestOutputStream digestStream = null;
				try {
					NullOutputStream nullStream = new NullOutputStream();
					digestStream = StreamFactory.getDigestOutputStream(nullStream, MessageDigest.getInstance("MD5"));

					this.download(adapterChunk, digestStream);
				} catch (NoSuchAlgorithmException ex) {
					Logger.getLogger(IntegrityCheckTask.class.getName()).log(Level.SEVERE, null, ex);
					if (digestStream != null) {
						try {
							digestStream.close();
						} catch (IOException e) {
							Logger.getLogger(IntegrityCheckTask.class.getName()).log(Level.SEVERE, null, e);
						}
					}
				}
			}
		}
	}
}
