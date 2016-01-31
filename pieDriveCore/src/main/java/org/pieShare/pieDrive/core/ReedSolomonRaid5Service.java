package org.pieShare.pieDrive.core;

import com.backblaze.erasure.ReedSolomon;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.ChunkHealthState;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.stream.BoundedInputStream;
import org.pieShare.pieDrive.core.stream.BoundedOutputStream;
import org.pieShare.pieDrive.core.stream.util.LimitReachedException;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieLogger.PieLogger;

public class ReedSolomonRaid5Service implements Raid5Service {
	
	private AdapterCoreService adapterCoreService;
	
	private int parityShardCount;
	
	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}
	
	public void setParityShardCount(int parityShardCount) {
		this.parityShardCount = parityShardCount;
	}
	
	@Override
	public int getParityShardCount() {
		return parityShardCount;
	}
	
	@Override
	public int getDataShardCount() {
		return getTotalShardCount() - parityShardCount;
	}
	
	@Override
	public int getTotalShardCount() {
		return adapterCoreService.getAdapters().size();
	}
	
	@Override
	public int calculateRaidChunkSize(PhysicalChunk physicalChunk) {
		int dataShardCount = getDataShardCount();
		int chunkSize = (int) physicalChunk.getSize() / dataShardCount;
		if (physicalChunk.getSize() % dataShardCount != 0) {
			chunkSize++;
		}
		return chunkSize;
	}
	
	@Override
	public byte[][] generateRaidShards(InputStream inputStream, PhysicalChunk physicalChunk) {
		int raidChunkSize = calculateRaidChunkSize(physicalChunk);
		
		BoundedInputStream boundedStream = StreamFactory.getLimitingInputStream(inputStream, physicalChunk.getSize());
		
		byte[][] raidBuffers = new byte[getTotalShardCount()][raidChunkSize];
		
		for (int i = 0; i < getDataShardCount(); i++) {
			try {
				boundedStream.read(raidBuffers[i], 0, raidChunkSize);
			} catch (IOException ex) {
				PieLogger.error(this.getClass(), "Could not read buffer from input stream", ex);
			}
		}
		
		ReedSolomon reedSolomon = ReedSolomon.create(getDataShardCount(), getParityShardCount());
		reedSolomon.encodeParity(raidBuffers, 0, raidChunkSize);
		
		return raidBuffers;
	}
	
	@Override
	public void recombineRaidShards(OutputStream outputStream, PhysicalChunk physicalChunk, byte[][] shards) {
		//TODO set stream offset or is this done at creation of the stream?
		BoundedOutputStream boundedStream = StreamFactory.getBoundedOutputStream(outputStream, physicalChunk.getSize());
		try {
			try {
				for (int i = 0; i < getDataShardCount(); i++) {
					boundedStream.write(shards[i]);
				}
			} catch (LimitReachedException ex) {
				//do nothing: intended to run into exception at last part
			} finally {
				boundedStream.flush();
				boundedStream.close();
			}
		} catch (IOException ex) {
			PieLogger.error(this.getClass(), "Could not recombine raid shards", ex);
		}
	}
	
	@Override
	public boolean repairRaidShards(byte[][] shards, PhysicalChunk physicalChunk) {
		boolean[] healthStates = new boolean[physicalChunk.getChunks().size()];

		//TODO map correct shard to adapter chunk
		int brokenShardCount = 0;
		for (AdapterChunk chunk : physicalChunk.getChunks()) {
			if (chunk.getState() == ChunkHealthState.Healthy) {
				healthStates[chunk.getDataShard()] = true;
			} else {
				healthStates[chunk.getDataShard()] = false;
				brokenShardCount++;
			}
		}
		
		if (brokenShardCount > getParityShardCount()) {
			return false; // Shards cannot be repaired
		}
		
		ReedSolomon reedSolomon = ReedSolomon.create(getDataShardCount(), getParityShardCount());
		reedSolomon.decodeMissing(shards, healthStates, 0, calculateRaidChunkSize(physicalChunk));

		//TODO check if properly recovered and update physical chunk?
		return true;
	}
	
	@Override
	public boolean isParityChunk(AdapterChunk chunk) {
		if (chunk.getDataShard() >= getDataShardCount()) {
			return true;
		}
		
		return false;
	}
	
}
