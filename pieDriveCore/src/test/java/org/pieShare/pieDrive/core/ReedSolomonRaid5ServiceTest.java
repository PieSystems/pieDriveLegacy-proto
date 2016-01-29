package org.pieShare.pieDrive.core;

import com.backblaze.erasure.ReedSolomon;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.*;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ReedSolomonRaid5ServiceTest {
	private final int PARITY_SHARD_COUNT = 1; // Do not change, will break tests because they expect one parity shard
	private final int DATA_SHARD_COUNT = 2;
	private AdapterCoreService adapterCoreService;
	
	List<Adaptor> adaptors;

	@BeforeTest
	public void setUp() {
		initializeMocks();
	}
	
	private void initializeMocks() {
		adapterCoreService = mock(AdapterCoreService.class);
		
		adaptors = new ArrayList<>();
		for(int i = 0; i <= DATA_SHARD_COUNT; i++) {
			adaptors.add(mock(Adaptor.class));
		}
		
		when(adapterCoreService.getAdapters()).thenReturn(adaptors);
	}
	
	private void initializeBuffer(byte[] buffer, int raidChunkSize) {
		for(int i = 0; i < DATA_SHARD_COUNT; i++) {
			for(int j = i * raidChunkSize; j < (i + 1) * raidChunkSize; j++) {
				if(j < buffer.length) {
					buffer[j] = (byte)(i + 1);
				}
			}
		}
	}
	
	private void verifyRaidChunkSize(PhysicalChunk physicalChunk, int raidChunkSize) {
		int expectedChunkSize = calculateExpectedRaidChunkSize(physicalChunk);
		
		Assert.assertEquals(expectedChunkSize, raidChunkSize);
	}
	
	private int calculateExpectedRaidChunkSize(PhysicalChunk physicalChunk) {
		int expectedChunkSize = (int) physicalChunk.getSize() / DATA_SHARD_COUNT;
		if(physicalChunk.getSize() % DATA_SHARD_COUNT != 0) {
			expectedChunkSize++;
		}
		
		return expectedChunkSize;
	}
	
	private void verifyRaidShards(PhysicalChunk physicalChunk, byte[][] shards) {
		byte expectedNormalParity = calculateNormalParityResult();
		byte expectedEmptyLastDataShardParity = calculateEmptyLastDataShardParityResult();
		int expectedRaidChunkSize = calculateExpectedRaidChunkSize(physicalChunk);
		
		int totalDataChecked = 0;
		for(int i = 0; i < DATA_SHARD_COUNT; i++) {
			for(int j = 0; j < expectedRaidChunkSize; j++) {
				if(totalDataChecked >= physicalChunk.getSize()) {
					Assert.assertEquals(shards[i][j], (byte)0);
				}
				else {
					Assert.assertEquals(shards[i][j], (byte)(i+1));
				}
				
				totalDataChecked++;
			}
		}
		
		long totalBytes = expectedRaidChunkSize * DATA_SHARD_COUNT;
		long emptyBytesInLastDataShard = totalBytes - physicalChunk.getSize();
		for(int i = 0; i < expectedRaidChunkSize; i++) {
			if(i < (expectedRaidChunkSize - emptyBytesInLastDataShard)) {
				Assert.assertEquals(shards[DATA_SHARD_COUNT][i], expectedNormalParity);
			}
			else {
				Assert.assertEquals(shards[DATA_SHARD_COUNT][i], expectedEmptyLastDataShardParity);
			}
		}
	}
	
	private byte calculateParityResult(byte[][] shards) {
		ReedSolomon reedSolomon = ReedSolomon.create(DATA_SHARD_COUNT, PARITY_SHARD_COUNT);
		reedSolomon.encodeParity(shards, 0, 1);
		return shards[shards.length - 1][0];
	}
	
	private byte calculateNormalParityResult() {
		byte[][] shards = new byte[DATA_SHARD_COUNT + PARITY_SHARD_COUNT][1];
		for(int i = 0; i < DATA_SHARD_COUNT; i++) {
			shards[i][0] = (byte)(i + 1);
		}
		
		return calculateParityResult(shards);
	}
	
	private byte calculateEmptyLastDataShardParityResult() {
		byte[][] shards = new byte[DATA_SHARD_COUNT + PARITY_SHARD_COUNT][1];
		for(int i = 0; i < DATA_SHARD_COUNT - 1; i++) {
			shards[i][0] = (byte)(i + 1);
		}
		
		shards[DATA_SHARD_COUNT - 1][0] = (byte)(0);
		
		return calculateParityResult(shards);
	}
	
	@Test
	public void calculateRaidChunkSize_ShouldReturnCorrectSize() {
		ReedSolomonRaid5Service service = new ReedSolomonRaid5Service();
		service.setAdapterCoreService(adapterCoreService);
		service.setParityShardCount(PARITY_SHARD_COUNT);
		
		for(int i = 0; i < DATA_SHARD_COUNT; i++) {
			PhysicalChunk physicalChunk = new PhysicalChunk();
			physicalChunk.setSize(1024 * DATA_SHARD_COUNT + i);
			verifyRaidChunkSize(physicalChunk, service.calculateRaidChunkSize(physicalChunk));
		}
	}
	
	@Test
	public void generateRaidShards_ShouldGenerateCorrectRaidedShards() {
		int dataShardCount = adaptors.size() - PARITY_SHARD_COUNT;
		ReedSolomonRaid5Service service = new ReedSolomonRaid5Service();
		service.setAdapterCoreService(adapterCoreService);
		service.setParityShardCount(PARITY_SHARD_COUNT);
		
		for(int i = 0; i < dataShardCount; i++) { //TODO loop over all possible values
			PhysicalChunk physicalChunk = new PhysicalChunk();
			physicalChunk.setSize(1024 * dataShardCount + i);
			int raidChunkSize = calculateExpectedRaidChunkSize(physicalChunk);
			
			byte[] inputBuffer = new byte[(int)physicalChunk.getSize()];
			initializeBuffer(inputBuffer, raidChunkSize);
			ByteArrayInputStream stream = new ByteArrayInputStream(inputBuffer);
			
			byte[][] result = service.generateRaidShards(stream, physicalChunk);
			verifyRaidShards(physicalChunk, result);
		}
	}
}
