package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Managers.ApplicationManager;
import Managers.WordPairsManager;
import NLP.StatisticalTests;
import util.PostgreSQLConnector;
import model.Application;
import model.Review;
import model.Vocabulary;

public class main {

	public static void main(String[] args) throws ClassNotFoundException,
			IOException {
		// TODO Auto-generated method stub
		init();
		long startTime = System.nanoTime();
		readReviews();
		long stopTime = System.nanoTime();
		System.out.println("Time taken to process new reviews: "
				+ ((double) (stopTime - startTime) / 1000000 / 1000 / 60)
				+ " minutes");
		startTime = System.nanoTime();
		int count = WordPairsManager.getInstance().findPossiblePairs();
		stopTime = System.nanoTime();
		System.out.println("Time taken to find new word pairs: "
				+ ((double) (stopTime - startTime) / 1000000 / 1000 / 60)
				+ " minutes");
		System.out.println("Total pairs = " + count);
		System.out.print("T-Test...");
		StatisticalTests.getInstance().tTest(WordPairsManager.getInstance(),
				Vocabulary.getInstance(), 2.576);
		StatisticalTests.getInstance()
				.testLikelyHoodRatio(WordPairsManager.getInstance(),
						Vocabulary.getInstance(), 7.879);
		System.out.println("-Done");
		// Vocabulary.getInstance().clusterWords();
		Vocabulary.getInstance().writeWordsToFile(
				"\\AndroidAnalysis\\ReviewData\\data\\word.csv");
		close(ApplicationManager.FILENAME, Vocabulary.FILENAME,
				WordPairsManager.FILENAME);
	}

	public static void init() {
		Vocabulary.getInstance();
		ApplicationManager.getInstance();
		WordPairsManager.getInstance();
	}

	private static void close(String appManagerFile, String vocFile,
			String pairsFile) throws IOException {
		writeSentenceToFile("\\AndroidAnalysis\\ReviewData\\data\\reviewDataSet.txt");
		// write application manager (all apps, reviews, sentences)
		System.err.println(">>Write Application Manager object to file:"
				+ appManagerFile);
		ApplicationManager appManager = ApplicationManager.getInstance();
		FileOutputStream fout = new FileOutputStream(appManagerFile);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(appManager);
		// write vocabulary (all words)
		System.err.println(">>Write Vocabulary object to file:" + vocFile);
		Vocabulary voc = Vocabulary.getInstance();
		fout = new FileOutputStream(vocFile);
		oos = new ObjectOutputStream(fout);
		oos.writeObject(voc);
		// write Word Pair List

		System.err.println(">>Write Pairs of Word data to file:" + pairsFile);
		WordPairsManager wpm = WordPairsManager.getInstance();
		fout = new FileOutputStream(pairsFile);
		oos = new ObjectOutputStream(fout);
		oos.writeObject(wpm);
	}

	private static void readReviews() {
		ApplicationManager appManager = ApplicationManager.getInstance();
		PostgreSQLConnector db = null;
		int count = 0;
		try {
			System.out.println(">>Update new applications.");
			db = new PostgreSQLConnector(PostgreSQLConnector.DBLOGIN,
					PostgreSQLConnector.DBPASSWORD,
					PostgreSQLConnector.REVIEWDB);

			String fields[] = { "ID", "name" };
			String condition = null;
			condition = "count>0";
			ResultSet results;
			results = db.select(PostgreSQLConnector.APPID_TABLE, fields,
					condition);
			while (results.next()) {
				String appID = results.getString("name");
				Integer dbID = results.getInt("ID");
				if (appID != null && dbID != null) {
					if (appManager.addNewApp(appID, dbID))
						count++;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		System.out.println(">>Total new applications added: " + count);
		System.out.println(">>Total applications: "
				+ appManager.applicationsNumber());
		System.out.println(">>Update new reviews.");
		count = appManager.fillAppsWithReviews();
		System.out.println("Total new reviews added: " + count);
		System.out.println("Total reviews: " + appManager.reviewsNumber());
	}

	private static void writeSentenceToFile(String fileName) {
		System.err.println(">>Write sentences to file for Word2Vec: "
				+ fileName);
		ApplicationManager appManager = ApplicationManager.getInstance();

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(fileName, true));
			appManager.writeSentenceToFile(pw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (pw != null)
				pw.close();
		}
	}

}
