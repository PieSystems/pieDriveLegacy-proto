/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.pieShare.pieDrive.core.IntegrationTestBase;
import org.pieShare.pieDrive.core.database.entities.FileEntity;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.springConfig.CoreAppConfig;
import org.pieShare.pieDrive.core.task.config.CoreTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 *
 * @author richy
 */
@DirtiesContext
@ContextConfiguration(classes = CoreTestConfig.class)
public class DatabaseTest extends IntegrationTestBase {

    private String databaseTestFile = "databaseTest.odb";

    @Autowired
    private Database database;
    @Autowired
    private DatabaseFactory databaseFactory;

    @BeforeClass
    public void beforeTest() throws Exception {
		super.setUpIt();
		File dbFile = new File(super.integrationTestFolder, databaseTestFile);
        databaseFactory.setDatabaseName(dbFile.getPath());
        databaseFactory.init();
    }

    @Test
    public void testDatabse() {

        FileEntity entity = new FileEntity();
        entity.setFileName("TestFile");

        database.persist(entity);

        FileEntity entity2 = database.findFileById(entity.getId());

        Assert.isTrue(entity.getId().equals(entity2.getId()));
        Assert.isTrue(entity.getFileName().equals(entity2.getFileName()));

        database.remove(entity);
    }

    @Test
    public void TestPieRaidFilePersistAndFind() {

        PieRaidFile file1 = new PieRaidFile();

        PhysicalChunk phChunk1 = new PhysicalChunk();

        AdapterChunk adChunk1 = new AdapterChunk();
        AdapterId id1 = new AdapterId();
        id1.setId("AdapterID1");
        adChunk1.setAdapterId(id1);
        adChunk1.setHash(null);
        adChunk1.setUuid("UUID1");

        AdapterChunk adChunk2 = new AdapterChunk();
        AdapterId id2 = new AdapterId();
        id2.setId("AdapterID2");
        adChunk2.setAdapterId(id2);
        adChunk2.setHash("Hash2".getBytes());
        adChunk2.setUuid("UUID2");

        phChunk1.addAdapterChunk(adChunk1);
        phChunk1.addAdapterChunk(adChunk2);
        phChunk1.setOffset(10);
        phChunk1.setSize(10);

        PhysicalChunk phChunk2 = new PhysicalChunk();

        AdapterChunk adChunk3 = new AdapterChunk();
        AdapterId id3 = new AdapterId();
        id3.setId("AdapterID3");
        adChunk3.setAdapterId(id3);
        adChunk3.setHash("Hash3".getBytes());
        adChunk3.setUuid("UUID3");

        AdapterChunk adChunk4 = new AdapterChunk();
        AdapterId id4 = new AdapterId();
        id4.setId("AdapterID4");
        adChunk4.setAdapterId(id4);
        adChunk4.setHash("Hash4".getBytes());
        adChunk4.setUuid("UUID4");

        phChunk2.addAdapterChunk(adChunk3);
        phChunk2.addAdapterChunk(adChunk4);
        phChunk2.setOffset(20);
        phChunk2.setSize(20);

        List<PhysicalChunk> chunks = new ArrayList<>();
        chunks.add(phChunk1);
        chunks.add(phChunk2);

        file1.setChunks(chunks);
        file1.setFileName("FileName1");
        file1.setLastModified(500);
        file1.setRelativeFilePath("Relative");

        database.persistPieRaidFile(file1);

        PieRaidFile fromDB = database.findPieRaidFileByName("FileName1");
        Assert.notNull(fromDB);

        for (PhysicalChunk physicalChunk : fromDB.getChunks()) {
            Assert.isNull(physicalChunk.getHash());
            Assert.notNull(physicalChunk);

            for (AdapterChunk adapterChunk : physicalChunk.getChunks().values()) {
                Assert.notNull(physicalChunk);

                if (adapterChunk.getUuid().equals("UUID1")) {
                    Assert.isNull(adapterChunk.getHash());
                } else {
                    Assert.notNull(adapterChunk.getHash());
                }
            }
        }

        adChunk1.setHash("VALUE".getBytes());
        database.updateAdaptorChunk(adChunk1);

        PieRaidFile fromDBNew = database.findPieRaidFileByName("FileName1");
        Assert.notNull(fromDB);

        for (PhysicalChunk physicalChunk : fromDBNew.getChunks()) {
            Assert.notNull(physicalChunk);

            for (AdapterChunk adapterChunk : physicalChunk.getChunks().values()) {
                Assert.notNull(physicalChunk);

                if (adapterChunk.getUuid().equals("UUID1")) {
                    Assert.notNull(adapterChunk.getHash());
                    org.junit.Assert.assertArrayEquals(adapterChunk.getHash(), "VALUE".getBytes());
                } else {
                    Assert.notNull(adapterChunk.getHash());
                }
            }
        }

        List<PieRaidFile> fromDBList = database.findAllPieRaidFiles();
        Assert.notNull(fromDBList);

        phChunk1.setHash("TEST".getBytes());
        phChunk2.setHash("TEST2".getBytes());

        database.updatePhysicalChunk(phChunk1);
        database.updatePhysicalChunk(phChunk2);

        PieRaidFile fromDBNewPhysical = database.findPieRaidFileByName("FileName1");
        Assert.notNull(fromDBNewPhysical);

        for (PhysicalChunk physicalChunk : fromDBNewPhysical.getChunks()) {
            Assert.notNull(physicalChunk.getHash());
        }

        database.removePieRadFile(file1);

    }
}
