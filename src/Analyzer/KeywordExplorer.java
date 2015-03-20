package Analyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import word2vec.WordVec;

public class KeywordExplorer {
	static WordVec word2vec = WordVec.getInstance();
	static Map<String, Double> pairSimiliarity = new HashMap<>();

	public static void main(String[] args) throws Throwable {
		// exploreKeyWords(new File(main.main.DATA_DIRECTORY
		// + "\\v21\\keywordExplorationResult.txt"));
		Set<String> selection = new HashSet<>();
		selection.add("battery");
		autoExploreKeyWords(new File(main.main.DATA_DIRECTORY
				+ "\\v21\\keywordExplorationResult.txt"), selection, 0.7);
	}

	public static double cosineSimilarity(String word1, String word2) {
		String pair = word1 + "_" + word2;
		Double sim = pairSimiliarity.get(pair);
		if (sim == null) {
			sim = word2vec.cosineSimilarityForWords(word1, word2, true);
			pairSimiliarity.put(pair, sim);
		}
		return sim;
	}

	public static void exploreKeyWords(File file) throws Throwable {
		Scanner scanner = new Scanner(System.in);
		List<String> explorationResult = new ArrayList<>();
		// first step:
		System.out.println("Input your first word:");
		String word1 = scanner.next();
		explorationResult.add(word1);
		List<String> results = findTopSimilarWords(word1, 5);
		for (String res : results) {
			System.out.println(res);
		}
		while (true) {
			System.out.println("---------------------------------------------");
			System.out.println("Choose your set of key words:");
			String[] keyWords = scanner.next().split(",");
			for (String keyWord : keyWords) {
				explorationResult.add(keyWord);
				System.out.println("Top ten words for <" + keyWord + "> is:");
				results = findTopSimilarWords(keyWord, 10);
				for (String res : results) {
					System.out.println(res);
				}
			}
			System.out.println("Do you want to stop and print result now?");
			String isStop = scanner.next();
			if (isStop.equalsIgnoreCase("y")) {
				PrintWriter pw = new PrintWriter(new FileWriter(file));
				pw.println(explorationResult.toString());
				pw.close();
				System.out.println("Printed result into " + file.toString());
				break;
			}
		}
	}

	public static void autoExploreKeyWords(File file, Set<String> selection,
			double threshold) throws Throwable {
		System.out.println(">>Start!");
		int count = 0;
		while (true) {
			count++;
			List<String> results = findTopSimilarWords(selection, 10);
			if (selection.containsAll(results))
				break;
			selection.add(results.get(0));
			if (avgSimilarity(selection) <= threshold )//|| selection.size() > 20)
				break;
		}

		// do printing here
		System.out.println(">> done with " + count + " iterations.");
		PrintWriter pw = new PrintWriter(new FileWriter(file));
		pw.println(selection.toString());
		pw.close();
		System.out.println("Printed result into " + file.toString());
	}

	private static double avgSimilarity(Collection<String> selection) {
		double totalSim = 0;
		int count = 0;
		for (String word1 : selection)
			for (String word2 : selection)
				if (word1 != word2) {
					totalSim += cosineSimilarity(word1, word2);
					count++;
				}
		return totalSim / count;
	}

	private static List<String> findTopSimilarWords(Set<String> selection,
			int top) {
		String[] words = new String[top];
		double[] cosineDistance = new double[top];
		for (Entry<String, float[]> entry : word2vec.getWordVector().entrySet()) {
			String word2 = entry.getKey();
			if(selection.contains(word2))
				continue;
			double result = cosineSimilarityForWords(selection, word2);
			for (int i = 0; i < top; i++) {
				if (result > cosineDistance[i]) {
					double lastDistance = cosineDistance[i];
					String lastWord = words[i];
					cosineDistance[i] = result;
					words[i] = word2.intern();
					double currentDistance = lastDistance;
					String currentWord = lastWord;
					for (int j = i + 1; j < top; j++) {
						lastDistance = cosineDistance[j];
						lastWord = words[j];
						cosineDistance[j] = currentDistance;
						if (currentWord == null)
							words[j] = null;
						else
							words[j] = currentWord.intern();
						currentDistance = lastDistance;
						currentWord = lastWord;
					}
					break;
				} else
					continue;
			}
		}
		List<String> results = new ArrayList<>();
		for (int i = 0; i < top; i++) {
			results.add(words[i]);
		}
		return results;
	}

	public static double cosineSimilarityForWords(Set<String> words,
			String word2) {
		double score = 1.0;
		for (String word1 : words) {
			score *= (1.0 - cosineSimilarity(word1, word2));
		}
		return 1.0 - score;
	}

	public static List<String> findTopSimilarWords(String word, int top) {
		String[] words = new String[top];
		double[] cosineDistance = new double[top];
		for (Entry<String, float[]> entry : word2vec.getWordVector().entrySet()) {
			String word2 = entry.getKey();
			if (word.equals(word2))
				continue;
			double result = word2vec
					.cosineSimilarityForWords(word, word2, true);
			for (int i = 0; i < top; i++) {
				if (result > cosineDistance[i]) {
					double lastDistance = cosineDistance[i];
					String lastWord = words[i];
					cosineDistance[i] = result;
					words[i] = word2.intern();
					double currentDistance = lastDistance;
					String currentWord = lastWord;
					for (int j = i + 1; j < top; j++) {
						lastDistance = cosineDistance[j];
						lastWord = words[j];
						cosineDistance[j] = currentDistance;
						if (currentWord == null)
							words[j] = null;
						else
							words[j] = currentWord.intern();
						currentDistance = lastDistance;
						currentWord = lastWord;
					}
					break;
				} else
					continue;
			}
		}
		List<String> results = new ArrayList<>();
		for (int i = 0; i < top; i++) {
			results.add(words[i] + "_" + cosineDistance[i]);
		}
		return results;
	}
}
