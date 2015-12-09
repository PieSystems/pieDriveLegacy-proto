/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.api;

import javax.persistence.EntityManager;

/**
 *
 * @author richy
 */
public interface IDatabaseFactory {
    EntityManager getEntityManger(Class clazz);
    void init();
    void setDatabaseName(String databaseName);
    
}
