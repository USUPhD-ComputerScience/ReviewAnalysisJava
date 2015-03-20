package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import au.com.bytecode.opencsv.CSVWriter;
import Analyzer.Clusterable;
import Analyzer.KMeanClustering;

public class Vocabulary implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8816913577751358916L;
	private List<Word> wordList;
	private Map<Word, Integer> wordIDMap;
	private static Vocabulary instance = null;
	public static final String FILENAME = main.main.DATA_DIRECTORY
			+ "vocabulary" + ".ser";

	public int gettotalWord() {
		int count = 0;
		for (Word w : wordList) {
			count += w.getCount();
		}
		return count;
	}

	public static synchronized Vocabulary getInstance() {
		if (instance == null) {
			File fcheckExist = new File(FILENAME);
			if (fcheckExist.exists() && !fcheckExist.isDirectory()) {
				System.err.println(">>Read Vocabulary from file.");
				FileInputStream fin;
				try {
					fin = new FileInputStream(FILENAME);
					ObjectInputStream oos = new ObjectInputStream(fin);
					instance = (Vocabulary) oos.readObject();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					instance = new Vocabulary();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					instance = new Vocabulary();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					instance = new Vocabulary();
				}
			} else
				instance = new Vocabulary();
		}
		return instance;
	}

	public void clusterWords() {
		KMeanClustering kmean = KMeanClustering.getInstance();
		List<Clusterable> itemList = new ArrayList<>();
		for (Word w : wordList)
			itemList.add(w);
		List<List<Clusterable>> clusters = kmean.clusterByCosineSimilarity(50,
				itemList, 350000);
		exportClusteringResultToCSV(clusters,
				"\\AndroidAnalysis\\ReviewData\\data\\cluster.csv");
	}

	private void exportClusteringResultToCSV(
			List<List<Clusterable>> thresholdClusters, String fileName) {
		System.out.print(">>Writing clusters to file");
		CSVWriter analistWriter = null;
		try {
			final File file = new File(fileName);
			final File parent_directory = file.getParentFile();

			if (null != parent_directory) {
				parent_directory.mkdirs();
			}
			analistWriter = new CSVWriter(new FileWriter(file));
			for (List<Clusterable> cluster : thresholdClusters) {
				if (cluster.size() < 2)
					continue;
				String[] title = { "Cluster Name", "Word",
						"Distance to Centroid", "number Of items" };
				analistWriter.writeNext(title);
				Clusterable topic = cluster.get(0);
				for (Clusterable item : cluster) {
					Word word = (Word) item;
					String[] content = { ((Word) topic).toString(),
							word.toString(),
							String.valueOf(item.getDistanceToCentroid()),
							String.valueOf(cluster.size()) };
					analistWriter.writeNext(content);
				}
			}
			System.out.println("-Done!");
		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				analistWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void writeWordsToFile(String fileName) {
		System.out.print(">>Writing Words to file");

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
			for (Word word : wordList) {
				pw.println(word.toString() + "," + word.getCount() + ","
						+ word.getPOSList().toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (pw != null)
				pw.close();
		}

	}

	private Vocabulary() {
		wordList = new ArrayList<>();
		wordIDMap = new HashMap<>();
	}

	/**
	 * A general function to add a word to this Vocabulary. If the word is
	 * already in the vocabulary, its count/frequency will be increased by 1.
	 * USAGE: Just add the word, don't need to worry about if it has already
	 * inside the vocabulary or not. NOTE: same word but different POS is still
	 * considered new word.
	 * 
	 * @param w2
	 *            - the word that needs to be added
	 * @param POS
	 *            - Part-of-Speech tagging for that word. It should be an ID.
	 * @return the wordID of this word
	 */
	public int addWord(String text, String POS) {
		Word word = new Word(text, POS);
		Integer id = wordIDMap.get(word);
		if (id == null) {
			id = wordList.size();
			wordIDMap.put(word, id);
			wordList.add(word);
		} else {
			Word w = wordList.get(id);
			w.increaseCount();
			w.addPOS(POS);
		}
		return id;
	}

	/**
	 * Return the word by wordID. The result will be null if there is no word
	 * associates with that wordID
	 * 
	 * @param w2
	 *            - the word that needs to be added
	 * @return the Word object of this wordID
	 */
	public Word getWord(int wordID) {
		if (wordList.size() <= wordID || wordID < 0)
			return null;
		return wordList.get(wordID);
	}

	/**
	 * Return the word by String word and POS. The result will be null if there
	 * is no word associates with that wordID
	 * 
	 */
	public Word getWord(String word, String POS) {
		Word w = new Word(word, POS.intern());
		Integer id = wordIDMap.get(w);
		if (id == null)
			return null;
		else
			return wordList.get(id);
	}

	/**
	 * Return the wordID of a word.
	 * 
	 */
	public int getWordID(Word w) {
		Integer id = wordIDMap.get(w);
		if (id == null)
			return -1;
		else
			return id;
	}
	/**
	 * Return the wordID of a word.
	 * 
	 */
	public int getWordID(String word) {
		Word w = new Word(word, "");
		Integer id = wordIDMap.get(w);
		if (id == null)
			return -1;
		else
			return id;
	}
	/**
	 * Return the wordID of a word. Null if there is no such word.
	 * 
	 */
	public int getWordID(String word, String POS) {
		Word w = new Word(word, POS.intern());
		Integer id = wordIDMap.get(w);
		if (id == null)
			return -1;
		else
			return id;
	}

	public int getWordCount(int wordID) {
		if (wordList.size() <= wordID || wordID < 0)
			return -1;
		return wordList.get(wordID).getCount();
	}
}
