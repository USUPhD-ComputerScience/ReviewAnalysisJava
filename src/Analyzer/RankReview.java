package Analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import model.Application;
import model.Review;
import model.Vocabulary;
import model.WordPair;
import Managers.ApplicationManager;
import Managers.WordPairsManager;

public class RankReview {

	public static final String DIR = "E:\\AndroidAnalysis\\ReviewData\\data\\v17\\";

	public static void main(String[] args) throws Throwable {
		System.out.println(">> Starting...");
		init();
		// readCollocations(new File(DIR + "testGoogleMetric.csv"));
		PrintWriter pw = new PrintWriter(DIR + "rankedReviewsGG.csv");
		pw.println("significant Pair,tfidf,review");
		Map<WordPair, Integer> pairVoc = WordPairsManager.getInstance()
				.getPairMap();
		System.out.println(">> Writing ranked review...");
		int revcount = 0, pairCount = 0;
		for (Application app : ApplicationManager.getInstance().getAppSet()) {
			for (Review rev : app.getReviews()) {
				revcount++;
				String significantPair = "";
				double significantScore = 0.0;
				HashMap<WordPair, Integer> pairDF = rev.getPairMap();
				for (Entry<WordPair, Integer> pair : pairDF.entrySet()) {
					pairCount++;
					if (!pair.getKey().isChoosenBy(1))
						continue;
					Integer TF = pairVoc.get(pair.getKey());
					if (TF == null)
						continue;
					double TFIDF = (double) TF
							/ (1 + Math.log(pair.getValue()));
					if (TFIDF > significantScore) {
						significantPair = pair.getKey().toString();
						significantScore = TFIDF;
					}
				}
				if (significantScore > 0) {
					pw.println(significantPair + "," + significantScore
							* rev.toString().split(" ").length + ","
							+ rev.toString());
				}
			}
		}
		pw.close();
		System.out.println(">> Done with " + revcount + " reviews and "
				+ pairCount + " pairs.");
	}

	public static void readCollocations(File file) throws Throwable {
		Scanner br = new Scanner(new FileReader(file));

		Vocabulary wordVoc = Vocabulary.getInstance();
		Map<WordPair, Integer> pairVoc = WordPairsManager.getInstance()
				.getPairMap();
		while (br.hasNext()) {
			String[] values = br.nextLine().split(",");
			if (values.length == 4) {
				String[] pair = values[0].split(" ");
				if (pair.length == 2) {
					WordPair choosenPair = new WordPair(
							wordVoc.getWordID(pair[0]),
							wordVoc.getWordID(pair[1]), values[3].split(" "));
					Integer frq = pairVoc.get(choosenPair);
					if (frq != null) {
						// collocations.put(choosenPair, frq);
					}
				}
			}
		}
		br.close();
	}

	public static void init() {
		Vocabulary.getInstance();
		ApplicationManager.getInstance();
		WordPairsManager.getInstance();
	}
}
