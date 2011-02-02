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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.stromberglabs.cluster.Clusterable;

public class ElkanKMeansClusterer extends AbstractKClusterer {
	public static double DISTANCE_TOLERANCE = 0.005;
	public static double MAX_RECLUSTERING = 100;
	
	//This contain's a point's maximum distance from the center
	private Map<Integer,Double> mUx = new HashMap<Integer,Double>();
	
	//Map of current point's distance status
	private Map<Integer,Boolean> mRx = new HashMap<Integer, Boolean>(); 
	
	//Total hack to give each point a unique id
	private Map<Clusterable,Integer> mPointIds = new HashMap<Clusterable, Integer>();
	
	private Cluster[] newClusters;
	
	public ElkanKMeansClusterer(){
		super();
	}
	
	protected Cluster[] assignClusters(Cluster[] clusters, List<? extends Clusterable> values) {
		int numClusters = clusters.length;
		//transferring points into new clusters now so I don't have to keep track of what I've already
		//seen in the old clusters
		newClusters = new Cluster[numClusters];
		for ( int i = 0; i < numClusters; i++ ){ newClusters[i] = new Cluster(clusters[i].getLocation(),i); }
		
		//Computing d(c,c')
		int numDistances = (numClusters * (numClusters-1))/2;
		double distances[] = new double[numDistances];
		for ( int i = 0; i <= numClusters; i++ ){
			for ( int j = i+1; j < numClusters; j++ ){
				distances[getTriangleIndex(i+1,j+1,numClusters)] = ClusterUtils.getEuclideanDistance(clusters[i],clusters[j]);
			}
		}
		
		//Computing s(c) = 0.5 * min(d(c,c'))
		double sc[] = new double[numClusters];
		for ( int i = 0; i < numClusters; i++ ){
			double minDistance = Double.MAX_VALUE;
			for ( int j = 0; j < numClusters; j++ ){
				double dccprime = ClusterUtils.getEuclideanDistance(clusters[i],clusters[j]);
				if ( dccprime < minDistance ){
					minDistance = dccprime;
				}
			}
			sc[i] = 0.5 * minDistance;
		}
		
		Map<Integer,Boolean> assignedPoints = new HashMap<Integer, Boolean>();
		
		//Identify all points x such that u(x) <= s(c(x)), leave assigned to their current clusters
		for ( int i = 0; i < numClusters; i++ ){
			Cluster c = clusters[i];
			for ( Clusterable x : c.getItems() ){
				int pointId = mPointIds.get(x);
//				System.out.println(pointId);
				double ux = mUx.get(pointId);
				if ( ux <= sc[i] ){
					//skip any comparisons of this one, goes into it's current cluster
					assignedPoints.put(pointId,Boolean.TRUE);
					//System.out.println("point " + pointId + " is staying in cluster " + i + ", there are no closer clusters");
//					double minDistance = ClusterUtils.getEuclideanDistance(c,x);
//					for ( int l = 0; l < numClusters; l++ ){
//						Cluster cprime = mClusters[l];
//						if ( l == i ) continue;
//						double dccprime = ClusterUtils.getEuclideanDistance(c,cprime);
//						if ( dccprime < minDistance ){
//							System.out.println(i + " isn't closer than " + l + ", sc[i] = " + sc[i] + " ux[" + pointId + "] = " + ux + ", actual dist: " + minDistance + ", dxcprime = " + dccprime);
//							//System.out.println("Oh crap something is wrong, cluster " + l + " is actually closer");
//						}
//					}
				}
			}
		}
//		if ( assignedPoints.size() > 0 )
//			System.out.println("points assigned: " + assignedPoints.size());
		
		//Try to eliminate some more points
		for ( int i = 0; i < numClusters; i++ ){
			Cluster c = clusters[i];
			List<Clusterable> items = c.getItems();
			for ( Clusterable x : items ){
				//calculate d(x,c)
				int newCluster = i;//assume it's going to be in the same cluster
				double minDistance = 0;
				if ( assignedPoints.containsKey(mPointIds.get(x)) ){
					//newClusters[i].addItem(c);
					newCluster = i;
				} else {
					double dxc = ClusterUtils.getEuclideanDistance(x,c);
					mUx.put(mPointIds.get(x),dxc);
					minDistance = dxc;
					for ( int j = 0; j < numClusters; j++ ){
						Cluster cprime = clusters[j];
						if ( c.equals(cprime) ) continue;
						double dccprime = distances[getTriangleIndex(Math.min(i+1,j+1),Math.max(i+1,j+1),numClusters)];
						//System.out.println("dccprime = " + dccprime + ", actual: " + ClusterUtils.getEuclideanDistance(c,cprime));
	//					double dxc = mPointMaxDistance.get(mPointIds.get(x));
	//					if ( mRx.get(mPointIds.get(x)) ){
	//						dxc = ClusterUtils.getEuclideanDistance(x,c);
	//						updatePointMaxDistance(mPointIds.get(x),dxc);
	//						mRx.put(mPointIds.get(x),Boolean.FALSE);
	//					}
						if ( dccprime/2 < dxc ){//must do the distance calculation to determine if it should move into this cluster
							//System.out.println(j + " may be a better cluster");
							double dxcprime = ClusterUtils.getEuclideanDistance(x,cprime);
							//System.out.println(dxcprime + "," + minDistance);
							if ( dxcprime < minDistance ){
								//System.out.println("yup");
								minDistance = dxcprime;
								newCluster = j;
							}
	//					} else { 
	//						System.out.println("skipped calc");
						}
					}
				}
//				int closest = -1;
//				double cl = Double.MAX_VALUE;
//				for ( int l = 0; l < numClusters; l++ ){
//					double dist = ClusterUtils.getEuclideanDistance(x,mClusters[l]);
//					if ( dist < cl ){
//						cl = dist;
//						closest = l;
//					}
//				}
//				if ( closest != newCluster )
//					System.out.println("Clusterable " + mPointIds.get(x) + " is going from cluster: " + i + " to cluster " + newCluster + ", actual closest is " + closest);
				//System.out.println(mPointIds.get(x));
				mUx.put(mPointIds.get(x),minDistance);
				newClusters[newCluster].addItem(x);
			}
		}
		//update all of the ux's
		//clusters = newClusters;
		int count = 0;
		for ( Cluster c : newClusters ){
			for ( Clusterable x : c.getItems() ){
				//System.out.println(x.getLocation())
				updatePointMaxDistance(
						mPointIds.get(x),
						ClusterUtils.getEuclideanDistance(
								x.getLocation(),c.getClusterMean()
							)
						);
				count++;
				mRx.put(mPointIds.get(x),Boolean.TRUE);
			}
		}
		return newClusters;
	}

	/**
	* Calculates the initial clusters from the values and assigns points based on their distance
	* @param values
	* @param numClusters
	* @return
	*/
	protected Cluster[] calculateInitialClusters(List<? extends Clusterable> values,int numClusters){
		Cluster[] clusters = new Cluster[numClusters];
		//Random random = new Random(System.currentTimeMillis());
		Random random = new Random(1);
		List<Integer> clusterCenters = new LinkedList<Integer>();
		for ( int i = 0; i < numClusters; i++ ){
			int index = random.nextInt(values.size());
			while ( clusterCenters.contains(index) ){
				index = random.nextInt(values.size());
			}
			clusterCenters.add(index);
			clusters[i] = new Cluster(values.get(index).getLocation(),i);
		}
		return assignClustersByDistance(values,clusters);
	}
	
	protected Cluster[] assignClustersByDistance(List<? extends Clusterable> values, Cluster[] clusters){
		for ( int j = 0; j < values.size(); j++ ){
			Clusterable val = values.get(j);
			Cluster nearestCluster = null;
			double minDistance = Float.MAX_VALUE;
			for ( int i = 0; i < clusters.length; i++ ){
				Cluster cluster = clusters[i];
				double distance = ClusterUtils.getEuclideanDistance(val,cluster);
				if ( distance < minDistance ){
					nearestCluster = cluster;
					minDistance = distance;
				}
			}
			updatePointMaxDistance(j,minDistance);
			mRx.put(j,Boolean.FALSE);
			mPointIds.put(val,j);
			nearestCluster.addItem(val);
		}
		return clusters;
	}
	
	private void updatePointMaxDistance(int point,double distance){
		if ( mUx.containsKey(point) ){
			mUx.put(point,mUx.get(point)+distance);
		} else {
			mUx.put(point,distance);
		}
	}
	
	private static int getTriangleIndex(int i,int j,int n){
		return ((2*n-i)*(i-1)/2-i+j)-1;
	}
	
	protected Cluster[] getNewClusters(Cluster[] clusters) {
		for ( Cluster c : clusters ){
			if ( c == null ) {
				System.out.println("wtf a null cluster?");
				continue;
			}
			c.setLocation(c.getClusterMean());
		}
		return clusters;
	}
	
	public static void main(String args[]){
		Random random = new Random(1);
		int numPoints = 100000;
		int numClusters = 10;
		List<Clusterable> points = new ArrayList<Clusterable>(numPoints);
		for ( int i = 0; i < numPoints; i++ ){
			int x = random.nextInt(1000) - 500;
			int y = random.nextInt(1000) - 500;
			points.add(new Point(x,y));
		}
		ElkanKMeansClusterer clusterer = new ElkanKMeansClusterer();
		Cluster[] clusters = clusterer.cluster(points,numClusters);

		KMeansClusterer clusterer2 = new KMeansClusterer();
		Cluster[] clusters2 = clusterer2.cluster(points,numClusters);
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
			System.out.println(i + " to " + idx + ", " + closest);
		}
	}
}
