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

public class NewVocAnalyser {
	private static final String URBAN_DIC_API = "http://api.urbandictionary.com/v0/define?term=";
	private static final HashMap<String, Integer> inWordCount = new HashMap<>();
	private static final HashMap<String, String[]> correctionMap = new HashMap<>();

	public static void main(String[] args) throws Throwable {
		loadCorrectionMap(new File("wordMap.txt"));
		loadDictionary(new File("E:\\dictionary\\improvised\\").listFiles());
		System.out.println("Dictionary loaded!!!");
		countWord();
		System.out.println("Done counting words!!!");
		filterReview();
		System.out.println("Done filtering bad reviews!!!");
	}

	private static void filterReview() throws Throwable {
		long startTime = System.nanoTime();
		int count = 0;

		PostgreSQLConnector db = new PostgreSQLConnector(PostgreSQLConnector.DBLOGIN,
				PostgreSQLConnector.DBPASSWORD, PostgreSQLConnector.REVIEWDB);
		String fields[] = { "title", "text" };
		ResultSet results;
		results = db.select(PostgreSQLConnector.REVIEWS_TABLE, fields, null);
		PrintWriter pw = new PrintWriter(
				"\\AndroidAnalysis\\ReviewData\\StrangeReviews\\strangeReview.txt");
		pw.println("count,ratio,sentence");
		while (results.next()) {
			count++;
			String text = results.getString("text");
			if (text.indexOf('\t') < 0) // Not from Android Market
				text = results.getString("title") + "." + text;		
			text = text.toLowerCase();
			String[] words = text.split("[^a-z']+");
			ArrayList<String> wordList = new ArrayList<>();
			for (String word : words) {
				if (word.equals("null") || word.length() < 1)
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
					score /= Math.log(wCount);
					goodScore += score;
				}
				
				totalScore += score;
			}
			double proportion = goodScore / totalScore;
			if (proportion < 0.5)
				pw.println(wordList.size() + "," + proportion + ","
						+ "\"" + wordList.toString() + "\"" );

			if (count % 10000 == 0) {
				long stopTime = System.nanoTime();
				long duration = stopTime - startTime;
				startTime = stopTime;
				System.out.println("Reviews processed: " + count
						+ ", time passed since last message: "
						+ (duration / 1000000) + " milliseconds");
			}

		}
		pw.close();
		db.close();
	}

	private static void countWord() throws Throwable {
		PostgreSQLConnector db = new PostgreSQLConnector(
				PostgreSQLConnector.DBLOGIN, PostgreSQLConnector.DBPASSWORD,
				PostgreSQLConnector.REVIEWDB);
		String fields[] = { "title", "text" };
		ResultSet results;
		results = db.select(PostgreSQLConnector.REVIEWS_TABLE, fields, null);

		while (results.next()) {
			String text = results.getString("text");
			if (text.indexOf('\t') < 0) // Not from Android Market
				text = results.getString("title") + "." + text;

			text = text.toLowerCase();
			String[] words = text.split("[^a-z']+");
			ArrayList<String> wordList = new ArrayList<>();
			for (String word : words) {
				if (word.equals("null") || word.length() < 1)
					continue;

				String[] wordarray = correctionMap.get(word);
				if (wordarray != null)
					wordList.addAll(Arrays.asList(wordarray));
				else
					wordList.add(word);
			}
			for (String word : wordList) {
				Integer wCount = inWordCount.get(word);
				if (wCount != null)
					inWordCount.put(word, wCount + 1); // in dictionary
			}
		}
		db.close();

	}

	private static void filterBadWord(HashMap<String, Integer> dictionary,
			HashSet<String> blackList) throws Throwable {
		HashSet<String> newDictionary = new HashSet<>();
		for (Entry<String, Integer> word : dictionary.entrySet()) {
			if (!blackList.contains(word.getKey())) {
				newDictionary.add(word.getKey());
			}
		}
		ArrayList<String> wordList = new ArrayList<>(newDictionary);
		Collections.sort(wordList);
		PrintWriter pw = new PrintWriter(
				"E:\\dictionary\\improvised\\eng_com_2.dic");
		for (String word : wordList)
			pw.println(word);
		pw.close();
	}

	private static void loadBlackList(HashSet<String> blackList, File file)
			throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNext())
			blackList.add(br.next().toLowerCase());
		br.close();
	}

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
			} else if (count >= minCount)
				pwDict.println(entry.getKey() + "," + count);
		}
		pwDict.close();
	}

	private static void loadDictionary(File[] fileLists) throws Exception {
		for (File file : fileLists) {
			Scanner br = new Scanner(new FileReader(file));
			while (br.hasNext())
				inWordCount.put(br.next(), 0);
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
