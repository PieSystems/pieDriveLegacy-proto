/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database;

import java.util.ArrayList;
import java.util.List;
import org.pieShare.pieDrive.core.database.Database;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.pieShare.pieDrive.core.database.entities.FileEntity;
import org.pieShare.pieDrive.core.database.entities.PieRaidFileEntity;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.springConfig.CoreAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.util.Assert;

/**
 *
 * @author richy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreAppConfig.class)
public class DatabaseTest {

    @Autowired
    private Database database;

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
        adChunk1.setHash("Hash1".getBytes());
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
        
        database.removePieRadFile(file1);
        
    }
}
