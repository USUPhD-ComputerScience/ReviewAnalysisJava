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
		report("redmi");
		report("aplikasi");
		report("bekar");
		report("yadav");
		report("babbel");
		report("encanta");
		report("boleh");
		report("untuk");
		report("vystar");
		report("usar");
		report("siii");
		report("fleksy");
		report("hebat");
		report("cacu");
		report("banget");
		report("tiempo");
		report("mudah");
		report("tiene");
		report("kvue");
		report("buen");
		report("tengo");
		report("ahora");
		report("galing");
		report("juego");
		report("gionee");
		report("adik");
		report("lumayan");
		report("falta");
		report("memang");
		report("melhor");
		report("versi");
		report("trulia");
		report("puede");
		report("sirve");
		report("itriage");
		report("recomiendo");
		report("membantu");
		report("owsm");
		report("aplicativo");
		report("iconia");
		report("baat");
		report("geniego");
		report("lebih");
		report("lovr");
		report("desde");
		report("accha");
		report("biasa");
		report("jazakallah");
		report("semua");
		report("cuenta");
		report("dengan");
		report("uygulama");
		report("sekali");
		report("minuum");
		report("picsart");
		report("bakvas");
		report("hvfcu");
		report("dapat");
		report("gotomypc");
		report("medco");
		report("terbaek");
		report("senang");
		report("oyun");
		report("gambar");
		report("bakit");
		report("vidio");
		report("cantik");
		report("mensajes");
		report("karbonn");
		report("problemas");
		report("walang");
		report("verson");
		report("seronok");
		report("cepat");
		report("mashaallah");
		report("khub");
		report("yatse");
		report("avtech");
		report("menarik");
		report("foarte");
		report("hoti");
		report("bastante");
		report("bagal");
		report("nakaka");
		report("dalam");
		report("jangan");
		report("musica");
		report("ossam");
		report("todas");
		report("dise");
		report("ipolis");
		report("kahit");
		report("bhut");
		report("mujhe");
		report("punya");
		report("ikaw");
		report("jazeera");
		report("trucos");
		report("memuaskan");
		report("pura");
		report("trabajo");
		report("aplikacija");
		report("atualiza");
		report("kadak");
		report("kalau");
		report("parece");
		report("subhanallah");
		report("tiwari");
		report("zabardast");
		report("espero");
		report("estaba");
		report("ekdam");
		report("sath");
		report("siip");
		report("trbaik");
		report("dantdm");
		report("duolingo");
	}
	public static void report(String word) throws Throwable {
		PostgreSQLConnector db = null;
		int count = 0;
		long startTime = System.nanoTime();
		db = new PostgreSQLConnector(PostgreSQLConnector.DBLOGIN,
				PostgreSQLConnector.DBPASSWORD, PostgreSQLConnector.REVIEWDB);
		String fields[] = { "title", "text" };
		ResultSet results;
		results = db.select(PostgreSQLConnector.REVIEWS_TABLE, fields, null);
		PrintWriter pw = new PrintWriter("\\AndroidAnalysis\\ReviewData\\Strangeword\\" + word + "_Review.txt");
		while (results.next()) {
			String text = results.getString("text");
			if (text.indexOf('\t') < 0) // Not from Android Market
				text = results.getString("title") + "." + text;
			count++;
			text = text.toLowerCase();
			String[] words = text.split("[^a-z']+");
			boolean contain = false;
			for (String temp : words) {
				if (temp.equals(word)) {
					contain = true;
					break;
				}
			}
			
			if (contain) {
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
