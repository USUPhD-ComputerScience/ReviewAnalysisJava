package model;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import util.Util;
import NLP.NatureLanguageProcessor;

public class Sentence implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8361528249850048793L;
	private List<Integer> wordIDList;

	/**
	 * @param fullSentence
	 *            - a standardized String (no non-word character)
	 */
	public Sentence(String fullSentence) {
		extractWords(fullSentence);
	}

	/**
	 * Only for constructor. This function break a string into words in 4 steps:
	 * 
	 * <pre>
	 * - Step 1: Lower case
	 * - Step 2: PoS tagging
	 * - Step 3: Remove StopWord
	 * - Step 4: Use Snowball Stemming (Porter 2)
	 * </pre>
	 * 
	 * @param fullSentence
	 *            - The sentence to extract words from
	 * @return TRUE if it successfully extracted some words, FALSE otherwise
	 */
	private boolean extractWords(String fullSentence) {
		Vocabulary voc = Vocabulary.getInstance();
		NatureLanguageProcessor nlp = NatureLanguageProcessor.getInstance();
		wordIDList = new ArrayList<>();
		List<String[]> wordList = nlp.breakSentenceIntoWords(fullSentence);
		if (wordList != null) {
			for (String[] pair : wordList) {
				if (pair.length < 2)
					continue;
				if (Util.isNumeric(pair[0]))
					continue;
				wordIDList.add(voc.addWord(new Word(pair[0])));
			}
		}
		if (wordIDList.isEmpty())
			return false;
		return true;
	}

	public List<Integer> getWordIDList() {
		return wordIDList;
	}

	/**
	 * @return the full sentence with each word separated by a space
	 */
	public String toString() {
		Vocabulary voc = Vocabulary.getInstance();
		StringBuilder strBld = new StringBuilder();
		String prefix = "";
		for (Integer wordID : wordIDList) {
			Word w = voc.getWord(wordID);
			if (w != null) {
				strBld.append(prefix);
				strBld.append(w.toString());
				prefix = " ";
			}
		}
		return strBld.toString();
	}
}
