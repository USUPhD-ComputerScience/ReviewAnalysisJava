package NLP;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import model.Vocabulary;
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
		Map<Long, Integer> bigramMap = bigramVoc.getPairMap();
		int totalPair = bigramVoc.getTotalPair();
		int totalWord = wordVoc.gettotalWord();
		double tTest = 0;
		double h0 = 0;
		double xBar = 0;

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(main.main.DATA_DIRECTORY+"ttest.csv"));
			for (Entry<Long, Integer> entry : bigramMap.entrySet()) {
				long w1w2 = entry.getKey();
				int w1 = (int) (w1w2 >> 32);
				int w2 = (int) w1w2;
				if (w1 == w2)
					continue;
				// int w1 = entry.getKey().getFirstWord();
				// int w2 = entry.getKey().getSecondWord();
				xBar = (double) entry.getValue() / totalPair;
				h0 = (double) (wordVoc.getWordCount(w1) * wordVoc
						.getWordCount(w2)) / ((double) totalWord * totalWord);
				tTest = (xBar - h0) / Math.sqrt(xBar / totalPair);

				if (tTest >= t) {
					pw.write(wordVoc.getWord(w1).toString());
					pw.write(" ");
					pw.write(wordVoc.getWord(w2).toString());
					pw.write(",");
					pw.write(entry.getValue() + "," + tTest);

					pw.write('\n');
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
		Map<Long, Integer> bigramMap = bigramVoc.getPairMap();
		int totalWord = wordVoc.gettotalWord();
		double p = 0;
		double p1 = 0;
		double p2 = 0;
		double logLikelyHood = 0;
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(main.main.DATA_DIRECTORY+"likelyHoodRatio.csv"));
			for (Entry<Long, Integer> entry : bigramMap.entrySet()) {
				long w1w2 = entry.getKey();
				int w1 = (int) (w1w2 >> 32);
				int w2 = (int) w1w2;
				if (w1 == w2)
					continue;
				double c1 = wordVoc.getWordCount(w1);
				double c2 = wordVoc.getWordCount(w2);
				double c12 = entry.getValue();
				p = c2 / totalWord;
				p1 = c12 / c1;
				p2 = (c2 - c12) / (totalWord - c1);
				logLikelyHood = Math.log(L(c12, c1, p))
						+ Math.log(L(c2 - c12, totalWord - c1, p))
						- Math.log(L(c12, c1, p1))
						- Math.log(L(c2 - c12, totalWord - c1, p2));
				double chiSquareEquivalence = -2 * logLikelyHood;
				if (chiSquareEquivalence >= confidentChiSquare)
					pw.println(wordVoc.getWord(w1).toString() + " "
							+ wordVoc.getWord(w2).toString() + ","
							+ entry.getValue() + "," + chiSquareEquivalence);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pw != null)
			pw.close();
	}

	// Likelihood function
	private static double L(double k, double n, double x) {
		return Math.pow(x, k) * Math.pow(1 - x, n - k);
	}
}
