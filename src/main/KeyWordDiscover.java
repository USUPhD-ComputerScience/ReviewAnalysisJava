package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import model.Application;
import model.Review;
import model.Vocabulary;
import model.Word;
import Managers.ApplicationManager;
import Managers.WordPairsManager;
import NLP.NatureLanguageProcessor;

public class KeyWordDiscover {
	public static final String DIR = "E:\\AndroidAnalysis\\ReviewData\\data\\v21-keyword\\";
	public static List<String> supportiveData = new ArrayList<>();
	public static List<String> unsupportiveData = new ArrayList<>();
	public static HashMap<String, Integer> supportiveWordRank = new HashMap<>();
	public static HashMap<String, Integer> unsupportiveWordRank = new HashMap<>();
	public static HashMap<String, int[]> wholePopulationStat = new HashMap<>();

	public static Set<String> POSoI = new HashSet<>(
			Arrays.asList(new String[] { "NNPS", "NNS", "VB", "VBD", "VBG",
					"VBN", "VBP", "VBZ", "NN", "NNP" }));

	public static void main(String[] args) throws Throwable {
		// extractWordsFromFile();
		// extractWordsFromData();
		// extractKeyFeatureUsingRatio();
		extractKeyFeatureUsingSkewness();
	}

	private static void extractKeyFeatureUsingSkewness() throws Throwable {

		System.out.println(">>reading data from Database");
		analysePopulationInDB();
		extractKeyWordsUsingSkewness();
	}

	private static void extractKeyWordsUsingSkewness() throws Throwable {
		System.out.println(">>analyzing");
		System.out.println(">>writing to file");
		PrintWriter pw = new PrintWriter(DIR + "keyWords_skewness.csv");
		pw.println("word,score,rate 1,rate 2,rate 3,rate 4,rate 5,total");
		int n = 5;
		for (Entry<String, int[]> word : wholePopulationStat.entrySet()) {
			int[] count = word.getValue();
			double sum = 0;
			double mean = 0.0;
			for (int i = 0; i < n; i++) {
				sum += count[i];
				mean += count[i] * (1 + i);
			}
			mean = mean / sum;
			if (sum < 21 || (count[0] + count[1]) < 21)
				continue;

			double m3 = 0; // sample third central moment
			double s3 = 0; // cubic of sample standard deviation.
			// double mean = sum / n;
			// double mean = count[2];
			for (int i = 0; i < n; i++) {
				m3 += Math.pow(count[i] - mean, 3);
				s3 += Math.pow(count[i] - mean, 2);
			}
			m3 = m3 / n;
			s3 = Math.pow(s3 / (n - 1), 1.5);
			double skewness = m3 / s3;
			// double slope = calcSlope(count[0], count[1], count[2], count[3],
			// count[4]);
			// double cor = pearsonCorrelation(count);
			pw.println(word.getKey() + "," + skewness + "," + count[0] + ","
					+ count[1] + "," + count[2] + "," + count[3] + ","
					+ count[4] + "," + sum);
		}
		pw.close();
		System.out.println(">>done!");
	}

	private static double pearsonCorrelation(int[] x) {
		double mean = 0;
		double xybar = 0;
		double ysqbar = 0;
		for (int i = 0; i < 5; i++) {
			mean += (double) x[i] / 5;
			xybar += ((double) x[i] * (1 + i)) / 5;
			ysqbar += ((double) x[i] * x[i]) / 5;
		}
		double numerator = (xybar - 3 * mean);
		double denominator = Math.sqrt(2 * (ysqbar - mean * mean));
		if (denominator == Double.NaN)
			return 0;
		return numerator / denominator;
	}

	private static double calcSlope(int x1, int x2, int x3, int x4, int x5) {
		// TODO Auto-generated method stub
		double mean = (x1 + x2 + x3 + x4 + x5) / 5;
		double sum = -2 * (x1 - mean) - (x2 - mean) + (x4 - mean) + 2
				* (x5 - mean);
		return sum / 10;
	}

	private static void analysePopulationInDB() {
		// TODO Auto-generated method stub
		Vocabulary voc = Vocabulary.getInstance();
		ApplicationManager appData = ApplicationManager.getInstance();
		NatureLanguageProcessor nlp = NatureLanguageProcessor.getInstance();
		Set<String> stopWords = nlp.getStopWordSet1();
		// WordPairsManager.getInstance();
		int revcount = 0;
		for (Application app : ApplicationManager.getInstance().getAppSet()) {
			for (Review rev : app.getReviews()) {
				revcount++;
				for (List<Integer> sentence : rev.getSentenceList()) {
					for (int wordID : sentence) {
						Word word = voc.getWord(wordID);
						if (!POSoI.contains(word.getPOS()))
							continue;
						int rate = rev.getRating();
						if (rate == 0)
							continue;
						String wordstr = word.toString();
						int[] count = wholePopulationStat.get(wordstr);
						if (count == null) {
							count = new int[5];
						}
						count[rate - 1]++;
						wholePopulationStat.put(wordstr, count);
					}
				}
				if (revcount % 100000 == 0)
					System.out.println("Number of reviews read: " + revcount);
			}
		}
	}

	private static void extractKeyFeatureUsingRatio() throws Throwable {

		System.out.println(">>reading data from Database");
		countWordsInDB();

		extractKeyWords();
	}

	private static void extractWordsFromData() throws Throwable {
		System.out.println(">>reading data from Database");
		loadData();

		countWords(supportiveData, supportiveWordRank);
		countWords(unsupportiveData, unsupportiveWordRank);
		extractKeyWords();
	}

	private static void extractWordsFromFile() throws FileNotFoundException {
		System.out.println(">>reading data from file");
		loadData(new File(DIR + "powerConsumptionKeyWords.csv"));

		countWords(supportiveData, supportiveWordRank);
		countWords(unsupportiveData, unsupportiveWordRank);
		extractKeyWords();
	}

	private static void extractKeyWords() throws FileNotFoundException {
		System.out.println(">>analyzing");
		System.out.println(">>writing to file");
		int supportiveSize = supportiveData.size();
		int unsupportiveSize = unsupportiveData.size();
		PrintWriter pw = new PrintWriter(DIR + "keyWords.csv");
		pw.println("word,ratio,difference,score,supportive count,unsupportive count");
		int totalWord = Vocabulary.getInstance().gettotalWord();
		for (Entry<String, Integer> word : supportiveWordRank.entrySet()) {
			// double supportiveScore = (double) (word.getValue()+1) /
			// supportiveSize;
			// Integer unsupportFreq = unsupportiveWordRank.get(word.getKey());
			// double score = 0.0;
			// if (unsupportFreq == null) {
			// score = word.getValue();
			// unsupportFreq = 0;
			// } else
			// //score =(double) supportiveScore /((double) (unsupportFreq+1) /
			// unsupportiveSize);
			// score =word.getValue() - unsupportFreq+1;
			// pw.println(word.getKey() + "," + score + "," + word.getValue()
			// + "," + unsupportFreq);
			Integer wordUnsupportiveFreq = unsupportiveWordRank.get(word
					.getKey());
			if (wordUnsupportiveFreq == null)
				wordUnsupportiveFreq = 1;
			else
				wordUnsupportiveFreq += 1;
			int wordSupportiveFreq = word.getValue() + 1;
			if (wordSupportiveFreq < 11)
				continue;
			double wordProb = (double) wordSupportiveFreq
					/ ((double) wordSupportiveFreq + wordUnsupportiveFreq);
			if (wordProb < 0.51)
				continue;
			double diff = wordSupportiveFreq - wordUnsupportiveFreq;
			if (diff < 10)
				continue;
			// bigger unsupportive size == smaller score
			// smaller supportive size == bigger score
			double score = wordProb
					* ((Math.log(totalWord) - Math.log(wordUnsupportiveFreq)))
					* (Math.log(wordSupportiveFreq) - Math.log(totalWord));
			pw.println(word.getKey() + "," + wordProb + "," + diff + ","
					+ score + "," + +word.getValue() + ","
					+ wordUnsupportiveFreq);
		}
		pw.close();
		System.out.println(">>done!");
	}

	private static void countWordsInDB() {
		Vocabulary voc = Vocabulary.getInstance();
		ApplicationManager appData = ApplicationManager.getInstance();
		NatureLanguageProcessor nlp = NatureLanguageProcessor.getInstance();
		Set<String> stopWords = nlp.getStopWordSet1();
		// WordPairsManager.getInstance();
		int revcount = 0;
		for (Application app : ApplicationManager.getInstance().getAppSet()) {
			for (Review rev : app.getReviews()) {
				revcount++;
				for (List<Integer> sentence : rev.getSentenceList()) {
					for (int wordID : sentence) {
						Word word = voc.getWord(wordID);
						if (!POSoI.contains(word.getPOS()))
							continue;
						if (rev.getRating() < 3) {

							addFrequency(supportiveWordRank, word.toString(),
									stopWords);
							// supportiveData.add(rev.toProperString());
						}
						if (rev.getRating() > 3) {
							// unsupportiveData.add(rev.toProperString());
							addFrequency(unsupportiveWordRank, word.toString(),
									stopWords);
						}
					}
				}
				if (revcount % 100000 == 0)
					System.out.println("Number of reviews read: " + revcount);
			}
		}
	}

	private static void addFrequency(HashMap<String, Integer> wordRank,
			String word, Set<String> stopWords) {
		if (word.equals("") || word.equals(" "))
			return;
		if (stopWords.contains(word))
			return;
		Integer freq = wordRank.get(word);
		if (freq != null)
			wordRank.put(word, freq + 1);
		else
			wordRank.put(word, 1);
	}

	private static void countWords(List<String> data,
			HashMap<String, Integer> wordRank) {
		NatureLanguageProcessor nlp = NatureLanguageProcessor.getInstance();
		Set<String> stopWords = nlp.getStopWordSet1();
		for (String text : data) {
			HashSet<String> wordSet = new HashSet<>();
			String[] sentences = text.split("\\.");
			for (String sen : sentences) {
				String[] words = sen.split(" ");
				wordSet.addAll(Arrays.asList(words));
			}
			for (String word : wordSet) {
				addFrequency(wordRank, word, stopWords);
			}
		}
	}

	private static void loadData() throws Throwable {
		Vocabulary voc = Vocabulary.getInstance();
		ApplicationManager appData = ApplicationManager.getInstance();
		// WordPairsManager.getInstance();
		int revcount = 0;
		for (Application app : ApplicationManager.getInstance().getAppSet()) {
			for (Review rev : app.getReviews()) {
				revcount++;
				if (rev.getRating() < 3)
					supportiveData.add(rev.toProperString());
				if (rev.getRating() > 3)
					unsupportiveData.add(rev.toProperString());
				if (revcount % 100000 == 0)
					System.out.println("Number of reviews read: " + revcount);
			}
		}

		System.out.println(">> Done reading " + revcount + " reviews");
	}

	private static void loadData(File file) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNextLine()) {
			String[] values = br.nextLine().split(",");
			if (values.length == 4) {
				if (values[3].equals("y"))
					supportiveData.add(values[1]);
				if (values[3].equals("x"))
					unsupportiveData.add(values[1]);
			}
		}
		br.close();
	}
}
