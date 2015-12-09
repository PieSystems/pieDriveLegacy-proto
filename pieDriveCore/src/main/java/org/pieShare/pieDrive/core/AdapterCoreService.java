/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core;

import java.util.Collection;
import org.pieShare.pieDrive.adapter.api.Adaptor;
import org.pieShare.pieDrive.core.model.AdapterId;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public interface AdapterCoreService {
	Collection<Adaptor> getAdapters();
	Collection<AdapterId> getAdaptersKey();
	Adaptor getAdapter(AdapterId id);
	void registerAdapter(AdapterId id, Adaptor adapter);
}
