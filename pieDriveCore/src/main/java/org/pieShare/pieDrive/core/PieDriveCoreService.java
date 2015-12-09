package org.pieShare.pieDrive.core;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;

public class PieDriveCoreService implements PieDriveCore {

	//5MB
	private long chunkSize = 5 * 1024 * 1024;
	
	public void setChunkSize(long chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	@Override
	public List<PhysicalChunk> calculateChunks(File file) {
		List<PhysicalChunk> chunks = new ArrayList<>();
		for(long i = 0; i < file.length(); i += chunkSize) {
			PhysicalChunk chunk = new PhysicalChunk();
			chunk.setOffset(i);
			
			long size = file.length() - i;
			if(size > chunkSize) {
				size = chunkSize;
			}
			chunk.setSize(size);
			
			chunks.add(chunk);
		}
		
		return chunks;
	}

	@Override
	public PieRaidFile calculateRaidFile(File file) {
		PieRaidFile raidFile = new PieRaidFile();
		raidFile.setFileName(file.getName());
		raidFile.setLastModified(file.lastModified());
		raidFile.setRelativeFilePath(file.getPath());
		raidFile.setChunks(calculateChunks(file));
		raidFile.setFileSize(file.length());
		return raidFile;
	}
}
