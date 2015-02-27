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

import java.util.Comparator;
import java.util.HashMap;
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

public class ReportWord {
	public static void main(String[] args) throws Throwable {
		report("sayee");
	}
	public static void report(String word) throws Throwable {
		PostgreSQLConnector db = null;
		int count = 0;
		long startTime = System.nanoTime();
		db = new PostgreSQLConnector(PostgreSQLConnector.DBLOGIN,
				PostgreSQLConnector.DBPASSWORD, PostgreSQLConnector.REVIEWDB);
		String fields[] = { "title", "text" ,"appid"};
		ResultSet results;
		results = db.select(PostgreSQLConnector.REVIEWS_TABLE, fields, null);
		PrintWriter pw = new PrintWriter("\\AndroidAnalysis\\ReviewData\\Strangeword\\" + word + "_Review.txt");
		while (results.next()) {
			String text = results.getString("text");
			if (text.indexOf('\t') < 0) // Not from Android Market
				text = results.getString("title") + "." + text;
			count++;
			String lwctext = text.toLowerCase();
			String[] words = lwctext.split("[^a-z']+");
			
			boolean contain = false;
			for (String temp : words) {
				if (temp.equals(word)) {
					contain = true;
					break;
				}
			}
			
			if (contain) {
				pw.print(results.getInt("appid")+" ");
				pw.println(text);
				for (int i = 0; i < words.length; i++) {
					if (words[i].length() <= 0) continue;
					if (i > 0)
						pw.append(' ');
					if (words[i].equals(word)) 
						pw.append(word.toUpperCase());
					else
						pw.append(words[i]);				
				}
				pw.println('.');
			}

			if (count % 100000 == 0) {
				long stopTime = System.nanoTime();
				long duration = stopTime - startTime;
				startTime = stopTime;
				System.out.println("Reviews processed: " + count
						+ ", time passed since last message: "
						+ (duration / 1000000) + " milliseconds");
			}

		}

		db.close();
		pw.close();
		System.out.println("Done!!!");
	}

	
}
