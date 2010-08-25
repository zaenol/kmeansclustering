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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.stromberglabs.cluster.Clusterable;

public class KMeansClusteringTest {
	
	private List<Clusterable> mPoints;
	
	private List<Clusterable> mLotsOfPoints;
	
	@Before
	public void setUp() throws Exception {
		mPoints = new ArrayList<Clusterable>();
		
		//A bunch of points centered around 4,5
		Clusterable point = new Point(4,5);
		mPoints.add(point);
		point = new Point(4.1F,5.1F);
		mPoints.add(point);
		point = new Point(4.2F,5.5F);
		mPoints.add(point);
		point = new Point(3.9F,4.9F);
		mPoints.add(point);
		point = new Point(4.1F,5.0F);
		mPoints.add(point);
		point = new Point(4.2F,5.1F);
		mPoints.add(point);
		
		//A bunch of points centered around 10,10
		point = new Point(10,10);
		mPoints.add(point);
		point = new Point(10.1F,10.1F);
		mPoints.add(point);
		point = new Point(10.2F,10.5F);
		mPoints.add(point);
		point = new Point(9.9F,9.9F);
		mPoints.add(point);
		point = new Point(10.1F,10.0F);
		mPoints.add(point);
		point = new Point(10.2F,10.1F);
		mPoints.add(point);
		
		
		//A bunch of points centered around 1,1
		point = new Point(1,1);
		mPoints.add(point);
		point = new Point(1.1F,1.1F);
		mPoints.add(point);
		point = new Point(1.2F,1.5F);
		mPoints.add(point);
		point = new Point(0.9F,0.9F);
		mPoints.add(point);
		point = new Point(1.1F,1.0F);
		mPoints.add(point);
		point = new Point(1.2F,1.1F);
		mPoints.add(point);
		
		Random random = new Random(1);
		int numPoints = 100000;
		mLotsOfPoints = new ArrayList<Clusterable>(numPoints);
		for ( int i = 0; i < numPoints; i++ ){
			int x = random.nextInt(1000) - 500;
			int y = random.nextInt(1000) - 500;
			mLotsOfPoints.add(new Point(x,y));
		}
	}
	
	@Test
	public void testKMeansClustering() throws Exception {
		KClusterer clusterer = new KMeansClusterer();
		Cluster clusters[] = clusterer.cluster(mPoints,3);
		for ( Cluster cluster : clusters ){
			for ( Clusterable item : cluster.getItems() ){
				assertTrue(ClusterUtils.getEuclideanDistance(item,cluster) < 0.5);
			}
		}
	}
	
	@Test
	public void testElkanKMeansClustering() throws Exception {
		ElkanKMeansClusterer clusterer = new ElkanKMeansClusterer();
		Cluster clusters[] = clusterer.cluster(mPoints,3);
		for ( Cluster cluster : clusters ){
			for ( Clusterable item : cluster.getItems() ){
				assertTrue(ClusterUtils.getEuclideanDistance(item,cluster) < 0.5);
			}
		}
	}
	
	@Test
	public void testElkanAndBasicEquivalency() throws Exception {
		int numClusters = 10;
		ElkanKMeansClusterer clusterer = new ElkanKMeansClusterer();
		Cluster[] clusters = clusterer.cluster(mLotsOfPoints,numClusters);

		KMeansClusterer clusterer2 = new KMeansClusterer();
		Cluster[] clusters2 = clusterer2.cluster(mLotsOfPoints,numClusters);
		
		for ( int i = 0; i < clusters.length; i++ ){
			Cluster c = clusters[i];
			double closest = Double.MAX_VALUE;
			int idx = -1;
			for ( int j = 0; j < clusters2.length; j++ ){
				Cluster c2 = clusters2[j];
				double dist = ClusterUtils.getEuclideanDistance(c,c2);
				if ( dist < closest ){
					closest = dist;
					idx = j;
				}
			}
			assertEquals(i,idx);
			assertEquals(closest,0.0,0.0);
		}
	}
	
	@Test
	public void testKMeansTreeClusterer() throws Exception {
		KClusterer clusterer = new KMeansTreeClusterer();
		Cluster clusters[] = clusterer.cluster(mPoints,3);
		for ( Cluster cluster : clusters ){
			for ( Clusterable item : cluster.getItems() ){
				assertTrue(ClusterUtils.getEuclideanDistance(item,cluster) < 0.5);
			}
		}
	}
	
	@Test
	public void testTreeAndBasicEquivalency() throws Exception {
		int numClusters = 10;
		ElkanKMeansClusterer clusterer = new ElkanKMeansClusterer();
		Cluster[] clusters = clusterer.cluster(mLotsOfPoints,numClusters);

		KClusterer clusterer2 = new KMeansTreeClusterer();
		Cluster[] clusters2 = clusterer2.cluster(mLotsOfPoints,numClusters);
		
		for ( int i = 0; i < clusters.length; i++ ){
			Cluster c = clusters[i];
			double closest = Double.MAX_VALUE;
			int idx = -1;
			for ( int j = 0; j < clusters2.length; j++ ){
				Cluster c2 = clusters2[j];
				double dist = ClusterUtils.getEuclideanDistance(c,c2);
				if ( dist < closest ){
					closest = dist;
					idx = j;
				}
			}
			assertEquals(i,idx);
			//Why not check that they're exactly the same you ask?
			//Well I'll tell you why
			//Because this version only returns an approximately similar version of the tree
			//So it's likely to be off by a bit, but it shouldn't be so far off that they're
			//radically different clusters
			//assertEquals(closest,0.0,0.0);
		}
	}
	
	@Test
	public void testKMeansForestClusterer() throws Exception {
		KClusterer clusterer = new KMeansForestClusterer();
		Cluster clusters[] = clusterer.cluster(mPoints,3);
		for ( Cluster cluster : clusters ){
			for ( Clusterable item : cluster.getItems() ){
				assertTrue(ClusterUtils.getEuclideanDistance(item,cluster) < 0.5);
			}
		}
	}
	
	@Test
	public void testForestAndBasicEquivalency() throws Exception {
		int numClusters = 10;
		ElkanKMeansClusterer clusterer = new ElkanKMeansClusterer();
		Cluster[] clusters = clusterer.cluster(mLotsOfPoints,numClusters);

		KClusterer clusterer2 = new KMeansForestClusterer();
		Cluster[] clusters2 = clusterer2.cluster(mLotsOfPoints,numClusters);
		
		for ( int i = 0; i < clusters.length; i++ ){
			Cluster c = clusters[i];
			double closest = Double.MAX_VALUE;
			int idx = -1;
			for ( int j = 0; j < clusters2.length; j++ ){
				Cluster c2 = clusters2[j];
				double dist = ClusterUtils.getEuclideanDistance(c,c2);
				if ( dist < closest ){
					closest = dist;
					idx = j;
				}
			}
			assertEquals(i,idx);
			//Why not check that they're exactly the same you ask?
			//Well I'll tell you why
			//Because this version only returns an approximately similar version of the tree
			//So it's likely to be off by a bit, but it shouldn't be so far off that they're
			//radically different clusters
			//assertEquals(closest,0.0,0.0);
		}
	}
}
