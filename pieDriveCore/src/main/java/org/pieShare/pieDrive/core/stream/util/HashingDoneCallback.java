/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.stream.util;

import org.pieShare.pieDrive.core.model.AdapterId;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public interface HashingDoneCallback<T extends ICallbackId> {
	void hashingDone(T id, byte[] hash);
}
