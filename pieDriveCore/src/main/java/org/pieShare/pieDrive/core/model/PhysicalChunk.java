/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.model;

import java.util.Map;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class PhysicalChunk {
	private Map<AdapterId, AdapterChunk> chunks;
	private long offset;
	private long size;
}
