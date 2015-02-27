package Analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class AnalyzeClusters {
	static private ArrayList<PairForCluster> likelihoodTest = new ArrayList<>();
	static private ArrayList<PairForCluster> googleMetricTest = new ArrayList<>();
	static private ArrayList<PairForCluster> tTest = new ArrayList<>();
	static private final String CURRENT_DIR = "E:\\AndroidAnalysis\\ReviewData\\data\\v9\\";

	public static void main(String[] args) throws Throwable {
		System.out.println(">>Read data from test files");
		loadTestSet(new File(CURRENT_DIR + "likelyHoodRatioManning.csv"),
				likelihoodTest);
		loadTestSet(new File(CURRENT_DIR + "testGoogleMetric.csv"),
				googleMetricTest);
		loadTestSet(new File(CURRENT_DIR + "ttest.csv"), tTest);
		System.out.println(">>Clustering log-likelihood test by K-Means.");
		cluster(likelihoodTest,
				new File(CURRENT_DIR + "likelihoodClusters.csv"));
		System.out.println(">>Clustering Google metric test by K-Means.");
		cluster(googleMetricTest, new File(CURRENT_DIR
				+ "googleMetricClusters.csv"));
		System.out.println(">>Clustering t-test by K-Means.");
		cluster(tTest, new File(CURRENT_DIR + "tTestClusters.csv"));

	}

	private static void cluster(ArrayList<PairForCluster> rawData, File file)
			throws Throwable {

		List<Clusterable> itemList = new ArrayList<>();
	    if(rawData.isEmpty())
	    	return;
		for (PairForCluster pair : rawData)
			itemList.add(pair);
		List<List<Clusterable>> clusters = KMeanClustering.getInstance()
				.clusterByCosineSimilarity(100, itemList, 450);
		System.out.println(">> Write clusters to file");
		writeClustersToFile(clusters, file);
	}

	private static void writeClustersToFile(List<List<Clusterable>> clusters,
			File file) throws Throwable {
		PrintWriter pw = new PrintWriter(new FileWriter(file));
		for (List<Clusterable> cluster : clusters) {
			if(cluster.isEmpty())
				continue;
			PairForCluster mainTopic = (PairForCluster) cluster.get(0);
			pw.println(mainTopic.getString() + ",");
			for (Clusterable item : cluster) {
				PairForCluster pair = (PairForCluster) item;
				pw.print("<" + pair.getString() + ">");
			}
			pw.println();
		}
	}

	private static void loadTestSet(File file, ArrayList<PairForCluster> testSet)
			throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNextLine()) {
			String[] pair = br.nextLine().split(",");
			if (pair.length == 4) {
				PairForCluster pairCL = new PairForCluster(pair[0],
						Double.valueOf(pair[1]));
				if (pairCL.getVector() != null)
					testSet.add(pairCL);
			}

		}
		br.close();
	}

}
