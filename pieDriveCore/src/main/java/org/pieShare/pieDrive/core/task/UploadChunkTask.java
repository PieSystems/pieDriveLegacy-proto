/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.RecursiveAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class UploadChunkTask extends RecursiveAction {

	private AdapterCoreService adapterCoreService;
	//private PhysicalChunk physicalChunk;
	private AdapterChunk chunk;
	private Database database;
	//private RandomAccessFile file;

	private InputStream in;

	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}

	public void setChunk(AdapterChunk chunk) {
		this.chunk = chunk;
	}

	public void setIn(InputStream in) {
		this.in = in;
	}

	public AdapterChunk getChunk() {
		return chunk;
	}

//	public void setFile(RandomAccessFile file) {
//		this.file = file;
//	}
//	public void setPhysicalChunk(PhysicalChunk physicalChunk) {
//		this.physicalChunk = physicalChunk;
//	}
	public void setDatabase(Database database) {
		this.database = database;
	}

	@Override
	public void compute() {
		PieLogger.debug(this.getClass(), "Starting chunk upload for "
				+ "{} with adapter {}", this.chunk.getUuid(), this.chunk.getAdapterId().getId());
		DigestInputStream hStr = null;
		try {
//			NioInputStream nioStream = StreamFactory.getNioInputStream(file, physicalChunk.getOffset());
//			BufferedInputStream bufferedStream = StreamFactory.getBufferedInputStream(nioStream, 65536); //64kB
//			BoundedInputStream lStr = StreamFactory.getLimitingInputStream(bufferedStream, physicalChunk.getSize());
//			hStr = StreamFactory.getDigestInputStream(lStr, MessageDigest.getInstance("MD5"));
			hStr = StreamFactory.getDigestInputStream(this.in, MessageDigest.getInstance("MD5"));

			adapterCoreService.getAdapter(chunk.getAdapterId()).upload(chunk, hStr);
			byte[] hash = hStr.getMessageDigest().digest();

			//todo: this boolean is basically only for the recovery to not write a 2nd time to the DB
			//is this really neccessary?!
			//boolean updateChunk = (chunk.getHash() == null || chunk.getHash().length == 0);
			chunk.setHash(hash);
			chunk.setState(ChunkHealthState.Healthy);
			//if we are the first and the physical chunk has not yet a hash value
			//has to be synchronized for the adapters of the same physical chunk
			//after this point they can work in parallel again
//			synchronized (physicalChunk) {
//				if (physicalChunk.getHash() == null
//						|| physicalChunk.getHash().length == 0) {
//					physicalChunk.setHash(hash);
//					synchronized (database) {
//						this.database.updatePhysicalChunk(physicalChunk);
//						return;
//					}
//				}
//			}	//todo: remove synchronizations after @richy fixes threading in DB
			//otherwise do an sanity check and persist hashes
			//if (updateChunk && Arrays.equals(physicalChunk.getHash(), hash)) {
			synchronized (database) {
				this.database.updateAdaptorChunk(chunk);
				//return;
			}
			//}

			//this should never happen!!! if this happens two adapters
			//produced different hashes while reading the samephysical chunk
			//todo: log! eventually pass on to user
		} catch (NoSuchAlgorithmException | AdaptorException ex) {
			PieLogger.warn(this.getClass(), "An exception was thrown during upload!", ex);
			chunk.setState(ChunkHealthState.Broken);
		} finally {
			try {
				if (hStr != null) {
					hStr.close();
				}
			} catch (IOException e) {
				PieLogger.warn(this.getClass(), "An exception was thrown during stream close!", e);
			}
		}
	}
}
