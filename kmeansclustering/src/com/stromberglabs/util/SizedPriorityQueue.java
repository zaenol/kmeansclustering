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
 * 
 * A priority queue that maintains a list of the top N values that are
 * added to the list. All the other values are dropped. It's useful for
 * tracking the top N scores of a result set.
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
	
	public SizedPriorityQueue(int size, boolean getLowest) {
		mSize = size;
		mGetLowest = getLowest;
		mList = new LinkedList<T>();
		mPriorities = new LinkedList<Double>();
	}
	
	public SizedPriorityQueue(int size, boolean getLowest, Comparator<T> comparator) {
		this(size,getLowest);
		mComparator = comparator;
	}
	
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
	
	public T pop(){
		if ( mPriorities.size() > 0 )
			mPriorities.pop();
		return mList.pop();
	}
	
	public T poll(){
		return mList.peek();
	}
	
	public int size(){
		return mList.size();
	}
	
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
