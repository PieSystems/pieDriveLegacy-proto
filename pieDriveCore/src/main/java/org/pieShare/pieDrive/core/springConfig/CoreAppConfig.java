/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.springConfig;

import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.database.DatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author richy
 */
@Configuration
public class CoreAppConfig {

    @Bean
    public Database database() {
        return new Database();
    }
    
    
    @Bean
    public DatabaseFactory databaseFactory() {
        return new DatabaseFactory();
    }

}
