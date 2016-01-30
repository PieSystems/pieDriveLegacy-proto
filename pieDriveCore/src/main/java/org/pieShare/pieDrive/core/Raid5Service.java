package org.pieShare.pieDrive.core;

import java.io.InputStream;
import java.io.OutputStream;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.PhysicalChunk;

public interface Raid5Service {
	byte[][] generateRaidShards(InputStream inputStream, PhysicalChunk physicalChunk);
	void recombineRaidShards(OutputStream outputStream, PhysicalChunk physicalChunk, byte[][] shards);
	boolean repairRaidShards(byte[][] shards, PhysicalChunk physicalChunk);
	int calculateRaidChunkSize(PhysicalChunk physicalChunk);
	boolean isParityChunk(AdapterChunk chunk);
}
