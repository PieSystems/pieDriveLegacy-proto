/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class Factory<T> {
	
	Class<T> clazz;
	
	public Factory(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public T get(Object... objs) throws FactoryException {
		Class[] types = new Class[objs.length];
		
		for(int i = 0; i < objs.length; i++) {
			types[i] = objs[i].getClass();
		}
		
		try {
			return clazz.getConstructor(types).newInstance(objs);
		} catch (NoSuchMethodException 
				| SecurityException 
				| InstantiationException 
				| IllegalAccessException 
				| IllegalArgumentException 
				| InvocationTargetException ex) {
			throw new FactoryException("Could not create instance!", ex);
		}
	}
}
