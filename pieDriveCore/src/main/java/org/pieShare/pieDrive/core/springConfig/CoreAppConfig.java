/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.springConfig;

import java.util.HashMap;
import java.util.Map;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCore;
import org.pieShare.pieDrive.core.PieDriveCoreService;
import org.pieShare.pieDrive.core.SimpleAdapterCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.database.DatabaseFactory;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author richy
 */
@Configuration
public class CoreAppConfig {

    @Bean
    @Lazy
    public Database database() {
        return new Database();
    }
    
    @Bean
    @Lazy
    public DatabaseFactory databaseFactory() {
        return new DatabaseFactory();
    }
	
	@Bean
	@Lazy
	@Scope("prototype")
	public AdapterId adapterId() {
		return new AdapterId();
	}
	
	@Bean
	@Lazy
	@Scope("prototype")
	public Map hashMap() {
		return new HashMap();
	}

	@Bean
	@Lazy
	public AdapterCoreService simpleAdapterCoreService() {
		SimpleAdapterCoreService service = new SimpleAdapterCoreService();
		service.setAdapters(this.hashMap());
		
		//register all used adapters
		service.registerAdapter(this.adapterId().setId("dropbox"), dropboxAdapter());
		service.registerAdapter(this.adapterId().setId("box"), boxAdapter());
		service.registerAdapter(this.adapterId().setId("s3"), s3Adapter());
		return service;
	}
	
	@Bean
	@Lazy
	public PieDriveCore pieDriveCore() {
		PieDriveCoreService service = new PieDriveCoreService();
		service.setChunkSize(5 * 1024 * 1024); // 5MiB
		
		return service;
	}
	
	public Adaptor dropboxAdapter() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
	public Adaptor boxAdapter() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
	public Adaptor s3Adapter() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
}
