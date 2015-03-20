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
	public static final String DIR = "E:\\AndroidAnalysis\\ReviewData\\data\\v21\\keyword\\";
	static Map<String, Set<String>> keywords = new HashMap<>();// Arrays.asList(new
																// String[]

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
		queryFromDatabase();
		// queryFromFile();
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
			PrintWriter pw = new PrintWriter(DIR + topic.getKey()
					+ "_queryKeyWords.csv");
			pw.println("keyword,sentence");
			Set<String> keys = topic.getValue();
			System.out.println(">> Writing ranked review...");
			for (String sentence : data) {

				HashSet<String> foundKey = new HashSet<>();
				String[] words = sentence.split("[^A-Z0-9']+");
				for (String w : words) {
					if (w.equals("") || w.equals("'"))
						continue;
					for (String keyword : keys) {
						if (w.toLowerCase().equals(keyword)) {
							foundKey.add(keyword);
							break;
						}
					}
				}

				if (foundKey.size() < 2)
					continue;

				StringBuilder key = new StringBuilder();
				for (String w : foundKey)
					key.append(w + "-");

				pw.println(key.toString() + "," + sentence);

			}
			pw.close();
		}

		System.out.println(">> Done");
	}

	private static void queryFromDatabase() throws Throwable,
			FileNotFoundException {
		// String keyTopic = "kicking";
		readKeyWords(new File(DIR + "wordClusters.csv"));
		System.out.println(">> Starting...");
		init();
		// readCollocations(new File(DIR + "testGoogleMetric.csv"));
		for (Entry<String, Set<String>> topic : keywords.entrySet()) {
			PrintWriter pw = new PrintWriter(DIR + topic.getKey()
					+ "_queryKeyWords.csv");
			pw.println("keyword,review,pairs,rating");
			Set<String> keys = topic.getValue();
			System.out.println(">> Writing ranked review...");
			for (Application app : ApplicationManager.getInstance().getAppSet()) {
				for (Review rev : app.getReviews()) {
					if (rev.getRating() > 2)
						continue;
					String[] sentences = rev.toProperString().split("\\.");
					StringBuilder reviewInString = new StringBuilder();
					HashSet<String> foundKey = new HashSet<>();
					for (String sen : sentences) {
						String[] words = sen.split(" ");
						String prefix = "";
						for (String w : words) {

							reviewInString.append(prefix);
							prefix = " ";
							boolean contains = false;
							for (String keyword : keys) {
								if (w.equals(keyword)) {
									foundKey.add(keyword);
									contains = true;
								}
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
					for (String w : foundKey)
						key.append(w + "-");
					StringBuilder strBuilder = new StringBuilder();
					for (Entry<WordPair, Integer> pair : rev.getPairMap()
							.entrySet()) {
						if (pair.getKey().isChoosenBy(WordPair.LOG_LIKELIHOOD))
							strBuilder.append("<" + pair.getKey().toString()
									+ ">");
					}
					pw.println(key.toString() + "," + reviewInString.toString()
							+ "," + strBuilder.toString() + ","
							+ rev.getRating());

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
