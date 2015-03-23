package Analyzer;

import word2vec.WordVec;

public class PairForCluster extends Clusterable {

	float[] vector = null;

	public static final WordVec word2vec = new WordVec();
	public PairForCluster(String str, Double freq) {
		pair = str.intern();
		frequency = freq.intValue();
		float[] tempVector = word2vec.getVectorForPhrase(pair);
		if(tempVector != null){
			vector = new float[WordVec.VECTOR_SIZE];
			for (int i = 0; i < WordVec.VECTOR_SIZE; i++) {
				vector[i] = tempVector[i];
			}
		}
			
	}
	public String getString(){
		return pair;
	}
	int frequency;
	String pair;
	boolean change = false;

	@Override
	public float[] getVector() {
		// TODO Auto-generated method stub
		return vector;
	}

	@Override
	public int getFrequency() {
		// TODO Auto-generated method stub
		return frequency;
	}

	@Override
	public void setChange(boolean isChanged) {
		// TODO Auto-generated method stub
		change = isChanged;
	}

	@Override
	public boolean isChanged() {
		// TODO Auto-generated method stub
		return change;
	}

}
