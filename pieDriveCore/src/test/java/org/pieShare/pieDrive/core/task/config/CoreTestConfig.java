/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task.config;

import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.database.DatabaseFactory;
import org.pieShare.pieDrive.core.springConfig.CoreAppConfig;
import org.pieShare.pieDrive.core.task.help.FakeAdapter;
import org.pieShare.pieDrive.core.task.help.FakeAdapterCallCounter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
@Configuration
public class CoreTestConfig extends CoreAppConfig {

	@Bean
	@Lazy
	@Override
	public DatabaseFactory databaseFactory() {
		return new DatabaseFactory();
	}
	
	@Bean
	@Lazy
	@Override
	public Adaptor s3Adapter() {
		return createFakeAdapter();
	}

	@Bean
	@Lazy
	@Override
	public Adaptor boxAdapter() {
		return createFakeAdapter();
	}

	@Bean
	@Lazy
	@Override
	public Adaptor dropboxAdapter() {
		return createFakeAdapter();
	}

	@Bean
	@Lazy
	public FakeAdapterCallCounter counter() {
		return new FakeAdapterCallCounter();
	}

	private FakeAdapter createFakeAdapter() {
		FakeAdapter adapter = new FakeAdapter();
		adapter.setCounter(counter());
		return adapter;
	}
	
}
