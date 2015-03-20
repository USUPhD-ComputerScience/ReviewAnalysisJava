package model;

import java.io.Serializable;

import NLP.NatureLanguageProcessor;

public class WordPair implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4560582940457019822L;
	private long w1w2ID;
	private int[] template;
	private boolean googleMetric = false;
	private boolean loglikelihood = false;
	private boolean ttest = false;
	public static final int GOOGLE_METRIC = 1;
	public static final int LOG_LIKELIHOOD =2;
	public static final int TTEST = 3;
	public void setTest(boolean choosen,int by) {
		if (by == GOOGLE_METRIC)
			googleMetric = choosen;
		if (by == LOG_LIKELIHOOD)
			loglikelihood = choosen;
		if (by == TTEST)
			ttest = choosen;
		
	}

	public boolean isChoosenBy(int by) {
		if (by == GOOGLE_METRIC)
			return googleMetric;
		if (by == LOG_LIKELIHOOD)
			return loglikelihood;
		if (by == TTEST)
			return ttest;
		return false;
	}

	public WordPair(int w1, int w2, String[] temp) {
		// TODO Auto-generated constructor stub
		w1w2ID = (((long) w1) << 32) | (w2 & 0xffffffffL);
		Vocabulary voc = Vocabulary.getInstance();
		template = new int[temp.length];
		template[0] = w1;
		for (int i = 1; i < temp.length; i++) {
			if (NatureLanguageProcessor.POSSET.contains(temp[i]))
				template[i] = w2; // only 1 time
			else
				template[i] = voc.getWordID(temp[i]);
		}
	}

	public String toPOS() {

		int w1 = (int) (w1w2ID >> 32);
		int w2 = (int) w1w2ID;
		Vocabulary voc = Vocabulary.getInstance();
		StringBuilder strBld = new StringBuilder();
		String prefix = "";
		for (int id : template) {
			strBld.append(prefix);
			if (id == w1 || id == w2)
				strBld.append(voc.getWord(id).getPOS());
			else
				strBld.append(voc.getWord(id).toString());
			prefix = " ";
		}
		return strBld.toString();
	}

	public String toString() {
		Vocabulary voc = Vocabulary.getInstance();
		StringBuilder strBld = new StringBuilder();
		String prefix = "";
		for (int id : template) {
			strBld.append(prefix);
			strBld.append(voc.getWord(id).toString());
			prefix = " ";
		}
		return strBld.toString();
	}

	public long getPair() {
		return w1w2ID;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(w1w2ID).hashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (this == arg0)
			return true;
		if (!(arg0 instanceof WordPair))
			return false;
		WordPair obj = (WordPair) arg0;
		if (this.w1w2ID == obj.w1w2ID)
			return true;
		return false;
	}
}
