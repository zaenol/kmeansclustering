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

package com.stromberglabs.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.stromberglabs.cluster.checker.ClusterChecker;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.tree.ClusterKDForest;

public class KMeansForestClusterer extends AbstractKClusterer {
	private int mNumTrees = 8;
	
	public KMeansForestClusterer(){
		super();
	}
	
	public KMeansForestClusterer(int numTrees){
		super();
		mNumTrees = numTrees;
	}
	
	public KMeansForestClusterer(int numTrees, ClusterChecker checker){
		super(checker);
		mNumTrees = numTrees;
	}
	
	public KMeansForestClusterer(int numTrees, ClusterChecker checker, int maxRecluster){
		super(checker,maxRecluster);
		mNumTrees = numTrees;
	}
	
	/**
	 * Use the mode of 8 k-d trees to determine which value goes into which cluster
	 * @param clusters
	 * @param values
	 */
	protected Cluster[] assignClusters(final Cluster[] clusters,final List<Clusterable> values){
		ClusterKDForest forest = new ClusterKDForest(clusters,mNumTrees,10);
		for ( Clusterable item : values ){
			Clusterable closest = forest.findClosest(item);
			((Cluster)closest).addItem(item);
		}
		return clusters;
	}
	
	protected Cluster[] getNewClusters(Cluster[] clusters){
		for ( int i = 0; i < clusters.length; i++ ){
			if ( clusters[i].getItems().size() > 0 )
				clusters[i] = new Cluster(clusters[i].getClusterMean(),clusters[i].getId());
		}
		return clusters;
	}
	
	public static void main(String args[]){
		Random random = new Random(System.currentTimeMillis());
		int numPoints = 10000;
		List<Clusterable> points = new ArrayList<Clusterable>(numPoints);
		for ( int i = 0; i < numPoints; i++ ){
			int x = random.nextInt(1000) - 500;
			int y = random.nextInt(1000) - 500;
			points.add(new Point((float)x,(float)y));
		}
		KClusterer clusterer = new KMeansForestClusterer();
		Cluster[] clusters = clusterer.cluster(points,10);
		for ( Cluster c : clusters ){
			System.out.println(c.getId() + "," + c.getItems().size());
		}
	}
}
