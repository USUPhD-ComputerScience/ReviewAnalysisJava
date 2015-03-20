package model;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import util.Util;
import NLP.NatureLanguageProcessor;

public class Review implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -720406586403564687L;
	// private List<Sentence> sentenceList;
	// private List<Integer> wordIDList;
	private List<List<Integer>> sentenceList;
	private HashMap<WordPair, Integer> pairDF;
	private int rating;

	public int getRating() {
		return rating;
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
	private boolean extractSentences(String fulltext) {
		Vocabulary voc = Vocabulary.getInstance();
		NatureLanguageProcessor nlp = NatureLanguageProcessor.getInstance();
		sentenceList = new ArrayList<>();
		String[] rawSentences = nlp.extractSentence(fulltext);
		for (String sentence : rawSentences) {
			List<Integer> wordIDList = new ArrayList<>();
			List<String> wordList = nlp.extractWordsFromText(sentence);
			if (wordList == null)
				return false;
			List<String[]> stemmedWordsWithPOS = nlp.stem(nlp
					.findPosTagAndRemoveStopWords(wordList));

			if (stemmedWordsWithPOS != null) {
				for (String[] pair : stemmedWordsWithPOS) {
					if (pair.length < 2)
						continue;
					wordIDList.add(voc.addWord(pair[0], pair[1]));
				}
			}
			if (!wordIDList.isEmpty())
				sentenceList.add(wordIDList);
		}
		if (sentenceList.isEmpty())
			return false;
		return true;
	}

	public void addNewPair(WordPair pair) {
		Integer df = pairDF.get(pair);
		if (df != null)
			pairDF.put(pair, df + 1);
		else
			pairDF.put(pair, 1);
	}

	public HashMap<WordPair, Integer> getPairMap() {
		return pairDF;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public String getDocumentVersion() {
		return documentVersion;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public String getReviewId() {
		return reviewId;
	}

	private String deviceName;
	private String documentVersion;
	private long creationTime;
	private String reviewId; // commentID and VersionID
	private Application application;

	public Review(String fullText, int nestedRating, String nestedDeviceName,
			String nestedDocumentVersion, long nestedCreationTime,
			String nestedReviewId, Application app) {
		// TODO Auto-generated constructor stub
		rating = nestedRating;
		deviceName = nestedDeviceName;
		documentVersion = nestedDocumentVersion;
		creationTime = nestedCreationTime;
		reviewId = nestedReviewId.intern();
		// extractSentence(NatureLanguageProcessor.getInstance().extractSentence(
		// fullText));
		extractSentences(fullText);
		application = app;
		pairDF = new HashMap<>();
	}

	@Override
	public int hashCode() {
		return Objects.hash(reviewId, application);
	}

	@Override
	public boolean equals(Object arg0) {
		if (this == arg0)
			return true;
		if (!(arg0 instanceof Review))
			return false;
		Review obj = (Review) arg0;
		if (this.reviewId.equals(obj.reviewId)
				&& this.application.equals(obj.application))
			return true;
		return false;
	}

	/**
	 * Extract the sentences of this review and store them
	 * 
	 * @param sentences
	 *            - the String contains the sentences that have already been
	 *            standardized.
	 * 
	 */
	// private void extractSentence(String[] sentences) {
	// sentenceList = new ArrayList<>();
	// for (String fullSentence : sentences) {
	// Sentence s = new Sentence(fullSentence);
	// if (!s.getWordIDList().isEmpty())
	// sentenceList.add(s);
	// }
	// }

	/**
	 * 
	 * @return the list of Sentences
	 * 
	 */
	// public List<Sentence> getSentenceList() {
	// return sentenceList;
	// }

	public static class ReviewBuilder {
		private String nestedText;
		private int nestedRating;
		private String nestedDeviceName;
		private String nestedDocumentVersion;
		private long nestedCreationTime;
		private String nestedReviewId;
		private Application nestedApplication;

		public ReviewBuilder() {
			nestedText = null;
			nestedRating = 0;
			nestedDeviceName = null;
			nestedDocumentVersion = null;
			nestedCreationTime = 0;
			nestedReviewId = null;
		}

		public ReviewBuilder application(Application app) {
			this.nestedApplication = app;
			return this;
		}

		public ReviewBuilder text(String text) {
			this.nestedText = text.intern();
			return this;
		}

		public ReviewBuilder rating(int rating) {
			this.nestedRating = rating;
			return this;
		}

		public ReviewBuilder deviceName(String deviceName) {
			this.nestedDeviceName = deviceName;
			return this;
		}

		public ReviewBuilder documentVersion(String documentVersion) {
			this.nestedDocumentVersion = documentVersion;
			return this;
		}

		public ReviewBuilder creationTime(long creationTime) {
			this.nestedCreationTime = creationTime;
			return this;
		}

		public ReviewBuilder reviewId(String reviewID) {
			this.nestedReviewId = reviewID.intern();
			return this;
		}

		public Review createReview() {
			return new Review(nestedText, nestedRating, nestedDeviceName,
					nestedDocumentVersion, nestedCreationTime, nestedReviewId,
					nestedApplication);
		}
	}

	public void writeSentenceToFile(PrintWriter fileWriter, long lastUpdate) {
		// TODO Auto-generated method stub
		if (creationTime > lastUpdate)
			//fileWriter.println(toString());
			fileWriter.println(toProperString());
	}

	/**
	 * @return the full review with each word separated by a space
	 */
	public String toString() {
		Vocabulary voc = Vocabulary.getInstance();
		StringBuilder strBld = new StringBuilder();
		String prefix = "";
		for (List<Integer> sentence : sentenceList) {
			for (Integer wordID : sentence) {
				Word w = voc.getWord(wordID);
				if (w != null) {
					strBld.append(prefix);
					strBld.append(w.toString());
					prefix = " ";
				}
			}
			strBld.append(" ");
		}
		return strBld.toString();
	}
	/**
	 * @return the full review with each word separated by a space
	 */
	public String toProperString() {
		Vocabulary voc = Vocabulary.getInstance();
		StringBuilder strBld = new StringBuilder();
		String prefix = "";
		for (List<Integer> sentence : sentenceList) {
			for (Integer wordID : sentence) {
				Word w = voc.getWord(wordID);
				if (w != null) {
					strBld.append(prefix);
					strBld.append(w.toString());
					prefix = " ";
				}
			}
			strBld.append(". ");
		}
		return strBld.toString();
	}
	public List<List<Integer>> getSentenceList() {
		// TODO Auto-generated method stub
		return sentenceList;
	}
}
