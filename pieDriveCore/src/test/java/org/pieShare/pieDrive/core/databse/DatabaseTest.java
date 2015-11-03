/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.databse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.pieShare.pieDrive.core.databse.entities.FileEntity;
import org.pieShare.pieDrive.core.springConfig.CoreAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author richy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=CoreAppConfig.class)
public class DatabaseTest {
    
    @Autowired 
    private Database database;


    @Test
    public void testDatabse() {
    
        FileEntity entity = new FileEntity();
        entity.setFileName("TestFile");
        
        database.persist(entity);
    
       FileEntity entity2 = database.FindFirstFile(entity.getId());
    
    }
    
}
