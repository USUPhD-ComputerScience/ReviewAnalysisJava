package Analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import org.json.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;

import model.Application;
import model.Review;
import model.Vocabulary;
import util.PostgreSQLConnector;
import util.Util;
import Managers.ApplicationManager;
import NLP.SymSpell;

public class AnalyserForVoc {
	private static final String URBAN_DIC_API = "http://api.urbandictionary.com/v0/define?term=";

	private static final HashMap<String, String[]> correctionMap = new HashMap<>();

	private static void loadCorrectionMap(File file)
			throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNextLine()) {
			String[] pair = br.nextLine().split(",");
			if (pair.length == 2)
				correctionMap.put(pair[0], pair[1].split(" "));

		}
		br.close();
	}

	public static void main(String[] args) throws Throwable {
		// HashSet<String> blackList = new HashSet<>();
		// loadBlackList(blackList, new File("blackList.txt"));
		HashMap<String, Integer> inWordCount = new HashMap<>();
		loadCorrectionMap(new File("E:\\dictionary\\Map\\wordMapper.txt"));
		System.out.println("Word Mapper loaded!!!");
		loadDictionary(inWordCount,
				new File("E:\\dictionary\\improvised\\").listFiles());
		System.out.println("Dictionary loaded!!!");
		HashMap<String, Integer> outWordCount = new HashMap<>();
		PostgreSQLConnector db = null;
		int count = 0;
		long startTime = System.nanoTime();
		db = new PostgreSQLConnector(PostgreSQLConnector.DBLOGIN,
				PostgreSQLConnector.DBPASSWORD, PostgreSQLConnector.REVIEWDB);
		String fields[] = { "title", "text" };
		ResultSet results;
		results = db.select(PostgreSQLConnector.REVIEWS_TABLE, fields, null);
		while (results.next()) {
			count++;
			String text = results.getString("text");
			if (text.indexOf('\t') < 0) // Not from Android Market
				text = results.getString("title") + "." + text;
			text = text.toLowerCase();
			String[] words = text.split("[^a-z']+");

			ArrayList<String> wordList = new ArrayList<>();
			for (String word : words) {
				if (word.equals("null") || word.length() < 2)
					continue;
				String[] wordarray = correctionMap.get(word);
				if (wordarray != null)
					wordList.addAll(Arrays.asList(wordarray));
				else
					wordList.add(word);
			}
			double totalScore = 0, goodScore = 0;
			for (String word : wordList) {
				Integer wCount = inWordCount.get(word);
				double score = 1.0;
				if (wCount != null) {
					// score /= Math.log(wCount);
					goodScore += score;
				}

				totalScore += score;
			}
			double proportion = goodScore / totalScore;

			if (proportion > 0.5)
				for (String word : wordList) {
					if (word.length() < 2)
						continue;
					Integer wCount = inWordCount.get(word);
					if (wCount != null)
						inWordCount.put(word, wCount + 1); // in dictionary
					else {
						wCount = outWordCount.get(word);
						if (wCount == null) // out of voc
							wCount = 0;
						outWordCount.put(word, wCount + 1);
					}
				}

			if (count % 10000 == 0) {
				long stopTime = System.nanoTime();
				long duration = stopTime - startTime;
				startTime = stopTime;
				System.out.println("Reviews processed: " + count
						+ ", time passed since last message: "
						+ (duration / 1000000) + " milliseconds");
			}

		}

		db.close();

		writeToFile(
				inWordCount,
				"E:\\AndroidAnalysis\\ReviewData\\dictionaryProcessing\\InWordCount",
				1, false);
		writeToFile(
				outWordCount,
				"E:\\AndroidAnalysis\\ReviewData\\dictionaryProcessing\\OutWordCount",
				10, false);
		System.out.println("Done!!!");
	}

	private static void loadBlackList(HashSet<String> blackList, File file)
			throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNext())
			blackList.add(br.next().toLowerCase());
		br.close();
	}

	private static void writeToFile(HashMap<String, Integer> inWordCount,
			String fileName, int minCount, boolean urbanCheck) throws Throwable {
		ArrayList<Map.Entry<String, Integer>> wordList = new ArrayList<>(
				inWordCount.entrySet());
		Collections.sort(wordList,
				new Comparator<Map.Entry<String, Integer>>() {
					@Override
					public int compare(Entry<String, Integer> o1,
							Entry<String, Integer> o2) {
						return o2.getValue() - o1.getValue();
					}
				});
		PrintWriter pwDict = new PrintWriter(fileName + "_DICT.txt");
		PrintWriter pwNonDict = null;
		SymSpell symspell = SymSpell.getInstance();
		if (urbanCheck)
			pwNonDict = new PrintWriter(fileName + "_NonDICT.txt");
		for (Map.Entry<String, Integer> entry : wordList) {
			int count = entry.getValue();
			if (urbanCheck) {
				if (count >= minCount) {
					String w = entry.getKey();
					if (checkExistanceWithUrbanDictionary(entry.getKey()))
						pwDict.println(w
								+ ","
								+ count
								+ ","
								+ symspell.correctThisWord(w,
										SymSpell.LANGUAGE, false));
					else {
						pwNonDict.println(entry.getKey()
								+ ","
								+ count
								+ ","
								+ symspell.correctThisWord(w,
										SymSpell.LANGUAGE, false));
					}
				}
			} else if (count >= minCount) {

				String w = entry.getKey();
				pwDict.println(w + "," + count + ","
						+ symspell.correctThisWord(w, SymSpell.LANGUAGE, false));
			}
		}
		pwDict.close();
	}

	private static void loadDictionary(HashMap<String, Integer> dic,
			File[] fileLists) throws Exception {
		for (File file : fileLists) {
			Scanner br = new Scanner(new FileReader(file));
			while (br.hasNext())
				dic.put(br.next(), 0);
			br.close();
		}
	}

	private static boolean checkExistanceWithUrbanDictionary(String word)
			throws Throwable {
		TimeUnit.SECONDS.sleep(1);
		String URLstr = URBAN_DIC_API + word;
		URL url = new URL(URLstr);
		Scanner scan = new Scanner(url.openStream());
		StringBuilder str = new StringBuilder();
		while (scan.hasNextLine())
			str.append(scan.nextLine());
		scan.close();
		JSONObject obj = new JSONObject(str.toString());
		if (obj.getString("result_type").equals("no_results"))
			return false;
		else
			return true;
	}
}
