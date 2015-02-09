package model;

import java.io.Serializable;

import Analyzer.Clusterable;
import NLP.NatureLanguageProcessor;
import util.Util;
import word2vec.WordVec;

// This is the word model for the entire analysis
public class Word extends Clusterable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1820812245518387992L;
	private String word;
	private int count;
	//private String POStag;
	private int hash;

	public Word(String w) {
		word = w.intern();
		count = 1;
		//POStag = POS;
		hash = word.hashCode();
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object arg0) {
		if (this == arg0)
			return true;
		if (!(arg0 instanceof Word))
			return false;
		Word obj = (Word) arg0;
		if (this.word.equals(obj.word))
			return true;
		return false;
	}



	public void increaseCount() {
		count++;
	}

	public int getCount() {
		return count;
	}

	public boolean isEqual(String w2, int POS) {
		if (word.equals(w2))
			return true;
		return false;
	}

	/**
	 * @return the string of this word
	 */
	public String toString() {
		return word;
	}

	@Override
	public float[] getVector() {
		// TODO Auto-generated method stub
		return WordVec.getInstance().getVectorForWord(word);
	}

	@Override
	public int getFrequency() {
		// TODO Auto-generated method stub
		return count;
	}

	@Override
	public void setChange(boolean isChanged) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isChanged() {
		// TODO Auto-generated method stub
		return false;
	}
}
