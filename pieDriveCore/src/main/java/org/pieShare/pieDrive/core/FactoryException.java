/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class FactoryException extends Exception {

	/**
	 * Creates a new instance of <code>FactoryException</code> without detail
	 * message.
	 */
	public FactoryException(String msg, Throwable th) {
		super(msg, th);
	}

	/**
	 * Constructs an instance of <code>FactoryException</code> with the
	 * specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public FactoryException(String msg) {
		super(msg);
	}
}
