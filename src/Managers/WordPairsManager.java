package Managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;

import NLP.NatureLanguageProcessor;
import NLP.StatisticalTests;
import model.Application;
import model.Review;
import model.Sentence;
import model.Vocabulary;
import model.Word;
import model.WordPair;

public class WordPairsManager implements Serializable {

	/**
	 * 
	 */
	public static final int NOT_OPTION = 2;
	public static final int TO_OPTION = 1;
	private static final long serialVersionUID = 1598928430921693922L;
	public static final int WINDOW_SIZE = 6;

	private static final ArrayList<Template> templateList = new ArrayList<>();

	private static void readPoSTemplates(File file)
			throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNextLine()) {
			String[] PoSs = br.nextLine().split(" ");
			if (PoSs.length >= 2)
				templateList.add(new Template(PoSs));
		}
		br.close();
	}

	private static WordPairsManager instance = null;
	// private Map<PairOfWords, Integer> pairMap;
	private Map<WordPair, Integer> pairMap;
	public static final String FILENAME = main.main.DATA_DIRECTORY
			+ "pairOfWordsData" + ".ser";

	// public static final Set<String> posFilterSet = new HashSet<>();

	// private static void readPoSFilter(File file) throws FileNotFoundException
	// {
	// // TODO Auto-generated method stub
	// Scanner br = new Scanner(new FileReader(file));
	// while (br.hasNextLine()) {
	// posFilterSet.add(br.nextLine());
	// }
	// br.close();
	// }

	public int getTotalPair() {
		int count = 0;
		for (Entry<WordPair, Integer> entry : pairMap.entrySet()) {
			count += entry.getValue();
		}
		return count;
	}

	public Map<WordPair, Integer> getPairMap() {
		return pairMap;
	}

	public static WordPairsManager getInstance() {
		if (instance == null) {
			File fcheckExist = new File(FILENAME);
			if (fcheckExist.exists() && !fcheckExist.isDirectory()) {
				System.err.println(">>Read Application Manager from file.");
				FileInputStream fin;
				try {
					fin = new FileInputStream(FILENAME);
					ObjectInputStream oos = new ObjectInputStream(fin);
					instance = (WordPairsManager) oos.readObject();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					instance = new WordPairsManager();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					instance = new WordPairsManager();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					instance = new WordPairsManager();
				}
			} else
				instance = new WordPairsManager();
		}
		try {
			readPoSTemplates(new File("lib/posTemplates.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instance;
	}

	public WordPairsManager() {
		// TODO Auto-generated constructor stub
		pairMap = new HashMap<>();
	}

	public int findPossiblePairs() {
		ApplicationManager appManager = ApplicationManager.getInstance();
		Set<Application> appSet = appManager.getAppSet();
		int count = 0;
		for (Application app : appSet) {
			List<Review> reviewList = app.getReviews();
			for (Review review : reviewList) {
				if (review.getCreationTime() <= appManager.getLastUpdate())
					continue;
				count++;
				List<List<Integer>> sentenceList = review.getSentenceList();
				for (List<Integer> wordIDList : sentenceList) {
					for (int i = 0; i < wordIDList.size() - 1; i++) {
						for (Template rule : templateList) {
							List<WordPair> extractedPairs = rule.match(
									wordIDList, i);
							for (WordPair pair : extractedPairs) {
								Integer freq = pairMap.get(pair);
								review.addNewPair(pair);
								if (freq != null)
									pairMap.put(pair, freq + 1);
								else
									pairMap.put(pair, 1);
							}
						}
					}
				}
			}
		}
		System.out.println("New pairs of Word extracted: " + count);
		return pairMap.size();
	}

	public void writePair(String fileName) {
		System.err.println(">>Write sentences to file for Word2Vec: "
				+ fileName);
		ApplicationManager appManager = ApplicationManager.getInstance();
		Vocabulary voc = Vocabulary.getInstance();
		PrintWriter pw = null;
		LinkedHashMap<Long, Integer> orderedPairs = new LinkedHashMap<Long, Integer>();
		orderedPairs = StatisticalTests.sortHashMapByValues(pairMap, false);
		try {
			pw = new PrintWriter(new FileWriter(fileName, true));
			for (Entry<Long, Integer> entry : orderedPairs.entrySet()) {
				long w1w2 = entry.getKey();
				int w1 = (int) (w1w2 >> 32);
				int w2 = (int) w1w2;
				Word word1 = voc.getWord(w1);
				Word word2 = voc.getWord(w2);
				pw.println(word1.toString() + " " + word2.toString() + ","
						+ entry.getValue() + "," + word1.getPOS() + " "
						+ word2.getPOS());
			}
			// appManager.writeSentenceToFile(pw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (pw != null)
				pw.close();
		}
	}
}
