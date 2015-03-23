package Analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import main.KeyWordDiscover;
import model.Application;
import model.Review;
import model.Vocabulary;
import model.WordPair;
import Managers.ApplicationManager;
import Managers.WordPairsManager;
import NLP.NatureLanguageProcessor;

public class QueryKeyWords {
	public static final String DIR = "E:\\AndroidAnalysis\\ReviewData\\data\\v22-request\\";
	static Map<String, Set<String>> keywords = new HashMap<>();// Arrays.asList(new
																// String[]
	private static Vocabulary voc = Vocabulary.getInstance();

	// {

	// "energy", "battery", "charge", "drain", "consume", "hog",
	// "cpu", "log", "ram", "memory", "kill", "slow", "freeze",
	// "black", "annoying", "out","eat","mistake"}));
	private static void readKeyWords(File file) throws Throwable {
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNextLine()) {
			String[] values = br.nextLine().split(",");
			if (values.length == 3) {
				Set<String> keywordsForThisTopic = new HashSet<>();
				String[] keys = values[1].split("[^a-z0-9]+");
				for (String w : keys) {
					if (w.length() < 3)
						continue;
					keywordsForThisTopic.add(w);
				}
				keywords.put(values[0], keywordsForThisTopic);
			}
		}
		br.close();
	}

	public static void main(String[] args) throws Throwable {
		// queryFromDatabase();
		queryFromFile();
	}

	static private final List<String> data = new ArrayList<>();

	private static void readData(File file) throws Throwable {
		Scanner br = new Scanner(new FileReader(file));
		HashMap<String, Integer> voc = new HashMap<>();
		while (br.hasNextLine()) {
			data.add(br.nextLine());
		}
	}

	private static void queryFromFile() throws Throwable, FileNotFoundException {
		System.out.println(">.read data");
		readKeyWords(new File(DIR + "requestClusters.csv"));
		readData(new File(DIR + "requestSentences.csv"));
		System.out.println(">> Starting...");
		// readCollocations(new File(DIR + "testGoogleMetric.csv"));
		for (Entry<String, Set<String>> topic : keywords.entrySet()) {
			PrintWriter pw = new PrintWriter(DIR + "request\\" + topic.getKey()
					+ "_queryKeyWords.csv");
			pw.println("keyword,sentence,cosineSim_tfidf");
			Set<String> keys = topic.getValue();
			System.out.println(">> Writing ranked review...");
			for (String sentence : data) {

				HashMap<String, Double> foundKey = new HashMap<>();
				String[] words = sentence.split("[^A-Z0-9']+");
				for (String w : words) {
					if (w.equals("") || w.equals("'"))
						continue;
					if (keys.contains(w.toLowerCase())) {
						Double tfidf = foundKey.get(w);
						if (tfidf == null)
							foundKey.put(w, 1.0);
						else
							foundKey.put(w, tfidf + 1.0);
					}
				}

				if (foundKey.size() < 1)
					continue;

				StringBuilder key = new StringBuilder();
				for (Entry<String, Double> w : foundKey.entrySet()) {
					String wordstr = w.getKey().toLowerCase();
					key.append(wordstr + "-");
				}
				pw.println(key.toString() + "," + sentence + ","
						+ cosineSimilarityOfTFIDF(keys, foundKey, true));

			}
			pw.close();
		}

		System.out.println(">> Done");
	}

	private static double cosineSimilarityOfTFIDF(Set<String> keys,
			HashMap<String, Double> foundKey, boolean normalize) {

		double[] vector1 = new double[keys.size()];
		double[] vector2 = new double[keys.size()];
		int index = 0;
		for (String key : keys) {
			vector1[index] = 1 / (1 + Math.log(voc.getWordCount(voc
					.getWordID(key))));
			Double freq = foundKey.get(key.toUpperCase());
			if (freq != null)
				vector2[index] = freq
						/ (1 + Math.log(voc.getWordCount(voc.getWordID(key
								.toLowerCase()))));
			else
				vector2[index] = 0.0;
			index++;
		}
		double sim = 0, square1 = 0, square2 = 0;
		if (vector1 == null || vector2 == null)
			return 0;
		for (int i = 0; i < vector1.length; i++) {
			square1 += vector1[i] * vector1[i];
			square2 += vector2[i] * vector2[i];
			sim += vector1[i] * vector2[i];
		}
		if (!normalize)
			return sim / Math.sqrt(square1) / Math.sqrt(square2);
		else
			return (1 + sim / Math.sqrt(square1) / Math.sqrt(square2)) / 2;
	}

	private static void queryFromDatabase() throws Throwable,
			FileNotFoundException {
		// String keyTopic = "kicking";
		readKeyWords(new File(main.main.DATA_DIRECTORY + "wordClusters.csv"));
		System.out.println(">> Starting...");
		init();
		// readCollocations(new File(DIR + "testGoogleMetric.csv"));
		for (Entry<String, Set<String>> topic : keywords.entrySet()) {
			PrintWriter pw = new PrintWriter(main.main.DATA_DIRECTORY
					+ "keyword\\" + topic.getKey() + "_queryKeyWords.csv");
			pw.println("keyword,review,pairs,score");
			Set<String> keys = topic.getValue();
			System.out.println(">> Writing ranked review...");
			for (Application app : ApplicationManager.getInstance().getAppSet()) {
				for (Review rev : app.getReviews()) {
					if (rev.getRating() > 2)
						continue;
					String[] sentences = rev.toProperString().split("\\.");
					StringBuilder reviewInString = new StringBuilder();
					HashMap<String, Double> foundKey = new HashMap<>();
					for (String sen : sentences) {
						String[] words = sen.split(" ");
						String prefix = "";
						for (String w : words) {

							reviewInString.append(prefix);
							prefix = " ";
							boolean contains = false;
							if (keys.contains(w)) {
								contains = true;
								Double tfidf = foundKey.get(w);
								if (tfidf == null)
									foundKey.put(w, 1.0);
								else
									foundKey.put(w, tfidf + 1.0);

							}

							if (contains)
								reviewInString.append(w.toUpperCase());
							else
								reviewInString.append(w);
						}
						reviewInString.append(".");
					}

					if (foundKey.size() < 2)
						continue;

					StringBuilder key = new StringBuilder();
					for (Entry<String, Double> w : foundKey.entrySet()) {
						String wordstr = w.getKey().toLowerCase();
						key.append(wordstr + "-");
					}
					StringBuilder strBuilder = new StringBuilder();
					for (Entry<WordPair, Integer> pair : rev.getPairMap()
							.entrySet()) {
						if (pair.getKey().isChoosenBy(WordPair.LOG_LIKELIHOOD))
							strBuilder.append("<" + pair.getKey().toString()
									+ ">");
					}
					pw.println(key.toString() + "," + reviewInString.toString()
							+ "," + strBuilder.toString() + ","
							+ cosineSimilarityOfTFIDF(keys, foundKey, true));

				}
			}
			pw.close();
		}

		System.out.println(">> Done");
	}

	public static void init() {
		Vocabulary.getInstance();
		ApplicationManager.getInstance();
		WordPairsManager.getInstance();
	}
}
