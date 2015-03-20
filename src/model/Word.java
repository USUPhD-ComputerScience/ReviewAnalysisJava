package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

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
	private HashMap<String, Integer> POSList;
	private int hash;
	private String POS;
	private int POSMaxCount = 0;

	public HashMap<String,Integer> getPOSList(){
		return POSList;
	}
	public Word(String w, String pos) {
		word = w.intern();
		count = 1;
		POSList = new HashMap<>();
		POSList.put(pos.intern(), 1);
		POSMaxCount = 1;
		POS = pos.intern();
		hash = word.hashCode();
	}

	public String getPOS() {
		return POS;
	}

	public void addPOS(String POSTag) {
		Integer pos = POSList.get(POSTag);
		if (pos != null) {
			POSList.put(POSTag.intern(), ++pos);
			if (pos > POSMaxCount) {
				POSMaxCount = pos;
				POS = POSTag.intern();
			}
		} else {
			POSList.put(POSTag.intern(), 1);
		}
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

	public boolean isEqual(String w2) {
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
