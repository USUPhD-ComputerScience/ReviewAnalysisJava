package model;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import NLP.NatureLanguageProcessor;

public class Review implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -720406586403564687L;
	private List<Sentence> sentenceList;
	private Set<Long> pairSet;
	private int rating;

	public int getRating() {
		return rating;
	}

	public void addNewPair(long pair){
		pairSet.add(pair);
	}
	public Set<Long> getPairSet(){
		return pairSet;
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
		reviewId = nestedReviewId;
		extractSentence(NatureLanguageProcessor.getInstance().extractSentence(
				fullText));
		application = app;
		pairSet = new HashSet<>();
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
	private void extractSentence(String[] sentences) {
		sentenceList = new ArrayList<>();
		for (String fullSentence : sentences) {
			Sentence s = new Sentence(fullSentence);
			if (!s.getWordIDList().isEmpty())
				sentenceList.add(s);
		}
	}

	/**
	 * 
	 * @return the list of Sentences
	 * 
	 */
	public List<Sentence> getSentenceList() {
		return sentenceList;
	}

	public static class ReviewBuilder {
		private String nestedText;
		private int nestedRating;
		private String nestedTitle;
		private String nestedDeviceName;
		private String nestedDocumentVersion;
		private long nestedCreationTime;
		private String nestedReviewId;
		private Application nestedApplication;

		public ReviewBuilder() {
			nestedText = null;
			nestedRating = 0;
			nestedTitle = null;
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
			this.nestedText = text;
			return this;
		}

		public ReviewBuilder rating(int rating) {
			this.nestedRating = rating;
			return this;
		}

		public ReviewBuilder title(String title) {
			this.nestedTitle = title;
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
			this.nestedReviewId = reviewID;
			return this;
		}

		public Review createReview() {
			String fullText = "";
			if (nestedTitle != null)
				fullText = nestedTitle + ". " + nestedText;
			else
				fullText = nestedText;
			return new Review(fullText, nestedRating, nestedDeviceName,
					nestedDocumentVersion, nestedCreationTime, nestedReviewId,
					nestedApplication);
		}
	}

	public void writeSentenceToFile(PrintWriter fileWriter, long lastUpdate) {
		// TODO Auto-generated method stub
		if (creationTime > lastUpdate)
			for (Sentence sentence : sentenceList) {
				fileWriter.println(sentence.toString());
				// System.out.println(sentence.toString());
			}
	}
}
