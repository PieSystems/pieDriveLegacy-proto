/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database;

import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.pieShare.pieDrive.core.database.api.IDatabaseFactory;

/**
 *
 * @author richy
 */
public class DatabaseFactory implements IDatabaseFactory {

    private EntityManagerFactory emf;
    private HashMap<Class, EntityManager> entityManagers;

    private String databaseName = "database.odb";
    
    public DatabaseFactory()
    {
        this.entityManagers = new HashMap<>();
    }
    
    @Override
    public void init() {
        
        if(emf != null && emf.isOpen())
        {
            emf.close();
        }
        
        emf = Persistence.createEntityManagerFactory(databaseName);
    }

    @Override
    public EntityManager getEntityManger(Class clazz) {

        if (entityManagers.containsKey(clazz)) {
            return entityManagers.get(clazz);
        }

        EntityManager manager = emf.createEntityManager();
        entityManagers.put(clazz, manager);
        return manager;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
  
}
