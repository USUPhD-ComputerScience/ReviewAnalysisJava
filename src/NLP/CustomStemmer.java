package NLP;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class CustomStemmer {
	private static final String FILENAME = "irregularVerbForms.txt";
	private Map<String, String> irregularVerbMap;
	private static CustomStemmer instance = null;
	private static final Set<Integer> vowelSet = new HashSet<>(
			Arrays.asList(new Integer[] { (int) 'a', (int) 'e', (int) 'i',
					(int) 'o', (int) 'u', (int) 'y' }));
	private static final Set<String> doubleSet = new HashSet<>(
			Arrays.asList(new String[] { "bb", "dd", "ff", "gg", "mm", "nn",
					"pp", "rr", "tt" }));

	private static final Set<String> specialSet = new HashSet<>(
			Arrays.asList(new String[] { "at", "bl", "iz", "dl", "gl", "pl",
					"tl", "kl", "tl", "dg", "iv", "tr", "dg", "uc", "rc", "ev",
					"rg", "fl", "ib", "av", "ng", "um", "ul", "lv", "nc", "rv",
					"rs", "ur" }));

	public static CustomStemmer getInstance() {
		if (instance == null)
			instance = new CustomStemmer();
		return instance;
	}

	private CustomStemmer() {
		try {
			readIrregularVerb();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readIrregularVerb() throws FileNotFoundException {
		irregularVerbMap = new HashMap<>();
		Scanner br = null;
		br = new Scanner(new FileReader(FILENAME));
		while (br.hasNextLine()) {
			String[] words = br.nextLine().split(" ");
			for (int i = 1; i < words.length; i++) {
				irregularVerbMap.put(words[i].intern(), words[0].intern());
			}
		}
		br.close();
	}

	public String stemNNS(String pluralNoun) {

		if (!pluralNoun.endsWith("s"))
			return pluralNoun;
		if (pluralNoun.length() < 4)
			return pluralNoun;
		StringBuilder noun = new StringBuilder();
		char[] seq = pluralNoun.substring(0, pluralNoun.length() - 1)
				.toCharArray();
		char c1 = seq[seq.length - 1]; // e
		if (c1 != 'e') {
			noun.append(seq);
			return noun.toString();
		}
		char c2 = seq[seq.length - 2]; // before e
		char c3 = seq[seq.length - 3];
		if (specialSet.contains("" + c3 + c2)) {
			noun.append(pluralNoun.subSequence(0, pluralNoun.length() - 2));
			noun.append("e");
			return noun.toString();
		}
		noun.append(pluralNoun.subSequence(0, pluralNoun.length() - 3));

		switch (c2) {
		case 'i':
			if (!isVowel(c3)) {
				noun.append('y');
			} else {
				noun.append(c3);
				noun.append(c2);
			}
			return noun.toString();
			/*
			if (!vowelSet.contains(pluralNoun
					.charAt(length - 4))) {
				noun.append(pluralNoun.subSequence(0,
						length - 3));
				noun.append('y');
			} else
				noun.append(pluralNoun.subSequence(0,
						length - 1));
			break;*/
		case 's':
			if (c3 == 's')
				noun.append(c2);
			else {
				noun.append(c2);
				noun.append(c1);// e
			}
			return noun.toString();
			// if (pluralNoun.charAt(length - 4) == 's')
			// noun.append(pluralNoun.subSequence(0,
			// length - 2));
			// else
			// noun.append(pluralNoun.subSequence(0,
			// length - 1));
			// break;
		case 'x':
		case 'o':
		case 'z':

			noun.append(c2);
			return noun.toString();
			// noun.append(pluralNoun.subSequence(0,
			// length - 2));
			// break;
		case 'h':
			switch (c3) {
			case 'c':
			case 's':
				noun.append(c2);
				return noun.toString();
				// noun.append(pluralNoun.subSequence(0,
				// length - 2));
				// break;
			default:
				noun.append(c2);
				noun.append(c1);// e
				return noun.toString();
				// noun.append(pluralNoun.subSequence(0,
				// length - 1));
			}
			// break;
		default:
			noun.append(c2);
			noun.append(c1);// e
			return noun.toString();
			// noun.append(pluralNoun.subSequence(0,
			// length - 1));

		}

	}

	public String stemVBD(String verb) {
		CharacterTriGramTrainer trainer = CharacterTriGramTrainer.getInstance();
		if (verb.length() < 5)
			return verb;
		if (!verb.endsWith("ed"))
			return verb;

		StringBuilder v = new StringBuilder();
		char[] seq = verb.substring(0, verb.length() - 2).toCharArray();
		int vowelCount = countVowel(seq);
		char c1 = seq[seq.length - 1];
		char c2 = seq[seq.length - 2];
		char c3 = seq[seq.length - 3];
		if (vowelCount == 0)
			return verb;
		v.append(verb.substring(0, verb.length() - 3));
		if (seq.length < 4 && isVowel(seq[seq.length - 2])) {
			v.append(c1);
			v.append('e');
			return v.toString();
		}
		if (c1 == 'i') {
			v.append('y');
			return v.toString();
		}
		if (vowelCount == 1 && c2 == c1 && isRemovableDoubleConsonents(c1))
			return v.toString();
		v.append(c1);
		try {
			double p0 = trainer.getProbability(c3, c2, c1, 'e')
					* trainer.getProbability(c2, c1, 'e', ' ');
			double p1 = trainer.getProbability(c3, c2, c1, ' ');
			if (p0 > p1)
				v.append('e');
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.out.println("" + c3 + c2 + c1 + 'e');
			return verb;
		}

		return v.toString();
	}

	public String verbPastSimpleStem(final CharSequence pastSimpleVerb) {
		int length = pastSimpleVerb.length();
		StringBuilder verb = new StringBuilder();
		if (pastSimpleVerb.charAt(length - 1) == 'd') {
			if (pastSimpleVerb.charAt(length - 2) == 'e') {
				int vowelCount = 0;
				for (int i = 0; i < length - 2; i++) {
					int c = pastSimpleVerb.charAt(i);
					if (vowelSet.contains(c)) {
						vowelCount++;
					}
				}
				if (vowelCount > 0) {
					if ((pastSimpleVerb.subSequence(0, length - 2).length() < 4 && vowelCount == 1)
							|| (pastSimpleVerb.charAt(length - 3) == 's' && pastSimpleVerb
									.charAt(length - 4) != 's')) {
						verb.append(pastSimpleVerb.subSequence(0, length - 2));
						verb.append("e");
					} else {
						if (doubleSet.contains(pastSimpleVerb.subSequence(
								length - 4, length - 2)))
							verb.append(pastSimpleVerb.subSequence(0,
									length - 3));
						else {
							if (specialSet.contains(pastSimpleVerb.subSequence(
									length - 4, length - 2))) {
								verb.append(pastSimpleVerb.subSequence(0,
										length - 2));
								verb.append("e");
							} else {
								if (pastSimpleVerb.charAt(length - 3) == 'i') {
									verb.append(pastSimpleVerb.subSequence(0,
											length - 3));
									verb.append("y");
								} else
									verb.append(pastSimpleVerb.subSequence(0,
											length - 2));
							}
						}
					}
					return verb.toString();
				} else {
					return (String) pastSimpleVerb;
				}
			} else {
				return (String) pastSimpleVerb;
			}
		} else {
			return (String) pastSimpleVerb;
		}
	}

	public String stemVBG(String verb) {
		CharacterTriGramTrainer trainer = CharacterTriGramTrainer.getInstance();
		if (verb.length() < 6)
			return verb;
		if (!verb.endsWith("ing"))
			return verb;

		StringBuilder v = new StringBuilder();
		char[] seq = verb.substring(0, verb.length() - 3).toCharArray();
		int vowelCount = countVowel(seq);
		char c1 = seq[seq.length - 1];
		char c2 = seq[seq.length - 2];
		char c3 = seq[seq.length - 3];
		if (vowelCount == 0)
			return verb;
		v.append(verb.substring(0, verb.length() - 4));
		if (seq.length < 4 && isVowel(seq[seq.length - 2])) {
			v.append(c1);
			v.append('e');
			return v.toString();
		}
		if (vowelCount == 1 && c2 == c1 && isRemovableDoubleConsonents(c1))
			return v.toString();
		v.append(c1);
		try {
			double p0 = trainer.getProbability(c3, c2, c1, 'e')
					* trainer.getProbability(c2, c1, 'e', ' ');

			double p1 = trainer.getProbability(c3, c2, c1, ' ');
			if (p0 > p1)
				v.append('e');
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.out.println("" + c3 + c2 + c1 + 'e');
		}

		return v.toString();
	}

	private int countVowel(char[] seq) {
		int count = 0;
		for (int i = 0; i < seq.length; i++) {
			if (isVowel(seq[i]))
				count++;
		}
		return count;
	}

	private boolean isRemovableDoubleConsonents(char c) {
		return c == 't' || c == 'n' || c == 'r' || c == 'd' || c == 'm'
				|| c == 'f' || c == 'g' || c == 'p' || c == 'b';
	}

	private boolean isVowel(char c) {
		return c == 'e' || c == 'a' || c == 'o' || c == 'i' || c == 'u'
				|| c == 'y';
	}

	public String verbPresentParticipleStem(
			final CharSequence presentParticipleVerb) {
		int length = presentParticipleVerb.length();
		StringBuilder verb = new StringBuilder();

		if (presentParticipleVerb.charAt(length - 1) == 'g') {
			if (presentParticipleVerb.charAt(length - 2) == 'n') {
				if (presentParticipleVerb.charAt(length - 3) == 'i') {
					int vowelCount = 0;
					for (int i = 0; i < length - 3; i++) {
						int c = presentParticipleVerb.charAt(i);
						if (vowelSet.contains(c)) {
							vowelCount++;
						}
					}
					if (vowelCount > 0) {
						if ((presentParticipleVerb.subSequence(0, length - 3)
								.length() < 4 && vowelCount == 1 && vowelSet
									.contains(presentParticipleVerb
											.charAt(length - 5)))
								|| (presentParticipleVerb.charAt(length - 4) == 's' && presentParticipleVerb
										.charAt(length - 5) != 's')) {
							verb.append(presentParticipleVerb.subSequence(0,
									length - 3));
							verb.append("e");
						} else {
							if (doubleSet.contains(presentParticipleVerb
									.subSequence(length - 5, length - 3)))
								verb.append(presentParticipleVerb.subSequence(
										0, length - 4));
							else {
								if (specialSet.contains(presentParticipleVerb
										.subSequence(length - 5, length - 3))) {
									verb.append(presentParticipleVerb
											.subSequence(0, length - 3));
									verb.append("e");
								} else {
									verb.append(presentParticipleVerb
											.subSequence(0, length - 3));
								}
							}
						}
						return verb.toString();
					} else {
						return (String) presentParticipleVerb;
					}
				} else {
					return (String) presentParticipleVerb;
				}
			} else {
				return (String) presentParticipleVerb;
			}
		} else {
			return (String) presentParticipleVerb;
		}
	}

	public String verbPastParticipleStem(final CharSequence pastParticipleVerb) {
		return verbPastSimpleStem(pastParticipleVerb);
	}

	public String stemVBZ(String thirdPersonSingularVerb) {
		return stemNNS(thirdPersonSingularVerb);
	}

	public String[] stem(String[] pair) {
		// TODO Auto-generated method stub
		String result[] = { null, null };
		if (pair[1].equals("NNS") || pair[1].equals("NNPS")) {
			result[0] = stemNNS(pair[0]).intern();
			result[1] = "NN".intern();
		} else {
			if (pair[1].equals("VBD") || pair[1].equals("VBG")
					|| pair[1].equals("VBN") || pair[1].equals("VBZ")) {
				result[0] = irregularVerbMap.get(pair[0]);
				result[1] = "VB".intern();
			} else {
				result[0] = pair[0];
				if (pair[1].equals("VBP"))
					result[1] = "VB".intern();
				else
					result[1] = pair[1].intern();
				return result;
			}
			if (result[0] == null) {
				switch (pair[1]) {
				case "VBD":
					result[0] = stemVBD(pair[0]).intern();
					break;
				case "VBG":
					result[0] = stemVBG(pair[0]).intern();
					break;
				case "VBN":
					result[0] = stemVBN(pair[0]).intern();
					break;
				case "VBZ":
					result[0] = stemVBZ(pair[0]).intern();
					break;

				}
				result[1] = "VB".intern();
			}
		}
		result[0] = SymSpell.getInstance().correctThisWord(result[0],
				SymSpell.LANGUAGE,true);
		return result;
	}

	private String stemVBN(String verb) {
		// TODO Auto-generated method stub
		return stemVBD(verb);
	}

	private void irregularVerbRedundancyTest() {
		Map<String, StringBuilder> irregularSet = new HashMap<>();
		for (Entry<String, String> pair : irregularVerbMap.entrySet()) {
			boolean redundant = false;
			if (pair.getValue().equals(verbPastSimpleStem(pair.getKey()))) {
				redundant = true;
			}
			if (pair.getValue().equals(stemVBZ(pair.getKey()))) {
				redundant = true;
			}
			if (pair.getValue()
					.equals(verbPresentParticipleStem(pair.getKey()))) {
				redundant = true;
			}
			if (!redundant) {
				StringBuilder vlist = irregularSet.get(pair.getValue());
				if (vlist == null)
					vlist = new StringBuilder();
				vlist.append(' ');
				vlist.append(pair.getKey());
				irregularSet.put(pair.getValue(), vlist);
			}
		}

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(FILENAME));
			for (Entry<String, StringBuilder> pair : irregularSet.entrySet()) {
				pw.write(pair.getKey());
				pw.write(pair.getValue().toString());
				pw.write('\n');
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pw != null)
			pw.close();
	}

	private static void prepareTestData() {
		Map<String, String> inputMap = new HashMap<>();
		Scanner br = null;
		try {
			br = new Scanner(new FileReader("stemmingInput.txt"));
			while (br.hasNextLine()) {
				String[] words = br.nextLine().split(" ");
				for (int i = 1; i < words.length; i++) {
					inputMap.put(words[i].intern(), words[0].intern());
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (br != null)
			br.close();

		MaxentTagger PoSTagger = new MaxentTagger(
				"lib/english-left3words-distsim.tagger");
		Set<String> POSN = new HashSet<>(Arrays.asList(new String[] { "NNS",
				"NN", "NNPS", "NNP" }));
		Set<String> POSV = new HashSet<>(Arrays.asList(new String[] { "VB",
				"VBG", "VBZ", "VBN", "VBP" }));
		int countNNS = 0;
		int countNN = 0;
		int countNNPS = 0;
		int countNNP = 0;
		int countVB = 0;
		int countVBG = 0;
		int countVBZ = 0;
		int countVBN = 0;
		int countVBP = 0;
		Map<String, String> outputMap = new HashMap<>();
		for (Entry<String, String> pair : inputMap.entrySet()) {
			String tagged = PoSTagger.tagString(pair.getKey());
			// Output the result
			// System.out.println(tagged);
			String noSpace[] = tagged.split(" ");
			String[] results = noSpace[0].split("_");
			if (results.length == 2) {
				switch (results[1]) {
				case "NNS":
					if (countNNS < 500 && results[0].length() > 1) {
						countNNS++;
						outputMap.put(pair.getValue(), pair.getKey() + " "
								+ results[1]);
					}
					break;
				case "NN":
					if (countNN < 500 && results[0].length() > 1) {
						countNN++;
						outputMap.put(pair.getKey(), pair.getKey() + " "
								+ results[1]);
					}
					break;
				case "NNPS":
					if (countNNPS <= 500 && results[0].length() > 1) {
						countNNPS++;
						outputMap.put(pair.getValue(), pair.getKey() + " "
								+ results[1]);
					}
					break;
				case "NNP":
					if (countNNP < 500 && results[0].length() > 1) {
						countNNP++;
						outputMap.put(pair.getKey(), pair.getKey() + " "
								+ results[1]);
					}
					break;
				case "VB":
					if (countVB < 500 && results[0].length() > 1) {
						countVB++;
						outputMap.put(pair.getKey(), pair.getKey() + " "
								+ results[1]);
					}
					break;
				case "VBG":
					if (countVBG < 500 && results[0].length() > 1) {
						countVBG++;
						outputMap.put(pair.getValue(), pair.getKey() + " "
								+ results[1]);
					}
					break;
				case "VBN":
					if (countVBN < 500 && results[0].length() > 1) {
						countVBN++;
						outputMap.put(pair.getValue(), pair.getKey() + " "
								+ results[1]);
					}
					break;
				case "VBZ":
					if (countVBZ < 500 && results[0].length() > 1) {
						countVBZ++;
						outputMap.put(pair.getValue(), pair.getKey() + " "
								+ results[1]);
					}
					break;
				case "VBP":
					if (countVBP < 500 && results[0].length() > 1) {
						countVBP++;
						outputMap.put(pair.getValue(), pair.getKey() + " "
								+ results[1]);
					}
					break;
				}
			}
		}
		System.out.println("Number of NN = " + countNN);
		System.out.println("Number of NNS = " + countNNS);
		System.out.println("Number of NNP = " + countNNP);
		System.out.println("Number of NNPS = " + countNNPS);
		System.out.println("Number of VB = " + countVB);
		System.out.println("Number of VBP = " + countVBP);
		System.out.println("Number of VBN = " + countVBN);
		System.out.println("Number of VBG = " + countVBG);
		System.out.println("Number of VBZ = " + countVBZ);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter("test.txt"));
			for (Entry<String, String> pair : outputMap.entrySet()) {
				pw.write(pair.getKey());
				pw.write(" ");
				pw.write(pair.getValue());
				pw.write('\n');
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pw != null)
			pw.close();
	}

	public static void main(String[] args) {
		// prepareTestData();
		// unitTest();
		// CustomStemmer.getInstance().irregularVerbRedundancyTest();
		CustomStemmerTest test = new CustomStemmerTest();
		test.testStem();
		// CharacterTriGramTrainer trainer = new CharacterTriGramTrainer();
		// System.out.println("Trainning...");
		// long startTime = System.nanoTime();
		// System.out.println("Number of word trained: "
		// + trainer.train("text8", "trainedData.txt"));
		// System.out.println("total time: "
		// + ((double) (System.nanoTime() - startTime) / 1000000 / 1000)
		// + " seconds");
		// trainer.getSpecialDoubles('e');
	}
}