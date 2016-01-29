package org.pieShare.pieDrive.core;

import java.io.InputStream;
import org.pieShare.pieDrive.core.model.PhysicalChunk;

public interface Raid5Service {
	byte[][] generateRaidShards(InputStream inputStream, PhysicalChunk physicalChunk);
	int calculateRaidChunkSize(PhysicalChunk physicalChunk);
}
