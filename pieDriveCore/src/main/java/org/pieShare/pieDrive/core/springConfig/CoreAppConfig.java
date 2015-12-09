/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.springConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataSource;
import javax.inject.Provider;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCoreService;
import org.pieShare.pieDrive.core.SimpleAdapterCoreService;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.database.DatabaseFactory;
import org.pieShare.pieDrive.core.database.repository.PieRaidFileRepositoryCustom;
import org.pieShare.pieDrive.core.database.repository.PieRaidFileRepositoryImpl;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.task.DownloadChunkTask;
import org.pieShare.pieDrive.core.task.DownloadRaidFileTask;
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
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author richy
 */
@EnableTransactionManagement
@Configuration
@EnableJpaRepositories("org.pieShare.pieDrive.core.database.repository")
public class CoreAppConfig {

    @Autowired
    private Provider<DownloadChunkTask> downloadChunkProvider;
    @Autowired
    private Provider<AdapterChunk> adapterChunkProvider;
    @Autowired
    private Provider<UploadChunkTask> uploadChunkTaskProvider;

    @Bean
    public EmbeddedDatabase dataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        return builder.setType(EmbeddedDatabaseType.HSQL).build();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        
        JpaVendorAdapter vendorAdapter = new AbstractJpaVendorAdapter() {
            @Override
            public PersistenceProvider getPersistenceProvider() {
                return new com.objectdb.jpa.Provider();
            }
 
            @Override
            public Map<String,?> getJpaPropertyMap() {
                return Collections.singletonMap(
                    "javax.persistence.jdbc.url", "database.odb");
            }
        };
        
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("org.pieShare.pieDrive.core.database.entities");
        factory.setDataSource(dataSource());
        factory.afterPropertiesSet();

        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory());
        return txManager;
    }

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
        fac.setEmf(entityManagerFactory());
        return fac;
    }

    @Bean
    @Lazy
    public PieRaidFileRepositoryCustom pieRaidFileRepositoryCustom()
    {
        return new PieRaidFileRepositoryImpl();
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
