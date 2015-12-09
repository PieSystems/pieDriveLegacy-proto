/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database;

import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.pieShare.pieDrive.core.database.api.IDatabaseFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author richy
 */
public class DatabaseFactory implements IDatabaseFactory {

    private EntityManagerFactory emf;
    private HashMap<Class, EntityManager> entityManagers;

    public DatabaseFactory()
    {
        this.entityManagers = new HashMap<>();
    }
    
    @PostConstruct
    public void init() {
        emf = Persistence.createEntityManagerFactory("database.odb");
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
}
