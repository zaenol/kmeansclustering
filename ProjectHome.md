# Overview #
This was a project to test various methods of K Means Clustering while working on a visual search application. I've implemented:

  1. K-Means Clustering - Plain vanilla clustering, for more information you can see the [wikipedia article](http://en.wikipedia.org/wiki/K-means_algorithm) on it.
  1. Elkan K-Means Clustering - Partially implemented, uses the triangle inequality to speed up assignment of points to their closest cluster. It works right now, I've only implemented the upper bounds removal of points though. More information is available [here](http://www.stromberglabs.com/k-means-clustering/#ElkanKMeans)
  1. K-Means KDTree Clustering - Uses a KD Tree to speed up the assignment of points to their closest cluster.
  1. K-Means KD Forest Clustering - Uses a forest of KD trees and an approximate nearest neighbor search to choose assignment of points to their closest cluster.


# Other Release Items #
I implemented a couple other things which are being released with this because they support the clustering code:

  1. KD Tree - A KD tree that supports exact nearest neighbor and approximate nearest neighbor searches.
  1. Sized Priority Queue - A data structure that just keeps a fixed size list of the most important items that are added to it.

# Other Release Items #
You can get the jar and it's supporting library by downloading it [here](https://code.google.com/p/kmeansclustering/downloads/detail?name=kmeansclustering.zip&can=2&q=).