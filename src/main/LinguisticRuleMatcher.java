package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import NLP.NatureLanguageProcessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class LinguisticRuleMatcher {
	public static final ArrayList<String> reviews = new ArrayList<>();
	public static final String DIR = "E:\\AndroidAnalysis\\ReviewData\\data\\v21\\request\\";
	public static final ArrayList<ArrayList<PartOfRule>> linguisticRules = new ArrayList<>();

	public static void main(String[] args) throws Throwable {
		MaxentTagger PoSTagger = new MaxentTagger(
				"lib/english-left3words-distsim.tagger");
		loadData(new File(DIR + "reviewDataSet.txt"));
		readLinguisticRules(new File(DIR + "linguisticRules.txt"));
		PrintWriter pw = new PrintWriter(DIR + "requestSentences.csv");
		int count = 0;
		for (String review : reviews) {
			count++;
			String[] sentences = review.split("\\.");
			for (String sentence : sentences) {
				String taggedSentences = PoSTagger.tagString(sentence
						.toString());
				String[] words = taggedSentences.split(" ");
				ArrayList<String[]> sentenceData = new ArrayList<>();
				for (String word : words) {
					String[] w_pos = word.split("_");
					if (w_pos.length == 2 && !w_pos[0].equals("")
							&& !w_pos[0].equals(" "))
						sentenceData.add(w_pos);
				}
				ArrayList<ArrayList<PartOfRule>> matches = match(sentenceData);
				for (ArrayList<PartOfRule> match : matches) {
					boolean isMeaningful = false;
					for (PartOfRule part : match) {
						if (part.getType() == PartOfRule.NAMED_ENTITY
								|| part.getType() == PartOfRule.OPTIONAL_NE
								|| part.getType() == PartOfRule.GENERAL) {
							isMeaningful = part.isMeaningfulNE();
							if (isMeaningful)
								break;
						}
					}
					if (isMeaningful) {
						for (PartOfRule part : match) {
							part.toFile(pw);
						}
						pw.println();
					}
				}
			}
			if (count % 10000 == 0) {
				System.out.println(">> Done with " + count + " reviews");
			}
		}
		pw.close();
		System.out.println(">> Done! Total reviews was " + count);
	}

	private static ArrayList<ArrayList<PartOfRule>> match(
			ArrayList<String[]> sentenceData) {
		ArrayList<ArrayList<PartOfRule>> matches = new ArrayList<>();
		for (ArrayList<PartOfRule> rule : linguisticRules) {
			ArrayList<PartOfRule> generalizedRule = new ArrayList<>();
			ArrayList<PartOfRule> generalPart = new ArrayList<>();
			for (PartOfRule part : rule) {
				if (part.getType() != PartOfRule.NORMAL) {
					generalPart.add(part);
				} else {
					if (generalPart.size() != 0) {
						generalizedRule.add(new PartOfRule(generalPart,
								PartOfRule.GENERAL));
						generalPart = new ArrayList<>();
					}
					generalizedRule.add(part);
				}
			}
			if (generalPart.size() != 0)
				generalizedRule.add(new PartOfRule(generalPart,
						PartOfRule.GENERAL));
			ArrayList<PartOfRule> transformedSentence = new ArrayList<>();
			ArrayList<String> words = new ArrayList<>();
			boolean test = false;
			int wordIndex = 0;
			for (int j = 0; j < generalizedRule.size(); j++) {
				if (generalizedRule.get(j).getType() == PartOfRule.NORMAL) {
					int i = wordIndex;
					boolean foundMatch = false;
					while (i < sentenceData.size()) {
						int result = generalizedRule.get(j).matchNormal(
								sentenceData, i);
						if (result != -1) {
							if (j > 0) {
								// add words to the previous general rule
								List<String[]> sublist = new ArrayList<String[]>(
										sentenceData.subList(wordIndex, i));
								generalizedRule.get(j - 1).setGeneralWords(
										sublist);
								transformedSentence.add(generalizedRule
										.get(j - 1));
							}
							// add word to this normal rule
							generalizedRule.get(j).setGeneralWords(
									new ArrayList<String[]>(sentenceData
											.subList(i, result)));
							transformedSentence.add(generalizedRule.get(j));
							wordIndex = result;
							foundMatch = true;
							break;
						} else {
							i++;
						}
					}
					if (!foundMatch) {
						transformedSentence = null;
						break;
					}
				}
			}
			if (transformedSentence != null) {
				PartOfRule lastPart = generalizedRule.get(generalizedRule
						.size() - 1);
				if (lastPart.getType() == PartOfRule.GENERAL) {
					lastPart.setGeneralWords(new ArrayList<String[]>(
							sentenceData.subList(wordIndex, sentenceData.size())));
					transformedSentence.add(lastPart);
				}
				// worked so far
				for (PartOfRule part : transformedSentence) {
					if (part.getType() == PartOfRule.GENERAL)
						if (!part.matchGeneral())
							transformedSentence = null;
				}
			}
			if (transformedSentence != null) {
				matches.add(transformedSentence);
			}
		}
		return matches;
	}

	private static void readLinguisticRules(File file) throws Throwable {
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNextLine()) {
			String rule = br.nextLine();
			linguisticRules.add(exploreRule(rule.toCharArray()));
		}
		br.close();
	}

	private static ArrayList<PartOfRule> exploreRule(char[] rule) {
		ArrayList<PartOfRule> processedRule = new ArrayList<>();
		int pointer = 0;
		while (pointer < rule.length) {
			switch (rule[pointer]) {
			case '(': // something optional
				StringBuilder insideBracket = new StringBuilder();
				pointer++;
				while (rule[pointer] != ')') {
					if (rule[pointer] == '<') { // OPTIONAL_POS or OPTIONAL_NE
						StringBuilder insideChevron = new StringBuilder();
						pointer++;
						while (rule[pointer] != '>') {
							insideChevron.append(rule[pointer]);
							pointer++;
						}
						if (Character.isUpperCase(insideChevron.charAt(0))) {
							// OPTIONAL_POS
							processedRule.add(new PartOfRule(insideChevron
									.toString(), PartOfRule.OPTIONAL_POS));
						} else {
							// OPTIONAL_NE
							processedRule.add(new PartOfRule(insideChevron
									.toString(), PartOfRule.OPTIONAL_NE));
						}
						pointer++;// == ')'
						break;
					}
					// for normal text
					insideBracket.append(rule[pointer]);
					pointer++;
				}
				if (insideBracket.length() != 0) {
					processedRule.add(new PartOfRule(insideBracket.toString(),
							PartOfRule.OPTIONAL_NORMAL));
				}
				pointer++;
				break;
			case '<': // POS or NAMED_ENTITY
				StringBuilder insideChevron = new StringBuilder();
				pointer++;
				while (rule[pointer] != '>') {
					insideChevron.append(rule[pointer]);
					pointer++;
				}
				if (Character.isUpperCase(insideChevron.charAt(0))) {
					// POS
					processedRule.add(new PartOfRule(insideChevron.toString(),
							PartOfRule.POS));
				} else {
					// NAMED_ENTITY
					processedRule.add(new PartOfRule(insideChevron.toString(),
							PartOfRule.NAMED_ENTITY));
				}
				pointer++;
				break;
			default: // NORMAL
				StringBuilder normalPart = new StringBuilder();
				while (pointer < rule.length && rule[pointer] != '<'
						&& rule[pointer] != '(') {
					normalPart.append(rule[pointer]);
					pointer++;
				}
				processedRule.add(new PartOfRule(normalPart.toString(),
						PartOfRule.NORMAL));
				break;
			}
		}
		return processedRule;
	}

	private static void loadData(File file) throws Throwable {
		// TODO Auto-generated method stub
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNextLine()) {
			reviews.add(br.nextLine().intern());
		}
		br.close();
	}

	private static class PartOfRule {
		public static final int NAMED_ENTITY = 1; // <lower case>
		public static final int POS = 2; // <UPPERCASE>
		public static final int OPTIONAL_NE = 3; // (<lower case>)
		public static final int OPTIONAL_POS = 4; // (<UPPERCASE>)
		public static final int OPTIONAL_NORMAL = 5; // (lower case)
		public static final int NORMAL = 6; // lower case
		public static final int GENERAL = 7; // a list of some
												// consecutive parts without
												// normal part
		private int type;
		private String value;
		private ArrayList<PartOfRule> partList;
		private ArrayList<String> words;
		private List<String[]> generalWords;

		public static final Set<String> LEGIT_NE_POS = new HashSet<>(
				Arrays.asList(new String[] { "VBG", "NN", "NNP", "NNPS", "NNS" }));
		public static final Set<String> INTERESTING_NE_POS = new HashSet<>(
				Arrays.asList(new String[] { "VB", "VBD", "VBG", "VBN", "VBP",
						"VBZ", "NN", "NNP", "NNPS", "NNS" }));

		public boolean isMeaningfulNE() {
			if (type == NAMED_ENTITY || type == OPTIONAL_NE) {
				Set<String> stopwords = NatureLanguageProcessor.getInstance()
						.getStopWordSet1();
				boolean meaningful = false;
				for (String[] word : generalWords) {
					if (LEGIT_NE_POS.contains(word[1])) {
						meaningful = true;
						break;
					}
				}
				if (!meaningful)
					return false;
				int countStopWord = 0;
				for (String[] word : generalWords) {
					if (stopwords.contains(word[0])) {
						countStopWord++;
					}
				}
				if (countStopWord == generalWords.size())
					return false;
				return true;
			}
			if (type == GENERAL) {

				for (PartOfRule part : partList) {
					if (part.isMeaningfulNE())
						return true;
				}
				return false;
			} else
				return false;
		}

		public PartOfRule(String val, int typ) {
			value = val.intern();
			type = typ;
			if (type == NORMAL || type == OPTIONAL_NORMAL) {
				words = new ArrayList<>();
				String[] array = value.split(" ");
				for (String w : array) {
					if (!w.equals(""))
						words.add(w);
				}
			}
		}

		public void toFile(PrintWriter pw) {
			// TODO Auto-generated method stub
			pw.print(toString() + " ");
		}

		public String toString() {

			Set<String> stopwords = NatureLanguageProcessor.getInstance()
					.getStopWordSet1();
			if (generalWords == null) // optional not found
				return "";
			StringBuilder str = new StringBuilder();

			String prefix = "";
			String openBracket = "(", closeBracket = ")";
			switch (type) {
			case GENERAL:

				for (PartOfRule part : partList) {
					str.append(prefix);
					str.append(part.toString());
					prefix = " ";
				}
				break;
			case POS:
			case OPTIONAL_POS:
				return generalWords.get(0)[0] + "_" + generalWords.get(0)[1];
			case NORMAL:
				openBracket = "";
				closeBracket = "";
			case OPTIONAL_NORMAL:
				str.append(openBracket);
				for (String[] word : generalWords) {
					str.append(prefix);
					str.append(word[0]);
					prefix = " ";
				}
				str.append(closeBracket);
				break;
			case NAMED_ENTITY:
				openBracket = "";
				closeBracket = "";
			case OPTIONAL_NE:
				str.append('<');
				str.append("__" + value + "__ ");
				str.append(openBracket);
				for (String[] word : generalWords) {
					str.append(prefix);
					if (INTERESTING_NE_POS.contains(word[1])
							&& !stopwords.contains(word[0]))
						str.append(word[0].toUpperCase());
					else
						str.append(word[0]);
					prefix = " ";
				}
				str.append(closeBracket);
				str.append('>');

				return str.toString();
			}
			return str.toString();
		}

		public void setGeneralWords(List<String[]> wordList) {
			generalWords = wordList;
		}

		public String getValue() {
			return value;
		}

		public boolean matchGeneral() {
			// a named entity (maybe optional) or a
			// lone part
			PartOfRule firstPart = partList.get(0);

			if (firstPart.getType() == NAMED_ENTITY) {
				if (generalWords.size() == 0)
					return false;
				firstPart.setGeneralWords(generalWords);
				return true;
			}
			if (firstPart.getType() == OPTIONAL_NE) {
				if (generalWords.size() == 0)
					return true;
				firstPart.setGeneralWords(generalWords);
				return true;
			}
			// POS or/and OPTIONAL_POS or/and OPTIONAL_NORMAL
			int wordIndex = 0;
			for (PartOfRule part : partList) {
				switch (part.getType()) {
				case OPTIONAL_NORMAL:
					if (wordIndex >= generalWords.size())
						return true;
					int result = part.matchNormal(generalWords, wordIndex);
					if (result != -1) {
						part.setGeneralWords(new ArrayList<String[]>(
								generalWords.subList(wordIndex, result)));
						wordIndex = result;
					}
					break;
				case POS:
					if (wordIndex >= generalWords.size())
						return false;
					if (!part.getValue().equals(generalWords.get(wordIndex)[1]))
						return false;
				case OPTIONAL_POS:
					if (wordIndex >= generalWords.size())
						return true;
					if (part.getValue().equals(generalWords.get(wordIndex)[1])) {
						part.setGeneralWords(new ArrayList<String[]>(
								generalWords.subList(wordIndex, wordIndex + 1)));
						wordIndex++;
					}
					break;
				case NAMED_ENTITY:
					if (wordIndex >= generalWords.size())
						return false;
					part.setGeneralWords(new ArrayList<String[]>(generalWords
							.subList(wordIndex, generalWords.size())));
					wordIndex++;
					break;
				default:
					return false;
				}
			}
			return true;
		}

		public int matchNormal(List<String[]> sentenceData, int index) {
			if (index + words.size() > sentenceData.size())
				return -1;
			int i;
			for (i = index; i < index + words.size(); i++) {
				if (!sentenceData.get(i)[0].equals(words.get(i - index)))
					return -1;
			}
			return i;
		}

		public PartOfRule(ArrayList<PartOfRule> list, int typ) {
			partList = list;
			type = typ;
		}

		public int getType() {
			return type;
		}
	}
}
