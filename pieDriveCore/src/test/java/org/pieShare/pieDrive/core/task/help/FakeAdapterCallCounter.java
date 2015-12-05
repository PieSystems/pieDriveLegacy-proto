/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task.help;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class FakeAdapterCallCounter {
	private int counter;
	
	public FakeAdapterCallCounter(int init) {
		this.counter = init;
	}
	
	public void increment() {
		this.counter ++;
	}

	public int getCount() {
		return counter;
	}
}
