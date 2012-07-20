/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

/**
 * Counter for for loops defined by the template
 */

public class ForLoopCounter {
  	private int loopCount = 0;
  	private int collectionSize;
  	private ForLoopCounter parent;
  	
 	public ForLoopCounter(int size, Object parent) {
 		collectionSize = size;
 		if ( null != parent) {
 			if (parent instanceof ForLoopCounter) {
 				this.parent = (ForLoopCounter)parent;
 			} else {
 				System.err.println("Looks like reserved key forloop is being set");
 			}
 		}
 	}
 	
 	public void increment() {
 		loopCount++;
 	}
 	
 	public int getCounter() {
 		return loopCount + 1;
 	}
 	
 	public int getCounter0() {
 		return loopCount;
 	}
 	
 	public int getRevcounter() {
 		return collectionSize + 1 - loopCount;
 	}
 	
 	public int getRevcounter0() {
 		return collectionSize - loopCount;	
 	}
 	
 	public boolean getFirst() {
 		return loopCount == 0;
 	}
 	
 	public boolean getLast() {
 		return (loopCount + 1) == collectionSize;
 	}
 	
 	public ForLoopCounter getParent() {
 		return parent;
 	}
}
