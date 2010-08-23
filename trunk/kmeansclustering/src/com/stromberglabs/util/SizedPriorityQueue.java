/*
Copyright (c) 2010, Andrew Stromberg
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither Andrew Stromberg nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Andrew Stromberg BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.stromberglabs.util;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * <pre>
 * A priority queue that maintains a list of the top N values that are
 * added to the list. All the other values are dropped. It's useful for
 * tracking the top N scores of a result set.
 * 
 * For instance, if you are processing scores, and you only want the top 3,
 * you could do something like:
 * <code>
 * SizedPriorityQueue<Integer> queue = new SizedPriorityQueue<Integer>(3,false);
 * for ( Score score : scores ){
 * 		queue.add(score.getId(),score.getValue());
 * }
 * </code>
 * at the end of the loop the queue will have an ordered list of the highest
 * score values.
 * </pre>
 * 
 * @author Andrew Stromberg
 *
 * @param <T>
 */
public class SizedPriorityQueue<T> {
	private int mSize;
	private boolean mGetLowest = true;
	private LinkedList<T> mList;
	private LinkedList<Double> mPriorities;
	private Comparator<T> mComparator;
	
	/**
	 * Creates a fixed size priority queue that only tracks N values
	 *  
	 * @param size - The maximum number of values to store
	 * @param getLowest - false means to track the highest N values, 
	 * 						true means to track the lowest N values
	 */
	public SizedPriorityQueue(int size, boolean getLowest) {
		mSize = size;
		mGetLowest = getLowest;
		mList = new LinkedList<T>();
		mPriorities = new LinkedList<Double>();
	}
	
	/**
	 * Creates a fixed size priority queue with an explicit comparator for the
	 * class that you want to track. This can be handy if the generic class you
	 * have doesn't implement {@link Comparable}
	 * 
	 * @param size - The maximum number of values to store
	 * @param getLowest - false means to track the highest N values, 
	 * 						true means to track the lowest N values
	 * @param comparator - Explicit comparator for the classyou are tracking
	 */
	public SizedPriorityQueue(int size, boolean getLowest, Comparator<T> comparator) {
		this(size,getLowest);
		mComparator = comparator;
	}
	
	/**
	 * Add a value to the current list of items, it will be inserted into the
	 * correct position in the list if it has a higher priority than the other
	 * items, otherwise it will be dropped
	 * 
	 * @param value
	 */
	public void add(T value){
		if ( mComparator == null ) throw new RuntimeException("Trying to use priority queue default add without comparator defined");
		int index = 0;
		for ( T val : mList ){
			//int comparison = val.compareTo(value);
			int comparison = mComparator.compare(val,value);
			if ( mGetLowest && comparison < 0 ) break;
			if ( !mGetLowest && comparison > 0 ) break;
			index++;
		}
		
		if ( index < mSize - 1)
			mList.add(index,value);
		
		if ( mList.size() > mSize ) mList.removeLast();
	}
	
	/**
	 * Add a value to the current list of items, it will be inserted into the
	 * correct position in the list if it has a higher priority than the other
	 * items, otherwise it will be dropped
	 * 
	 * @param value
	 */
	public void add(T value, double priority){
		int index = 0;

		for ( double val : mPriorities ){
			double comparison = priority - val;
			
			if ( mGetLowest && comparison < 0 ) break;
			if ( !mGetLowest && comparison > 0 ) break;
			index++;
		}
		
		if ( index < mSize - 1) {
			mList.add(index,value);
			mPriorities.add(index,priority);
		}
		
		if ( mList.size() > mSize ){
			mList.removeLast();
			mPriorities.removeLast();
		}
	}
	
	/**
	 * Like any ohter queue, it returns the top 
	 * @return
	 */
	public T pop(){
		if ( mPriorities.size() > 0 )
			mPriorities.pop();
		return mList.pop();
	}
	
	/**
	 * Just returns the top in the list, doesn't remove it
	 * 
	 * @return
	 */
	public T poll(){
		return mList.peek();
	}
	
	/**
	 * @return The size of current list
	 */
	public int size(){
		return mList.size();
	}
	
	/**
	 * Returns an ordered list of all of the scores currently held
	 * 
	 * @return
	 */
	public List<T> getAllScores(){
		return mList;
	}
	
	public static void main(String args[]){
		int numScores = 10;
		int numTopScores = 5;
		Random r = new Random(2);
		SizedPriorityQueue<Integer> queue = new SizedPriorityQueue<Integer>(numTopScores,false);
		for ( int i = 0; i < numScores; i++ ){
			double score = r.nextDouble();
			System.out.println("inserting score: " + score + ", number " + i);
			queue.add(i,score);
		}
		
		while ( queue.size() > 0 ){
			System.out.println(queue.pop());
		}
	}
}
