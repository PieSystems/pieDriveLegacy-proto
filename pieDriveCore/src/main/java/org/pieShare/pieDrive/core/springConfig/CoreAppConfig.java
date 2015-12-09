/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.springConfig;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Provider;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCoreService;
import org.pieShare.pieDrive.core.SimpleAdapterCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.database.DatabaseFactory;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.task.DownloadChunkTask;
import org.pieShare.pieDrive.core.task.DownloadRaidFileTask;
import org.pieShare.pieDrive.core.task.IntegrityCheckTask;
import org.pieShare.pieDrive.core.task.UploadChunkTask;
import org.pieShare.pieDrive.core.task.UploadRaidFileTask;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorTaskFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author richy
 */
@Configuration
public class CoreAppConfig {

	@Autowired
	private Provider<DownloadChunkTask> downloadChunkProvider;
	@Autowired
	private Provider<AdapterChunk> adapterChunkProvider;
	@Autowired
	private Provider<UploadChunkTask> uploadChunkTaskProvider;
	@Autowired
	private Provider<IntegrityCheckTask> integrityCheckTaskProvider;

    @Bean
    @Lazy
    public Database database() {
        Database db = new Database();
		db.setDatabseFactory(this.databaseFactory());
		return db;
    }
    
    @Bean
    @Lazy
    public DatabaseFactory databaseFactory() {
        DatabaseFactory fac = new DatabaseFactory();
		fac.init();
		return fac;
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
	public PieDriveCoreService pieDriveCoreService() {
		PieDriveCoreService service = new PieDriveCoreService();
		service.setChunkSize(5 * 1024 * 1024); // 5MiB
		return service;
	}
	
	@Bean
	@Lazy
	public Adaptor dropboxAdapter() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
	@Bean
	@Lazy
	public Adaptor boxAdapter() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Bean
	@Lazy
	public Adaptor s3Adapter() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
	@Bean
	@Lazy
	@Scope("prototype")
	public UploadChunkTask uploadChunkTask() {
		UploadChunkTask task = new UploadChunkTask();
		task.setAdapterCoreService(this.simpleAdapterCoreService());
		task.setDatabase(this.database());
		return task;
	}
	
	@Bean
	@Lazy
	@Scope("prototype")
	public UploadRaidFileTask uploadRaidFileTask() {
		UploadRaidFileTask task = new UploadRaidFileTask();
		task.setAdapterCoreService(this.simpleAdapterCoreService());
		task.setExecutorService(this.executorService());
		task.setDatabase(this.database());
		task.setDriveCoreService(this.pieDriveCoreService());
		
		task.setAdapterChunkProvider(adapterChunkProvider);
		task.setUploadChunkTaskProvider(uploadChunkTaskProvider);
		return task;
	}
	
	@Bean
	@Lazy
	@Scope("prototype")
	public DownloadChunkTask downloadChunkTask() {
		DownloadChunkTask task = new DownloadChunkTask();
		task.setAdapterCoreService(this.simpleAdapterCoreService());
		return task;
	}
	
	@Bean
	@Lazy
	@Scope("prototype")
	public DownloadRaidFileTask downloadRaidFileTask() {
		DownloadRaidFileTask task = new DownloadRaidFileTask();
		task.setAdapterCoreService(this.simpleAdapterCoreService());
		task.setExecutorService(this.executorService());
		
		task.setDownloadChunkProvider(downloadChunkProvider);
		return task;
	}
	
	@Bean
	@Lazy
	@Scope("prototype")
	public IntegrityCheckTask integrityCheckTask() {
		IntegrityCheckTask task = new IntegrityCheckTask();
		task.setAdapterCoreService(this.simpleAdapterCoreService());
		
		return task;
	}
	
	@Bean
	@Lazy
	@Scope("prototype")
	public AdapterChunk adapterChunk() {
		AdapterChunk chunk = new AdapterChunk();
		return chunk;
	}
	
	@Bean
	@Lazy
	public PieExecutorService executorService() {
		PieExecutorService service = PieExecutorService.newCachedPieExecutorService();
		service.setExecutorFactory(this.executorFactory());
		return service;
	}

	@Bean
	@Lazy
	public PieExecutorTaskFactory executorFactory() {
		PieExecutorTaskFactory fac = new PieExecutorTaskFactory();
		return fac;
	}
}
