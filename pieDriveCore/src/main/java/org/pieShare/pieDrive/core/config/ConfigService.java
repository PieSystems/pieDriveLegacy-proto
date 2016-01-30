/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pieShare.pieDrive.core.config;

import java.util.ArrayList;
import java.util.List;


public class ConfigService {
	List<Config> configs;
	
	public ConfigService(){
		configs = new ArrayList<>();
	}
	
	public void addConfig(Config config){
		this.configs.add(config);
	}
	
	public void save(){
		for(Config config : configs){
			config.saveConfig();
		}
	}
}
