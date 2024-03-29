/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task.config;

import java.util.Collections;
import java.util.Map;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.springConfig.CoreAppConfig;
import org.pieShare.pieDrive.core.task.help.FakeAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
@Configuration
public class FakeAdapterCoreTestConfig extends CoreAppConfig {

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

	private FakeAdapter createFakeAdapter() {
		FakeAdapter adapter = new FakeAdapter();
		return adapter;
	}
}
