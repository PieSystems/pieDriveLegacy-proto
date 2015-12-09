package org.pieShare.pieDrive.core.task.config;

import org.pieShare.pieDrive.core.database.DatabaseFactory;
import org.pieShare.pieDrive.core.springConfig.CoreAppConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class CoreTestConfig extends CoreAppConfig {
	@Bean
	@Lazy
	@Override
	public DatabaseFactory databaseFactory() {
		return new DatabaseFactory();
	}
}
