package Managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import NLP.NatureLanguageProcessor;
import model.Vocabulary;
import model.Word;
import model.WordPair;

public class Template {
	// POS1.....w2w3w4w5POS2
	String rules[];

	public Template(String[] temp) {
		rules = temp;
	}

	public List<WordPair> match(List<Integer> wordIDList, int index) {
		List<WordPair> results = new ArrayList<>();
		if (index > wordIDList.size() - 1)
			return results;
		Vocabulary voc = Vocabulary.getInstance();
		int w1 = wordIDList.get(index);
		if (!voc.getWord(w1).getPOS().equals(rules[0]))
			return results;
		NatureLanguageProcessor nlp = NatureLanguageProcessor.getInstance();
		if (nlp.getStopWordSet1().contains(voc.getWord(w1).toString()))
			return results;
		int maxWindow = WordPairsManager.WINDOW_SIZE - rules.length;
		for (int j = index + 1; j < index + maxWindow; j++) {
			if (j + (rules.length - 2) > wordIDList.size() - 1)
				break;
			// test rule
			int w2 = -1;
			for (int ruleIndex = 1; ruleIndex < rules.length; ruleIndex++) {
				int wID = wordIDList.get(j + ruleIndex - 1);
				Word WoI = voc.getWord(wID);
				if (WoI.toString().equals(rules[ruleIndex])) {
					continue;
				} else {
					if (WoI.getPOS().equals(rules[ruleIndex])) {
						if (w2 != -1) {
							// the template is wrong
							w2 = -1;
							break;
						}
						if (nlp.getStopWordSet1().contains(WoI.toString())) {
							w2 = -1;
							break;
						}
						w2 = wID;
					} else {
						w2 = -1;
						break;
					}
				}
			}
			if (w2 != -1)
				results.add(new WordPair(w1, w2, rules));
		}
		return results;
	}
}
