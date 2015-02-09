package Managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.Application;
import model.Review;
import model.Sentence;
import model.Word;

public class WordPairsManager implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1598928430921693922L;
	private static final int WINDOW_SIZE = 5;
	private static WordPairsManager instance = null;
	//private Map<PairOfWords, Integer> pairMap;
	private Map<Long, Integer> pairMap;
	public static final String FILENAME = "\\AndroidAnalysis\\ReviewData\\data\\"
			+ "pairOfWordsData" + ".ser";

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
				List<Sentence> sentenceList = review.getSentenceList();
				for (Sentence sentence : sentenceList) {
					List<Integer> wordIDList = sentence.getWordIDList();
					for (int i = 0; i < wordIDList.size() - 1; i++) {
						int j = 1;
						while (j <= WINDOW_SIZE && (i + j) < wordIDList.size()) {
							count++;
							long pair = (((long)wordIDList.get(i)) << 32) | (wordIDList.get(i + j++) & 0xffffffffL);
							//int x = (int)(l >> 32);
							//int y = (int)l;
							//PairOfWords pair = new PairOfWords(
							//		wordIDList.get(i), wordIDList.get(i + j++));
							Integer pairFreq = pairMap.get(pair);
							if (pairFreq != null) {
								review.addNewPair(pair);
								pairMap.put(pair, pairFreq++);
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
}
