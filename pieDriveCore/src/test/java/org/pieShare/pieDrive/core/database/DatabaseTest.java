/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database;

import org.pieShare.pieDrive.core.database.Database;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.pieShare.pieDrive.core.database.entities.FileEntity;
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
}
