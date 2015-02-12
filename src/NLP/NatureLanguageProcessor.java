package NLP;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Word;

import org.tartarus.snowball.SnowballStemmer;

import util.Util;
import au.com.bytecode.opencsv.CSVReader;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class NatureLanguageProcessor {
	public static final String[] POSLIST = { "''", "(", ")", ",", "--", ".",
			":", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP",
			"WP$", "WRB", "$", "``", "NNPS", "NNS", "PDT", "POS", "PRP",
			"PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "CC", "CD", "DT",
			"EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP" };
	public static final Set<String> POSSET = new HashSet<>(
			Arrays.asList(POSLIST));
	private Set<String> stopWordSet;
	private static NatureLanguageProcessor instance = null;
	MaxentTagger PoSTagger;

	public static synchronized NatureLanguageProcessor getInstance() {
		if (instance == null)
			instance = new NatureLanguageProcessor();
		return instance;
	}

	private NatureLanguageProcessor() {
		readStopWordsFromFile();
		PoSTagger = new MaxentTagger("lib/english-left3words-distsim.tagger");
		try {
			Porter2StemmerInit();
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readStopWordsFromFile() {
		stopWordSet = new HashSet<>();
		System.err.println(">>Read StopWords from file - english.stop");
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader("lib/english.stop"));
			String[] row = null;
			while ((row = reader.readNext()) != null) {
				stopWordSet.add(row[0]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	/**
	 * Standardize the text and then split it into sentences, separated by DOT
	 * 
	 * @param text
	 *            - a text
	 * @return a String array of all the sentences.
	 */
	public String[] extractSentence(String text) {
		text = Util.standardizeText(text);
		return text.split("\\.");
	}

	/**
	 * Return the index of the corresponding PoS tag in the list provided by
	 * this Class. This provides a way to reduce the memory for String objects.
	 * Instead of storing the String of PoS tag, we can store its index.
	 * 
	 * @param PoS
	 *            - a PoS tag
	 * @return the index of that PoS tag or -1 if it is not in the list
	 */
	public boolean checkValidityOfPOS(String PoS) {
		return POSSET.contains(PoS);
	}

	/**
	 * Only for atomic sentence, it won't work as expected if the there are
	 * still some non-character words (not-standardized). This function break a
	 * the sentence into words with the following steps:
	 * 
	 * <pre>
	 * - Step 1: Lower case
	 * - Step 2: PoS tagging
	 * - Step 3: Remove StopWord
	 * - Step 4: Use Snowball Stemming (Porter2/English)
	 * </pre>
	 * 
	 * @param fullSentence
	 *            - the String that has already been standardized.
	 * @return a list contains a String array of 2 elements: 0-word, 1-PoS Or
	 *         NULL if less than 2 words were extracted
	 */
	public List<String[]> breakSentenceIntoWords(String fullSentence) {
		List<String[]> wordList = findPosTagAndRemoveStopWords(fullSentence);
		if (wordList == null)
			return null;
		if (wordList.size() < 1)
			return null;
		return stem(wordList);
	}

	/**
	 * This function will stem the words in the input List using Porter2/English
	 * stemmer and replace the String value of that word with the stemmed
	 * version.
	 * 
	 * @param wordList
	 *            - a List contains a String array of 2 elements: 0-word, 1-PoS
	 */
	public List<String[]> stem(List<String[]> wordList) {
		List<String[]> results = new ArrayList<>();
		for (String[] pair : wordList) {
			if (pair.length < 2)
				continue;
			// System.out.print(count++ + ": " + pair[0]);
			pair = CustomStemmer.getInstance().stem(pair);

			results.add(pair);
			// System.out.println("-" + pair[0] + "<->" + pair[1]);
		}
		return results;
	}

	private void Porter2StemmerInit() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class stemClass = Class
				.forName("org.tartarus.snowball.ext.englishStemmer");
	}

	/**
	 * PoS tagging and remove stop word of a string input.
	 * 
	 * <pre>
	 * - Step 1: Lower case
	 * - Step 2: PoS tagging
	 * - Step 3: Remove StopWord
	 * </pre>
	 * 
	 * @param input
	 *            - the String that need to be processed
	 * @return a list contains a String array of 2 elements: 0-word, 1-PoS
	 */
	public List<String[]> findPosTagAndRemoveStopWords(String input) {
		SymSpell spellCorrector = SymSpell.getInstance();
		input = input.toLowerCase();
		StringBuilder textForTag = new StringBuilder();
		String[] words = input.split(" ");
		String prefix = "";
		boolean ignore = false;
		HashSet<String> blackList = spellCorrector.getBlackList();
		for (int i = 0; i < words.length; i++) {

			if (blackList.contains(words[i]))
				ignore = true;
		}
		if (ignore)
			return null;
		for (int i = 0; i < words.length; i++) {
			textForTag.append(prefix);
			words[i] = spellCorrector.correctThisWord(words[i],
					SymSpell.LANGUAGE, false);
			textForTag.append(words[i]);
			prefix = " ";
		}
		// The tagged string
		String tagged = PoSTagger.tagString(textForTag.toString());
		// Output the result
		// System.out.println(tagged);

		words = tagged.split(" ");
		// System.out.println("length = " + words.length);

		List<String[]> results = new ArrayList<>();
		for (int i = 0; i < words.length; i++) {
			String[] w = words[i].split("_");
			if (!stopWordSet.contains(w[0]))
				results.add(w);
		}
		return results;
	}
}
