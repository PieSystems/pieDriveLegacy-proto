/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.springConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import javax.inject.Provider;
import javax.persistence.spi.PersistenceProvider;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.adapter.box.BoxAdapter;
import org.pieShare.pieDrive.adapter.box.BoxAuthentication;
import org.pieShare.pieDrive.adapter.dropbox.DropboxAdapter;
import org.pieShare.pieDrive.adapter.s3.S3Adapter;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCoreService;
import org.pieShare.pieDrive.core.Raid5Service;
import org.pieShare.pieDrive.core.ReedSolomonRaid5Service;
import org.pieShare.pieDrive.core.SimpleAdapterCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.database.repository.*;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.task.DeleteRaidFileTask;
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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.pieShare.pieDrive.core.task.UploadRaid5FileTask;

/**
 *
 * @author richy
 */
@Configuration
@EnableJpaRepositories("org.pieShare.pieDrive.core.database.repository")
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
    public EmbeddedDatabase dataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        return builder.setType(EmbeddedDatabaseType.HSQL).build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        AbstractJpaVendorAdapter vendorAdapter = new AbstractJpaVendorAdapter() {
            @Override
            public PersistenceProvider getPersistenceProvider() {
                return new com.objectdb.jpa.Provider();
            }

            @Override
            public Map<String, ?> getJpaPropertyMap() {
                return Collections.singletonMap(
                        "javax.persistence.jdbc.url", "database.odb");
            }
        };
        //HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("org.pieShare.pieDrive.core", "org.pieShare.pieDrive.core.database.entities", "org.pieShare.pieDrive.core.database.repository");
        factory.setDataSource(dataSource());
        factory.afterPropertiesSet();

        return factory;
    }

    @Bean

    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return txManager;
    }

    @Bean
    @Lazy
    public Database database() {
        Database db = new Database();
        return db;
    }

    @Bean
    @Lazy
    public PieRaidFileEntityRepositoryCustom pieRaidFileRepositoryCustom() {
        return new PieRaidFileEntityRepositoryImpl();
    }

    @Bean
    @Lazy
    public VolumeEntityRepositoryCustom volumeEntityRepositoryCustom() {
        return new VolumeEntityRepositoryImpl();
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
	public Raid5Service raid5Service() {
		ReedSolomonRaid5Service service = new ReedSolomonRaid5Service();
		service.setAdapterCoreService(simpleAdapterCoreService());
		service.setParityShardCount(1);
		return service;
	}

    @Bean
    @Lazy
    public Adaptor dropboxAdapter() {
        DropboxAdapter adapter = new DropboxAdapter();
        return adapter;
    }

    @Bean
    @Lazy
    public Adaptor boxAdapter() {
        BoxAdapter box = new BoxAdapter();
        return box;
    }

    @Bean
    @Lazy
    public BoxAuthentication boxAuthentication() {
        return new BoxAuthentication();
    }

    @Bean
    @Lazy
    public Adaptor s3Adapter() {
        S3Adapter s3 = new S3Adapter();
        return s3;
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
    public UploadRaid5FileTask uploadRaid5FileTask() {
        UploadRaid5FileTask task = new UploadRaid5FileTask();
        task.setAdapterCoreService(this.simpleAdapterCoreService());
        task.setDatabase(this.database());
        task.setDriveCoreService(this.pieDriveCoreService());
		task.setRaid5Service(raid5Service());
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
        task.setTask(this.integrityCheckTask());
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
        task.setUploadChunkTaskProvider(uploadChunkTaskProvider);
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
    public ForkJoinPool executorService() {
        ForkJoinPool pool = new ForkJoinPool(8);
		return pool;
    }

    @Bean
    @Lazy
    public PieExecutorTaskFactory executorFactory() {
        PieExecutorTaskFactory fac = new PieExecutorTaskFactory();
        return fac;
    }

    @Bean
    @Lazy
    @Scope("prototype")
    public DeleteRaidFileTask deleteRaidFileTask() {
        DeleteRaidFileTask task = new DeleteRaidFileTask();
        task.setDatabase(this.database());
        task.setAdapterCoreService(this.simpleAdapterCoreService());
        return task;
    }
	
	@Bean
	@Lazy
	public VersionedPieRaidFileEntityRepositoryCustom versionedPieRaidFileEntityRepository() {
		return new VersionedPieRaidFileEntityRepositoryImpl();
	}
}
