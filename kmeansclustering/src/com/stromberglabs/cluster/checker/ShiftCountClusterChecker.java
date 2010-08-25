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

package com.stromberglabs.cluster.checker;

import java.util.HashMap;
import java.util.Map;

import com.stromberglabs.cluster.Cluster;

/**
 * <pre>
 * This cluster checker considers clustering done if it meets two conditions:
 *  1) That the percentage of point that shifted was less than K% of the previous
 *  	count for that cluster (i.e. had less than a 5% change)
 *  2) That all the clusters had fewer than N points move in or out of it
 * </pre>
 * @author Andrew
 *
 */
public class ShiftCountClusterChecker implements ClusterChecker {
	Map<Integer,Integer> mPreviousCounts = new HashMap<Integer, Integer>();
	
	private float mPercentChange;
	private float mAbsoluteCount;
	
	public ShiftCountClusterChecker(float percentChange, int absoluteCount) {
		mPercentChange = percentChange;
		mAbsoluteCount = absoluteCount;
	}
	
	public boolean recalculateClusters(Cluster[] clusters) {
		Map<Integer,Integer> newCounts = new HashMap<Integer, Integer>();
		for ( Cluster c : clusters ) {
			newCounts.put(c.getId(),c.getItems().size());
		}
		
		for ( Integer id : newCounts.keySet() ){
			if ( mPreviousCounts.get(id) != null ){
				int absoluteDiff = Math.abs(newCounts.get(id) - mPreviousCounts.get(id));
				float percentChange = (float)(absoluteDiff)/(float)(mPreviousCounts.get(id));
				if ( percentChange > mPercentChange && absoluteDiff > mAbsoluteCount ){
					return true;
				}
			}
			if ( mPreviousCounts.get(id) == null || Math.abs(mPreviousCounts.get(id) - newCounts.get(id)) > mAbsoluteCount ){
				return true;
			}
		}
		return false;
	}
}
