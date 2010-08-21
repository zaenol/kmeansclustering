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

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import com.stromberglabs.cluster.ClusterUtils;
import com.stromberglabs.cluster.Point;
import com.stromberglabs.jopensurf.Clusterable;
import com.stromberglabs.util.SizedPriorityQueue;

public class ClusterKDTree {
	private static Random r = new Random(System.currentTimeMillis());
	
	Clusterable cluster = null;
	int splitIndex = -1;
	double splitValue = -1;
	
	ClusterKDTree right;
	ClusterKDTree left;
	
	/**
	 * Creates a KDTree which takes an array of Clusterable objects. It has the option to either
	 * choose the split dimension incrementally (1,2,3,4,etc) or choose the split dimension psuedo
	 * randomly. If the psuedo random version is used, it will simple check that it isn't repeating
	 * the same split dimension as the node above it.
	 * 
	 * @param points
	 * @param randomSplit
	 */
	public ClusterKDTree(Clusterable[] points, boolean randomSplit){
		this(points,randomSplit ? -1 : 0,randomSplit);
	}
	
	private ClusterKDTree(Clusterable[] points, int height, boolean randomSplit){
		if ( points.length == 1 ){
			cluster = points[0];
		} else {
			splitIndex = chooseSplitDimension(points[0].getLocation().length,height,randomSplit);
			splitValue = chooseSplit(points,splitIndex);
			
			Vector<Clusterable> left = new Vector<Clusterable>();
			Vector<Clusterable> right = new Vector<Clusterable>();
			for ( int i = 0; i < points.length; i++ ){
				double val = points[i].getLocation()[splitIndex];
				if ( val == splitValue && cluster == null ){
					cluster = points[i];
				} else if ( val >= splitValue ){
					right.add(points[i]);
				} else {
					left.add(points[i]);
				}
			}
			
			if ( right.size() > 0 )
				this.right = new ClusterKDTree(right.toArray(new Clusterable[right.size()]),randomSplit ? splitIndex : height+1, randomSplit);
			if ( left.size() > 0 )
				this.left = new ClusterKDTree(left.toArray(new Clusterable[left.size()]),randomSplit ? splitIndex : height+1, randomSplit);
		}
	}
	
	private int chooseSplitDimension(int dimensionality,int height,boolean random){
		if ( !random ) return height % dimensionality;
		int rand = r.nextInt(dimensionality);
		while ( rand == height ){
			rand = r.nextInt(dimensionality);
		}
		return rand;
	}
	
	private double chooseSplit(Clusterable points[],int splitIdx){
		double[] values = new double[points.length];
		for ( int i = 0; i < points.length; i++ ){
			values[i] = points[i].getLocation()[splitIdx];
		}
		Arrays.sort(values);
		return values[values.length/2];
	}
	
	/**
	 * Does an approximate nearest neighbor search. It will check up to a certain number of bins
	 * before returning the best option that it has found so far for the point specified.
	 * 
	 * @param point
	 * @param numMaxBinsChecked
	 * @return
	 */
	public Clusterable restrictedNearestNeighbor(Clusterable point, int numMaxBinsChecked){
		//Do the first run down the tree, this gives us the initial closest point and the initial set of bins to search
		SizedPriorityQueue<ClusterKDTree> bins = new SizedPriorityQueue<ClusterKDTree>(50,true);
		Clusterable closest = restrictedNearestNeighbor(point,bins);
		double closestDist = ClusterUtils.getEuclideanDistance(point,closest);
		//System.out.println("retrieved point: " + closest + ", dist: " + closestDist);
		int count = 0;
		while ( count < numMaxBinsChecked && bins.size() > 0 ){
			ClusterKDTree nextBin = bins.pop();
			//System.out.println("Popping of next bin: " + nextBin);
			Clusterable possibleClosest = nextBin.restrictedNearestNeighbor(point,bins);
			double dist = ClusterUtils.getEuclideanDistance(point,possibleClosest);
			if ( dist < closestDist ){
				closest = possibleClosest;
				closestDist = dist;
			}
			count++;
		}
		return closest;
	}
	
	private Clusterable restrictedNearestNeighbor(Clusterable point, SizedPriorityQueue<ClusterKDTree> values){
		if ( splitIndex == -1 ) { /* System.out.println("woo hit the bottom node returning " + cluster); */return cluster; }
		
		double val = point.getLocation()[splitIndex];
		Clusterable closest = null;
		if ( val >= splitValue && right != null || left == null ){
			//put the left branch into the priority queue
			if ( left != null ){
				double dist = val - splitValue;
				values.add(left,dist);
			}
			closest = right.restrictedNearestNeighbor(point,values);
		} else if ( val < splitValue && left != null || right == null ) {
			//put the right branch into the priority queue
			if ( right != null ){
				double dist = splitValue - val;
				values.add(right,dist);
			}
			closest = left.restrictedNearestNeighbor(point,values);
		}
		//current distance of the 'ideal' node
		double currMinDistance = ClusterUtils.getEuclideanDistance(closest,point);
		//check to see if the current node we've backtracked to is closer
		double currClusterDistance = ClusterUtils.getEuclideanDistance(cluster,point);
		if ( closest == null || currMinDistance > currClusterDistance ){
			closest = cluster;
			currMinDistance = currClusterDistance;
		}
		return closest;
	}
	
	public Clusterable exactNearestNeighbor(Clusterable point){
		return restrictedNearestNeighbor(point,Integer.MAX_VALUE);
	}
	
	public void print(){ print(0); }
	
	private void print(int height){
		String s = "";
		for ( int i = 0; i < height; i++ ){
			if ( cluster == null )
				s += "x";
			else
				s += "-";
		}
		s += ">";
		if ( cluster == null ) {
			s += splitIndex + "," + splitValue;
		} else {
			s += "(" + cluster.getLocation()[0] + "," + cluster.getLocation()[1] + ")";
		}
		System.out.println(s);
		if ( right != null ){
			right.print(height+1);
		}
		if ( left != null ){
			left.print(height+1);
		}
	}
	
	public String toString(){
		return "Cluster = " + cluster + ", splitVal = " + splitValue + ", splitIndex = " + splitIndex;
	}
	
	public static void main(String args[]){
//		TEST HARNESS A
		Clusterable clusters[] = new Clusterable[10];
		clusters[0] = new Point(0,0);
		clusters[1] = new Point(1,2);
		clusters[2] = new Point(2,3);
		clusters[3] = new Point(1,5);
		clusters[4] = new Point(2,5);
		clusters[5] = new Point(1,1);
		clusters[6] = new Point(3,3);
		clusters[7] = new Point(0,2);
		clusters[8] = new Point(4,4);
		clusters[9] = new Point(5,5);
		ClusterKDTree tree = new ClusterKDTree(clusters,true);
		tree.print();
		Clusterable c = tree.restrictedNearestNeighbor(new Point(5,2),1000);
		System.out.println(c);
		
//		TEST HARNESS B
//		int numPoints = 1000;
//		int numQueries = 10;
//		Random r = new Random(1);
//		Clusterable points[] = new Clusterable[numPoints];
//		for ( int i = 0; i < points.length; i++ ){
//			int x = r.nextInt(1000) - 500;
//			int y = r.nextInt(1000) - 500;
//			points[i] = new Point(x,y);
//		}
//		Clusterable queries[] = new Clusterable[numQueries];
//		for ( int i = 0; i < queries.length; i++ ){
//			int x = r.nextInt(1000) - 500;
//			int y = r.nextInt(1000) - 500;
//			queries[i] = new Point(x,y);
//		}
//		ClusterKDTree tree = new ClusterKDTree(points,false);
//		for ( Clusterable query : queries ){
//			Clusterable c = tree.restrictedNearestNeighbor(query);
//			double actualClosest = Double.MAX_VALUE;
//			Clusterable closest = null;
//			for ( int i = 0; i < points.length; i++ ){
//				double distance = ClusterUtils.getEuclideanDistance(query,points[i]);
//				if ( distance < actualClosest ){
//					actualClosest = distance;
//					closest = points[i];
//				}
//			}
//			System.out.println("actual closest: " + closest);
//			System.out.println("restricted closest: " + c);
//		}
	}
}
