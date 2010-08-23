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

package com.stromberglabs.tree;

import java.util.HashMap;
import java.util.Map;

import com.stromberglabs.jopensurf.Clusterable;

/**
 * A class that will create a forest of KDTrees based on a set of Clusterable points
 * passed in at creation time. It will do an approximate nearest neighbor search on
 * each of the tree and take the mode of the tree's guessed for the best branch. 
 * 
 * @author Andrew
 *
 */
public class ClusterKDForest {
	private ClusterKDTree trees[];
	private int mMaxBins;
	
	/**
	 * 
	 * @param clusters
	 * @param numTrees
	 * @param maxBinsChecked
	 */
	public ClusterKDForest(Clusterable clusters[], int numTrees, int maxBinsChecked){
		trees = new ClusterKDTree[numTrees];
		for ( int i = 0; i < numTrees; i++ ){
			trees[i] = new ClusterKDTree(clusters,true);
		}
		mMaxBins = maxBinsChecked;
	}
	
	/**
	 * Choses the closest point in the tree through the following method:
	 * - Create N KD trees which split on random dimensions
	 * - Poll them for their approximate nearest neighbor checking M max bins
	 * - Whatever bin gets the most votes "wins"
	 * 
	 * This is NOT guaranteed to return the closest, but has a good chance for
	 * high dimensional data.
	 * 
	 * @param point
	 * @return
	 */
	public Clusterable findClosest(Clusterable point){
		Map<Clusterable,Integer> votes = new HashMap<Clusterable,Integer>();
		for ( int i = 0; i < trees.length; i++ ){
			Clusterable c = trees[i].restrictedNearestNeighbor(point,mMaxBins);
			if ( votes.containsKey(c) ){
				votes.put(c,votes.get(c)+1);
			} else {
				votes.put(c,1);
			}
		}
		int highVoteCount = 0;
		Clusterable highest = null;
		for ( Clusterable c : votes.keySet() ){
			if ( votes.get(c) > highVoteCount ){
				highVoteCount = votes.get(c);
				highest = c;
			}
		}
		return highest;
	}
}
