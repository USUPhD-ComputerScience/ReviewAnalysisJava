package Analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Map.Entry;

import crawlers.GooglePlayCrawler;
import util.PostgreSQLConnector;

public class DictionaryProcessor {
	public static final String LOGIN = "phongvm90@gmail.com";
	public static final String PASSWORD = "dcmdnacmkct";
	public static final String ANDROID = "3FA8A9EFF6CA06E0";
	private static final HashMap<String, Integer> dictionary = new HashMap<>();
	private static final HashMap<String, String[]> correctionMap = new HashMap<>();
	private static final HashSet<String> newWords = new HashSet<>();

	public static void main(String[] args) throws Throwable {
		loadCorrectionMap(new File("E:\\dictionary\\Map\\wordMapper.txt"));
		System.out.println("Word Mapper loaded!!!");
		loadDictionary(new File("E:\\dictionary\\improvised\\eng_com.dic"));
		System.out.println("Dictionary loaded!!!");
		loadNewWords(new File("E:\\dictionary\\improvised\\newword.txt"));
		System.out.println("New Words loaded!!!");

		for (Entry<String, String[]> entry : correctionMap.entrySet()) {
			for (String word : entry.getValue()) {
				if (!dictionary.containsKey(word)) {
					newWords.add(word);
				}
			}
		}
		writeNewWords("E:\\dictionary\\improvised\\newword.txt");
		System.out.println("Write new words! Done");

		//getNewWordsFromDesc("E:\\dictionary\\improvised\\newword_DESC.txt");
		//System.out.println("Write new words from descriptions! Done");
	}

	private static void getNewWordsFromDesc(String fileName) throws Throwable {
		HashSet<String> newWordsFromDesc = new HashSet<>();
		GooglePlayCrawler GPcrawler = new GooglePlayCrawler(LOGIN, PASSWORD,
				ANDROID);
		PostgreSQLConnector db = new PostgreSQLConnector(
				PostgreSQLConnector.DBLOGIN, PostgreSQLConnector.DBPASSWORD,
				PostgreSQLConnector.REVIEWDB);

		String fields[] = { "name" };
		String condition = "count>0";

		ResultSet results = db.select(PostgreSQLConnector.APPID_TABLE, fields,
				condition);
		long startTime = System.nanoTime();
		int count = 0;
		while (results.next()) {
			String information = GPcrawler.getNameAndPermision(results
					.getString("name"));
			information = information.toLowerCase();
			String[] words = information.split("[^a-z']+");
			for (String word : words) {
				if (word.length() < 2)
					continue;
				if (!dictionary.containsKey(word)
						&& !correctionMap.containsKey(word)
						&& !newWords.contains(word)) {
					newWordsFromDesc.add(word);
				}
			}

			count++;
			if (count % 10000 == 0) {
				long stopTime = System.nanoTime();
				long duration = stopTime - startTime;
				startTime = stopTime;
				System.out.println("Descriptions processed: " + count
						+ ", time passed since last message: "
						+ (duration / 1000000) + " milliseconds");
			}
		}
		ArrayList<String> wordList = new ArrayList<>(newWordsFromDesc);
		Collections.sort(wordList);
		PrintWriter pw = new PrintWriter(fileName);
		for (String word : wordList)
			pw.println(word);
		pw.close();

	}

	private static void writeNewWords(String fileName) throws Throwable {

		ArrayList<String> wordList = new ArrayList<>(newWords);
		Collections.sort(wordList);
		PrintWriter pw = new PrintWriter(fileName);
		for (String word : wordList)
			pw.println(word);
		pw.close();
	}

	private static void loadDictionary(File file) throws Exception {
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNext())
			dictionary.put(br.next(), 0);
		br.close();

	}

	private static void loadNewWords(File file) throws Exception {
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNext())
			newWords.add(br.next());
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

}
