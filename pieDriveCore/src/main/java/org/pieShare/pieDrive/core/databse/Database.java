/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.databse;

import javax.persistence.EntityManager;
import org.pieShare.pieDrive.core.databse.api.IDatabaseFactory;
import org.pieShare.pieDrive.core.databse.entities.FileEntity;
import org.pieShare.pieDrive.core.databse.entities.IBaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author richy
 */
public class Database {

    @Autowired
    private IDatabaseFactory databseFactory;

    public void persist(IBaseEntity entity) {
        EntityManager em = databseFactory.getEntityManger(entity.getClass());
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
    }

    public FileEntity findFileById(long id) {
        EntityManager em = databseFactory.getEntityManger(FileEntity.class);
        return em.find(FileEntity.class, id);
    }

    public void remove(IBaseEntity entity) {
        EntityManager em = databseFactory.getEntityManger(entity.getClass());
        em.getTransaction().begin();
        em.remove(entity);
        em.getTransaction().commit();
    }

}
