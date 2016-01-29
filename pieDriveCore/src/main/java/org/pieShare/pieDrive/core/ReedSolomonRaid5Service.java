package org.pieShare.pieDrive.core;

import com.backblaze.erasure.ReedSolomon;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.stream.BoundedInputStream;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;

public class ReedSolomonRaid5Service implements Raid5Service {
	private AdapterCoreService adapterCoreService;
	
	private int parityShardCount;
	
	public void setAdapterCoreService(AdapterCoreService adapterCoreService) {
		this.adapterCoreService = adapterCoreService;
	}
	
	public void setParityShardCount(int parityShardCount) {
		this.parityShardCount = parityShardCount;
	}
	
	public int getParityShardCount() {
		return parityShardCount;
	}
	
	public int getDataShardCount() {
		return adapterCoreService.getAdapters().size() - parityShardCount;
	}
	
	@Override
	public int calculateRaidChunkSize(PhysicalChunk physicalChunk) {
		int dataShardCount = getDataShardCount();
		int chunkSize = (int) physicalChunk.getSize() / dataShardCount;
		if(physicalChunk.getSize() % dataShardCount != 0) {
			chunkSize++;
		}
		return chunkSize;
	}
	
	@Override
	public byte[][] generateRaidShards(InputStream inputStream, PhysicalChunk physicalChunk) {
		int raidChunkSize = calculateRaidChunkSize(physicalChunk);
		int adapterCount = adapterCoreService.getAdapters().size();

		BufferedInputStream bufferedStream = StreamFactory.getBufferedInputStream(inputStream, 65536); //64kB
		BoundedInputStream boundedStream = StreamFactory.getLimitingInputStream(bufferedStream, physicalChunk.getSize());

		byte[][] raidBuffers = new byte[adapterCount][raidChunkSize];
		
		for(int i = 0; i < getDataShardCount(); i++) {
			try {
				boundedStream.read(raidBuffers[i]);
			} catch (IOException ex) {
				//TODO error handling
			}
		}

		ReedSolomon reedSolomon = ReedSolomon.create(adapterCount - parityShardCount, parityShardCount);
		reedSolomon.encodeParity(raidBuffers, 0, raidChunkSize);
		
		return raidBuffers;
	}

}
