package Analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.util.Comparators;

public class KMeanClustering {
	private static KMeanClustering instance = null;

	private KMeanClustering() {
	}

	public static KMeanClustering getInstance() {
		if (instance == null)
			instance = new KMeanClustering();
		return instance;
	}

	public List<List<Clusterable>> clusterByCosineSimilarity(int iteration,
			List<Clusterable> itemList, int K) {

		if (itemList == null || itemList.size() == 0)
			return null;
		int iterator = 0;
		int vectorSize = itemList.get(0).getVector().length;
		int numItems = itemList.size();
		int[] itemToCluster = new int[numItems];
		float[][] centroids = new float[K][vectorSize];
		// initial step: Select K centroids
		Set<Integer> selected = new HashSet<>();
		for (int i = 0; i < K; i++) {
			int item = 0;
			do {
				item = (int) (Math.random() * numItems);
			} while (selected.contains(item));
			selected.add(item);
			centroids[i] = itemList.get(item).getVector();
		}
		// training
		for (int i = 0; i < iteration; i++) {
			iterator++;
			// Step 1: re-assign items to clusters
			boolean terminate = true;
			for (int item = 0; item < numItems; item++) {
				double maxSimilarity = Double.MIN_VALUE;
				int maxK = 0;
				for (int k = 0; k < K; k++) {
					double similarity = cosineSimilarityForVectors(itemList
							.get(item).getVector(), centroids[k], true);
					if (maxSimilarity < similarity) {
						maxSimilarity = similarity;
						maxK = k;
					}
				}

				if (itemToCluster[item] != maxK) {
					itemToCluster[item] = maxK;
					itemList.get(item).setDistanceToCentroid(maxSimilarity);
					terminate = false;
				}
			}

			if (terminate)
				break;
			// Step 2: recalculate centroids
			for (int k = 0; k < K; k++) {
				Arrays.fill(centroids[k], 0);
				int count = 0;
				for (int item = 0; item < numItems; item++) {
					if (itemToCluster[item] == k) {
						float[] itemVector = itemList.get(item).getVector();
						count += itemList.get(item).getFrequency();
						for (int j = 0; j < vectorSize; j++) {
							centroids[k][j] += itemVector[j];
						}
					}
				}
				for (int j = 0; j < vectorSize; j++) {
					centroids[k][j] /= (float) count;
				}
			}
		}
		// order by distance to centroid
		List<List<Clusterable>> clusters = new ArrayList<>();
		for (int k = 0; k < K; k++) {
			clusters.add(new ArrayList<Clusterable>());
		}
		for (int item = 0; item < numItems; item++) {
			clusters.get(itemToCluster[item]).add(itemList.get(item));
		}

		for (int k = 0; k < K; k++) {
			Collections.sort(clusters.get(k), new Comparator<Clusterable>() {

				@Override
				public int compare(Clusterable o1, Clusterable o2) {
					// TODO Auto-generated method stub
					return (int) Math.signum(o2.getDistanceToCentroid()
							- o1.getDistanceToCentroid());
				}

			});
		}
		System.out.println("--Number of Iteration = " + iterator);
		return clusters;
	}

	public double cosineSimilarityForVectors(float[] vector1, float[] vector2,
			boolean normalize) {

		double sim = 0, square1 = 0, square2 = 0;

		if (vector1 == null || vector2 == null)
			return 0;
		for (int i = 0; i < vector1.length; i++) {
			square1 += vector1[i] * vector1[i];
			square2 += vector2[i] * vector2[i];
			sim += vector1[i] * vector2[i];
		}
		if (sim == 0)
			return 0;
		if (!normalize)
			return sim / Math.sqrt(square1) / Math.sqrt(square2);
		else
			return (1 + sim / Math.sqrt(square1) / Math.sqrt(square2)) / 2;
	}
}
