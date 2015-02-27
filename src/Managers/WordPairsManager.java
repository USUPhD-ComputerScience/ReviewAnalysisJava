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

import NLP.StatisticalTests;
import model.Application;
import model.Review;
import model.Sentence;
import model.Vocabulary;
import model.Word;

public class WordPairsManager implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1598928430921693922L;
	private static final int WINDOW_SIZE = 5;
	private static WordPairsManager instance = null;
	// private Map<PairOfWords, Integer> pairMap;
	private Map<Long, Integer> pairMap;
	public static final String FILENAME = main.main.DATA_DIRECTORY
			+ "pairOfWordsData" + ".ser";
	public static final Set<String> posFilterSet = new HashSet<>();

	private static void readPoSFilter(File file) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNextLine()) {
			posFilterSet.add(br.nextLine());
		}
		br.close();
	}

	public int getTotalPair() {
		int count = 0;
		for (Entry<Long, Integer> entry : pairMap.entrySet()) {
			count += entry.getValue();
		}
		return count;
	}

	public Map<Long, Integer> getPairMap() {
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
			readPoSFilter(new File("lib/posFilter.txt"));
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
				List<List<Integer>> sentenceList = review.getSentenceList();
				for (List<Integer> wordIDList : sentenceList) {
					for (int i = 0; i < wordIDList.size() - 1; i++) {
						int j = 1;
						while (j <= WINDOW_SIZE && (i + j) < wordIDList.size()) {
							int w1 = wordIDList.get(i);
							int w2 = wordIDList.get(i + j++);
							if (w1 == w2)
								continue;
							count++;
							long pair = (((long) w1) << 32)
									| (w2 & 0xffffffffL);
							// int x = (int)(l >> 32);
							// int y = (int)l;
							// PairOfWords pair = new PairOfWords(
							// wordIDList.get(i), wordIDList.get(i + j++));
							Integer pairFreq = pairMap.get(pair);
							if (pairFreq != null) {
								// review.addNewPair(pair);
								pairMap.put(pair, ++pairFreq);
							} else {
								review.addNewPair(pair);
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
