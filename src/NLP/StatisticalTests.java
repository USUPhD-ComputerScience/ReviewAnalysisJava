package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import model.Vocabulary;
import model.Word;
import model.WordPair;
import Managers.WordPairsManager;

public class StatisticalTests {
	private static StatisticalTests instance = null;

	public static StatisticalTests getInstance() {
		if (instance == null)
			instance = new StatisticalTests();
		return instance;
	}

	// t=2.576 for confidence level 0.005 (99.5% confidence)
	public static void tTest(WordPairsManager bigramVoc, Vocabulary wordVoc,
			double t) {
		Map<WordPair, Integer> bigramMap = bigramVoc.getPairMap();
		// int totalPair = bigramVoc.getTotalPair();
		int totalWord = wordVoc.gettotalWord();
		// double tTest = 0;
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(main.main.DATA_DIRECTORY
					+ "ttest.csv"));
			pw.println("pair,count,score,pos");
			for (Entry<WordPair, Integer> entry : bigramMap.entrySet()) {
				long w1w2 = entry.getKey().getPair();
				int w1 = (int) (w1w2 >> 32);
				int w2 = (int) w1w2;
				if (w1 == w2)
					continue;
				// int w1 = entry.getKey().getFirstWord();
				// int w2 = entry.getKey().getSecondWord();
				// xBar = (double) entry.getValue() / totalPair;
				// h0 = (double) (wordVoc.getWordCount(w1) * wordVoc
				// .getWordCount(w2)) / ((double) totalWord * totalWord);
				// tTest = (xBar - h0) / Math.sqrt(xBar / totalPair);

				// ////////////
				double o11 = entry.getValue();
				double o_1 = wordVoc.getWordCount(w1);
				double o1_ = wordVoc.getWordCount(w2);

				double e11 = (o1_ * o_1) / totalWord;
				double tTest = (o11 - e11) / Math.sqrt(o11);

				if (tTest >= t) {

					Word word1 = wordVoc.getWord(w1);
					Word word2 = wordVoc.getWord(w2);
					String pos = word1.getPOS() + " " + word2.getPOS();
					writeTestResult(pw, entry.getKey(), o11, tTest);

					entry.getKey().setTest(true, WordPair.TTEST);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pw != null)
			pw.close();

	}

	public static void writeTestResult(PrintWriter pw, WordPair pair,
			double pairFreq, double score) {
		pw.println(pair.toString() + "," + pairFreq + "," + score + ","
				+ pair.toPOS());
	}

	public static void testMutualInformation(WordPairsManager bigramVoc,
			Vocabulary wordVoc, double threshold) {
		Map<WordPair, Integer> bigramMap = bigramVoc.getPairMap();
		double totalWord = wordVoc.gettotalWord();
		// int totalPair = bigramVoc.getTotalPair();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(main.main.DATA_DIRECTORY
					+ "mutualInformation.csv"));
			pw.println("pair,count,score,pos");
			for (Entry<WordPair, Integer> entry : bigramMap.entrySet()) {
				long w1w2 = entry.getKey().getPair();
				int w1 = (int) (w1w2 >> 32);
				int w2 = (int) w1w2;
				if (w1 == w2)
					continue;
				double o11 = entry.getValue(); // c12
				double o_1 = wordVoc.getWordCount(w1); // c1
				double o1_ = wordVoc.getWordCount(w2); // c2

				double e11 = (o1_ * o_1) / totalWord;
				double mi = Math.log10(o11 / e11);

				if (mi <= threshold || o11 >= 500) {

					Word word1 = wordVoc.getWord(w1);
					Word word2 = wordVoc.getWord(w2);
					String pos = word1.getPOS() + " " + word2.getPOS();
					writeTestResult(pw, entry.getKey(), o11, mi);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pw != null)
			pw.close();
	}

	public static void testLikelyHoodRatio(WordPairsManager bigramVoc,
			Vocabulary wordVoc, double confidentChiSquare) {
		Map<WordPair, Integer> bigramMap = bigramVoc.getPairMap();
		double totalWord = wordVoc.gettotalWord();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(main.main.DATA_DIRECTORY
					+ "likelyHoodRatio.csv"));
			pw.println("pair,count,score,pos");
			for (Entry<WordPair, Integer> entry : bigramMap.entrySet()) {
				long w1w2 = entry.getKey().getPair();
				int w1 = (int) (w1w2 >> 32);
				int w2 = (int) w1w2;
				if (w1 == w2)
					continue;

				double o11 = entry.getValue(); // c12
				double o_1 = wordVoc.getWordCount(w1); // c1
				double o1_ = wordVoc.getWordCount(w2); // c2
				double o12 = o1_ - o11; // count of w2 without w1
				double o21 = o_1 - o11; // count of w1 without w2
				double o22 = totalWord - o1_ - o21; // no w2 and no w1
				double e11 = (o1_ * o_1) / totalWord;
				double e12 = (o1_ * (totalWord - o_1)) / totalWord;
				double e21 = ((totalWord - o1_) * (o_1)) / totalWord;
				double e22 = (totalWord - o1_) * (totalWord - o_1) / totalWord;

				// double error = Math.log10(o12/e12);
				// double error2 = Math.log10(o21/e21);
				// double error3 = Math.log10(o22/e22);

				// System.out.println("o21: " + o21);

				if (o12 == 0)
					o12++;

				if (o21 == 0)
					o21++;

				if (o22 == 0)
					o22++;

				double loglikelihood = 2 * ((o11 * Math.log(((o11) / e11)))
						+ (o12 * Math.log((o12) / e12))
						+ (o21 * Math.log(((o21) / e21))) + (o22 * Math
						.log(((o22) / e22))));

				if (loglikelihood >= confidentChiSquare) {
					Word word1 = wordVoc.getWord(w1);
					Word word2 = wordVoc.getWord(w2);

					String pos = word1.getPOS() + " " + word2.getPOS();
					writeTestResult(pw, entry.getKey(), o11, loglikelihood);

				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pw != null)
			pw.close();
	}

	public static void testLikelyHoodRatioManning(WordPairsManager bigramVoc,
			Vocabulary wordVoc, double confidentChiSquare) {
		Map<WordPair, Integer> bigramMap = bigramVoc.getPairMap();
		double totalWord = wordVoc.gettotalWord();
		PrintWriter pw = null;

		try {
			pw = new PrintWriter(new FileWriter(main.main.DATA_DIRECTORY
					+ "likelyHoodRatioManning.csv"));
			pw.println("pair,count,score,pos");
			for (Entry<WordPair, Integer> entry : bigramMap.entrySet()) {
				long w1w2 = entry.getKey().getPair();
				int w1 = (int) (w1w2 >> 32);
				int w2 = (int) w1w2;
				if (w1 == w2)
					continue;

				double c12 = entry.getValue(); // c12
				double c1 = wordVoc.getWordCount(w1); // c1
				double c2 = wordVoc.getWordCount(w2); // c2
				if (c12 < 10 || c1 < 10 || c2 < 10)
					continue;
				double p = c2 / totalWord;
				double p1 = c12 / c1;
				// if (c2 == c12)
				c2++;
				c1++;
				double p2 = (c2 - c12) / (totalWord - c1);
				double loglikelihood = -2
						* (logL(c12, c1, p) + logL(c2 - c12, totalWord - c1, p)
								- logL(c12, c1, p1) - logL(c2 - c12, totalWord
								- c1, p2));
				if (loglikelihood >= confidentChiSquare) {
					Word word1 = wordVoc.getWord(w1);
					Word word2 = wordVoc.getWord(w2);
					String pos = word1.getPOS() + " " + word2.getPOS();
					writeTestResult(pw, entry.getKey(), c12, loglikelihood);

					entry.getKey().setTest(true, WordPair.LOG_LIKELIHOOD);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pw != null)
			pw.close();
	}

	public static void testGoogleMetric(WordPairsManager bigramVoc,
			Vocabulary wordVoc, double threshold) {
		Map<WordPair, Integer> bigramMap = bigramVoc.getPairMap();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(main.main.DATA_DIRECTORY
					+ "testGoogleMetric.csv"));
			pw.println("pair,count,score,pos");
			for (Entry<WordPair, Integer> entry : bigramMap.entrySet()) {
				long w1w2 = entry.getKey().getPair();
				int w1 = (int) (w1w2 >> 32);
				int w2 = (int) w1w2;
				if (w1 == w2)
					continue;

				double c12 = entry.getValue(); // c12
				double c1 = wordVoc.getWordCount(w1); // c1
				double c2 = wordVoc.getWordCount(w2); // c2
				// if (c12 < 10 || c1 < 10 || c2 < 10)
				// continue;
				// double p = c2 / totalWord;
				// double p1 = c12 / c1;
				// if (c2 == c12)
				// c2++;
				// c1++;
				// double p2 = (c2 - c12) / (totalWord - c1);
				double googleRatio = (c12 - threshold) / (c1 * c2);
				if (c12 > 10) {
					Word word1 = wordVoc.getWord(w1);
					Word word2 = wordVoc.getWord(w2);
					String pos = word1.getPOS() + " " + word2.getPOS();
					writeTestResult(pw, entry.getKey(), c12, googleRatio);

					entry.getKey().setTest(true, WordPair.GOOGLE_METRIC);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pw != null)
			pw.close();
	}

	// Likelihood function
	private static double logL(double k, double n, double x) {
		double result = k * Math.log(x) + (n - k) * (Math.log(1 - x));
		return result;
	}

	public static void main(String[] args) {
		String fileName = "file.txt";
		Scanner input = null;
		System.out.println("The program is reading the file...");

		// reading the file
		try {
			input = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("Could not open the file " + fileName);
			System.exit(0);
		}
		input.useDelimiter("\\Z");
		// storing the text as a string and then after removing whitespaces in
		// an array
		String str = input.next();
		String[] word;
		word = str.split("\\s+");
		int textSize = word.length / 3;
		// extracting the POS and Lemmas and storing in corresponding arrays
		String[] arrPOS = new String[textSize];
		String[] arrLemma = new String[textSize];
		// index for the POS
		int k = 1;
		// index for the Lemmas
		int l = 2;
		// the array for the POS
		for (int i = 0; i < textSize; i++) {
			if ((k >= word.length) || (l >= word.length))
				break;
			arrPOS[i] = word[k];
			arrLemma[i] = word[l];
			l = l + 3;
			k = k + 3;
		}

		// user inputs the parts of speech and distance
		Scanner keyboard = new Scanner(System.in);
		System.out
				.println("Please input the Part of Speech of the first word: ");
		String posX = keyboard.next();
		System.out
				.println("Please input the Part of Speech of the second word: ");
		String posY = keyboard.next();
		System.out
				.println("Please input the maximal distance between the words: ");
		int d = keyboard.nextInt();
		String[] arrTmp = arrLemma.clone();
		List<String> lemmas = Arrays.asList(arrTmp);
		HashMap<String, Integer> lemmasCount = new HashMap<String, Integer>();
		HashMap<String, Integer[]> mapo11 = new HashMap<String, Integer[]>();
		Collections.sort(lemmas);
		System.out
				.println("The program is extracting collocations from a corpus."
						+ " This might take a few minutes...Facebook is not any "
						+ "faster either! ");
		outer: for (int i = 0; i < arrPOS.length; i++) {
			if (arrPOS[i].equals(posX))
				for (int j = i + 1; j < d + i + 1; j++) {
					if (j >= arrPOS.length)
						continue outer;
					if ((arrPOS[i].equals(posX)) && (arrPOS[j].equals(posY))) {
						String o11 = arrLemma[i] + " " + arrLemma[j];
						// if (o11.equals("new year"))
						// System.out.println(i + " " + arrLemma[i] + " " +j +
						// " "+ arrLemma[j] + " " + posX + " " + posY);
						if (mapo11.containsKey(o11)) {
							// System.out.println(o11);
							Integer[] arr = mapo11.get(o11);
							arr[0] += 1;

							mapo11.put(o11, arr);
						} else {

							Integer[] counters = new Integer[3];
							counters[0] = 1;
							if (!lemmasCount.containsKey(arrLemma[i])) {
								counters[1] = countLemma(arrLemma[i], lemmas,
										lemmasCount);
								lemmasCount.put(arrLemma[i], counters[1]);
							} else
								counters[1] = lemmasCount.get(arrLemma[i]);

							if (!lemmasCount.containsKey(arrLemma[j])) {
								counters[2] = countLemma(arrLemma[j], lemmas,
										lemmasCount);
								lemmasCount.put(arrLemma[j], counters[2]);
							} else
								counters[2] = lemmasCount.get(arrLemma[j]);

							mapo11.put(o11, counters);
						}
					}
				}
			else
				continue outer;
		}

		// calculating t statistics
		HashMap<String, Double> tStats = new HashMap<String, Double>();
		System.out.println("Calculating t statistics... ");
		tStatistic(mapo11, tStats, textSize);
		System.out.println("Sorting the results for t statistics... ");
		LinkedHashMap<String, Double> ordered = new LinkedHashMap<String, Double>();
		// ordered = sortHashMapByValues(tStats, false);
		Set ref = ordered.keySet();
		Iterator it = ref.iterator();
		int countTop10 = 0;
		while (it.hasNext()) {
			if (countTop10 == 10)
				break;
			String wordKey = (String) it.next();
			System.out.println("The collocation: " + wordKey
					+ " and the t statistic is: " + ordered.get(wordKey));
			countTop10++;
		}

		// calculating x2 score
		HashMap<String, Double> x2scoreStats = new HashMap<String, Double>();
		System.out.println("Calculating x2 score... ");
		x2score(mapo11, x2scoreStats, textSize);
		System.out.println("Sorting the results for x2 score... ");
		LinkedHashMap<String, Double> orderedX2Score = new LinkedHashMap<String, Double>();
		// orderedX2Score = sortHashMapByValues(x2scoreStats, false);
		Set ref2 = orderedX2Score.keySet();
		Iterator it2 = ref2.iterator();
		countTop10 = 0;
		while (it2.hasNext()) {
			if (countTop10 == 10)
				break;
			String wordKey = (String) it2.next();
			System.out.println("The collocation: " + wordKey
					+ " and the x2 score is: " + orderedX2Score.get(wordKey));
			countTop10++;
		}

		// calculating log likelihood ratio
		HashMap<String, Double> logs = new HashMap<String, Double>();
		System.out.println("Calculating log likelihood ratios... ");
		logRatio(mapo11, logs, textSize);
		System.out.println("Sorting the results for log likelihood ratios... ");
		LinkedHashMap<String, Double> orderedLogs = new LinkedHashMap<String, Double>();
		// orderedLogs = sortHashMapByValues(logs, false);
		Set ref3 = orderedLogs.keySet();
		Iterator it3 = ref3.iterator();
		countTop10 = 0;
		while (it3.hasNext()) {
			if (countTop10 == 10)
				break;
			String wordKey = (String) it3.next();
			System.out.println("The collocation: " + wordKey
					+ " and the log likelihood ratio is: "
					+ orderedLogs.get(wordKey));
			countTop10++;
		}

		// calculating the pointwise mutual information
		HashMap<String, Double> pmis = new HashMap<String, Double>();
		System.out
				.println("Calculating the pointwise mutual information scores... ");
		pmi(mapo11, pmis, textSize);
		System.out
				.println("Sorting the results for the pointwise mutual information scores... ");
		LinkedHashMap<String, Double> orderedPMI = new LinkedHashMap<String, Double>();
		// orderedPMI = sortHashMapByValues(pmis, false);
		Set ref4 = orderedPMI.keySet();
		Iterator it4 = ref4.iterator();
		countTop10 = 0;
		while (it4.hasNext()) {
			if (countTop10 == 10)
				break;
			String wordKey = (String) it4.next();
			System.out.println("The collocation: " + wordKey
					+ " and the pointwise mutual information score is: "
					+ orderedPMI.get(wordKey));
			countTop10++;
		}

	}

	// a method to count the number of occurence of lemmas
	public static int countLemma(String lemma, List<String> arrayLemma,
			HashMap<String, Integer> lemmasCount) {
		int firstOccurence = arrayLemma.indexOf(lemma);
		// System.out.println("First occurence: " + firstOccurence);
		int lastOccurence = arrayLemma.lastIndexOf(lemma);
		// System.out.println("Last occurence: " + lastOccurence);
		int count = lastOccurence - firstOccurence + 1;
		// lemmasCount.put(lemma, count);
		return count;
	}

	// a method to compute the t statistic
	private static void tStatistic(HashMap<String, Integer[]> data,
			HashMap<String, Double> results, int textSize) {
		Collection c = data.keySet();
		Iterator itr = c.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			Integer[] o = data.get(key);
			double o11 = o[0];
			double o_1 = o[1];
			double o1_ = o[2];

			double e11 = (o1_ * o_1) / textSize;
			double t = (o11 - e11) / Math.sqrt(o11);
			results.put(key, t);
		}

	}

	private static void x2score(HashMap<String, Integer[]> data,
			HashMap<String, Double> results, int textSize) {
		Collection c = data.keySet();
		Iterator itr = c.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			Integer[] o = data.get(key);
			double o11 = o[0];
			double o_1 = o[1];
			double o1_ = o[2];
			double o12 = o1_ - o11;
			double o21 = o_1 - o11;
			double o22 = textSize - o1_ - o21;
			double e11 = (o_1 * o1_) / textSize;
			double e12 = (o1_ * (textSize - o_1)) / textSize;
			double e21 = ((textSize - o1_) * (o_1)) / textSize;
			double e22 = (textSize - o1_) * (textSize - o_1) / textSize;

			double x2 = ((Math.pow((o11 - e11), 2)) / e11)
					+ ((Math.pow((o12 - e12), 2)) / e12)
					+ ((Math.pow((o21 - e21), 2)) / e21)
					+ ((Math.pow((o22 - e22), 2)) / e22);

			results.put(key, x2);

		}

	}

	// a method to compute the log likelihood ratios
	private static void logRatio(HashMap<String, Integer[]> data,
			HashMap<String, Double> results, int textSizeI) {
		Collection c = data.keySet();
		Iterator itr = c.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			Integer[] o = data.get(key);
			double textSize = textSizeI;
			double o11 = o[0]; // c12
			double o_1 = o[1]; // c1
			double o1_ = o[2]; // c2
			double o12 = o1_ - o11; // count of w2 without w1
			double o21 = o_1 - o11; // count of w1 without w2
			double o22 = textSize - o1_ - o21; // no w2 and no w1
			double e11 = (o1_ * o_1) / textSize;
			double e12 = (o1_ * (textSize - o_1)) / textSize;
			double e21 = ((textSize - o1_) * (o_1)) / textSize;
			double e22 = (textSize - o1_) * (textSize - o_1) / textSize;

			// double error = Math.log10(o12/e12);
			// double error2 = Math.log10(o21/e21);
			// double error3 = Math.log10(o22/e22);

			// System.out.println("o21: " + o21);

			if (o12 == 0)
				o12++;

			if (o21 == 0)
				o21++;

			if (o22 == 0)
				o22++;

			double l = 2 * ((o11 * Math.log10(((o11) / e11)))
					+ (o12 * Math.log10((o12) / e12))
					+ (o21 * Math.log10(((o21) / e21))) + (o22 * Math
					.log10(((o22) / e22))));
			results.put(key, l);

		}

	}

	// a method to compute the pointwise mutual information score
	private static void pmi(HashMap<String, Integer[]> data,
			HashMap<String, Double> results, int textSize) {
		Collection c = data.keySet();
		Iterator itr = c.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			Integer[] o = data.get(key);
			double o11 = o[0];
			double o_1 = o[1];
			double o1_ = o[2];

			double e11 = (o1_ * o_1) / textSize;
			double mi = Math.log10(o11 / e11);
			results.put(key, mi);
		}

	}

	// a method to sort the results by descending order
	public static LinkedHashMap<Long, Integer> sortHashMapByValues(
			Map passedMap, boolean ascending) {

		List mapKeys = new ArrayList(passedMap.keySet());
		List mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		if (!ascending)
			Collections.reverse(mapValues);

		LinkedHashMap someMap = new LinkedHashMap();
		Iterator valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Object val = valueIt.next();
			Iterator keyIt = mapKeys.iterator();
			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				if (passedMap.get(key).toString().equals(val.toString())) {
					passedMap.remove(key);
					mapKeys.remove(key);
					someMap.put(key, val);
					break;
				}
			}
		}
		return someMap;
	}

}
