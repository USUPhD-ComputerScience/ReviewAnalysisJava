package Analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import NLP.NatureLanguageProcessor;
import word2vec.WordVec;

public class AnalyzeClusters {
	static private ArrayList<PairForCluster> likelihoodTest = new ArrayList<>();
	static private ArrayList<PairForCluster> googleMetricTest = new ArrayList<>();
	static private ArrayList<PairForCluster> tTest = new ArrayList<>();
	static private ArrayList<Item> words = new ArrayList<>();
	static private final String CURRENT_DIR = "E:\\AndroidAnalysis\\ReviewData\\data\\v22-request\\";
	static private final List<String> data = new ArrayList<>();

	public static void main(String[] args) throws Throwable {
		// clusterPairs();
		clusterWords();
		// clusterRequestSentences();
	}

	public static void clusterRequestSentences() throws Throwable {
		System.out.println(">>Read data from test files");
		loadRequestSentences(new File(CURRENT_DIR + "requestSentences.csv"));
		System.out.println(">>Clustering..");
		cluster(new File(CURRENT_DIR + "requestClusters.csv"));
		System.out.println(">>Done");
	}

	private static void loadRequestSentences(File file) throws Throwable {
		Scanner br = new Scanner(new FileReader(file));
		HashMap<String, Integer> voc = new HashMap<>();
		while (br.hasNextLine()) {
			String sentence = br.nextLine();
			data.add(sentence);
			Set<String> UPPERCASE = new HashSet<>(Arrays.asList(sentence
					.split("[^A-Z0-9']+")));
			for (String word : UPPERCASE) {
				if (word.equals("") || word.equals("'"))
					continue;
				if (NatureLanguageProcessor.POSSET.contains(word))
					continue;
				Integer freq = voc.get(word);
				if (freq == null)
					voc.put(word, 1);
				else
					voc.put(word, freq + 1);
			}
		}
		for (Entry<String, Integer> entry : voc.entrySet()) {
			Item item = new Item(entry.getKey().toLowerCase(), entry.getValue());
			if (item.getVector() != null)
				words.add(item);
		}
		br.close();
	}

	private static void clusterWords() throws Throwable {
		System.out.println(">>Read data from test files");
		loadTestSet(new File(main.main.DATA_DIRECTORY
				+ "keyWords_pearsonCor.csv"));
		cluster(new File(main.main.DATA_DIRECTORY + "wordClusters.csv"));

	}

	private static void clusterPairs() throws FileNotFoundException, Throwable {
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
		if (rawData.isEmpty())
			return;
		for (PairForCluster pair : rawData)
			itemList.add(pair);
		List<List<Clusterable>> clusters = KMeanClustering.getInstance()
				.clusterByCosineSimilarity(100, itemList, 450);
		System.out.println(">> Write clusters to file");
		writeClustersToFile(clusters, file, true);
	}

	private static void cluster(File file) throws Throwable {

		List<Clusterable> itemList = new ArrayList<>();
		if (words.isEmpty())
			return;
		for (Item word : words)
			itemList.add(word);
		List<List<Clusterable>> clusters = KMeanClustering.getInstance()
				.clusterByCosineSimilarity(100, itemList,
						(int) Math.round(Math.sqrt(words.size() / 2)));
		System.out.println(">> Write clusters to file");
		writeClustersToFile(clusters, file, false);
	}

	private static void writeClustersToFile(List<List<Clusterable>> clusters,
			File file, boolean isPair) throws Throwable {
		PrintWriter pw = new PrintWriter(new FileWriter(file));
		if (isPair) {
			for (List<Clusterable> cluster : clusters) {
				if (cluster.isEmpty())
					continue;
				PairForCluster mainTopic = (PairForCluster) cluster.get(0);
				pw.println(mainTopic.getString() + ",");
				for (Clusterable item : cluster) {
					PairForCluster pair = (PairForCluster) item;
					pw.print("<" + pair.getString() + ">");
				}
				pw.println();
			}
		} else {
			for (List<Clusterable> cluster : clusters) {
				if (cluster.isEmpty())
					continue;
				Item mainTopic = (Item) cluster.get(0);
				pw.print(mainTopic.toString() + ",");
				int totalCount = 0;
				for (Clusterable item : cluster) {
					Item word = (Item) item;
					pw.print("<" + word.toString() + ">");
					totalCount += word.getFrequency();
				}
				pw.println("," + totalCount);
			}
		}
		pw.close();
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

	private static void loadTestSet(File file) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner br = new Scanner(new FileReader(file));
		String first = br.nextLine();
		Set<String> stopwords = NatureLanguageProcessor.getInstance()
				.getStopWordSet1();
		int count = 0;
		while (br.hasNextLine()) {
			String[] values = br.nextLine().split(",");
			if (values.length == 8) {
				String word = values[0];
				if (stopwords.contains(word))
					continue;
				if (Double.parseDouble(values[1]) > -0.4)
					continue;
				count++;
				int freq = (int) Double.parseDouble(values[7]);
				Item item = new Item(word, freq);
				if (item.getVector() != null)
					words.add(item);
			}
		}
		br.close();
		System.out.println(">> Read " + count + " words!");
	}

	private static class Item extends Clusterable {

		float[] vector = null;
		int frequency;
		String word;
		boolean change = false;
		public static final WordVec word2vec = new WordVec();

		public Item(String str, int freq) {
			// TODO Auto-generated constructor stub
			frequency = freq;
			word = str.intern();
			float[] tempVector = word2vec.getVectorForWord(word);
			if (tempVector != null) {
				vector = new float[WordVec.VECTOR_SIZE];
				for (int i = 0; i < WordVec.VECTOR_SIZE; i++) {
					vector[i] = tempVector[i];
				}
			}
		}

		public String toString() {
			return word;
		}

		@Override
		public float[] getVector() {
			// TODO Auto-generated method stub
			return vector;
		}

		@Override
		public int getFrequency() {
			// TODO Auto-generated method stub
			return frequency;
		}

		@Override
		public void setChange(boolean isChanged) {
			// TODO Auto-generated method stub
			change = isChanged;
		}

		@Override
		public boolean isChanged() {
			// TODO Auto-generated method stub
			return change;
		}

	}

}
