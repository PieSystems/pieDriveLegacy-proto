/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core;

import java.util.Collection;
import java.util.Map;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.model.AdapterId;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class SimpleAdapterCoreService implements AdapterCoreService {
	
	private Map<AdapterId, Adaptor> adapters;

	public void setAdapters(Map<AdapterId, Adaptor> adapters) {
		this.adapters = adapters;
	}

	@Override
	public Collection<Adaptor> getAdapters() {
		return this.adapters.values();
	}

	@Override
	public void registerAdapter(AdapterId id, Adaptor adapter) {
		//the SimpleAdapterCoreService is designed to be filled with adapters once at startup by the DI framework
		//therefor no synchroization is needed
		//when this changes to on the fly we need to sync
		adapters.put(id, adapter);
	}

	@Override
	public Adaptor getAdapter(AdapterId id) {
		return this.adapters.get(id);
	}

	@Override
	public Collection<AdapterId> getAdaptersKey() {
		return this.adapters.keySet();
	}
	
}
