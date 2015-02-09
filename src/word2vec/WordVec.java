package word2vec;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class WordVec {

	private final Map<String, float[]> wordVector;
	private static WordVec instance = null;
	private static Map<String, float[]> phraseVector = new HashMap<>();
	private static final int VECTOR_SIZE = 200;

	private WordVec() {
		wordVector = new HashMap<>();
		try {
			System.out.print("Loading Word Vectors");
			loadTextModel("ReviewVectors.txt");
			System.out.println("-Done!");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static WordVec getInstance() {
		if (instance == null)
			instance = new WordVec();
		return instance;
	}

	public float[] getVectorForPhrase(String phrase) {
		float[] vector = phraseVector.get(phrase);
		if (vector != null)
			return vector;
		vector = new float[VECTOR_SIZE];
		String[] words1 = phrase.split(" ");
		for (String word : words1) {
			float[] wordvec = wordVector.get(word);
			if (wordvec != null) {
				for (int i = 0; i < vector.length; i++) {
					vector[i] += wordvec[i];
				}
			}
		}
		phraseVector.put(phrase, vector);

		return vector;
	}



	public double cosineSimilarityForPhrases(String phrase1, String phrase2,
			boolean normalize) {

		double sim = 0, square1 = 0, square2 = 0;
		float[] vector1 = getVectorForPhrase(phrase1);
		float[] vector2 = getVectorForPhrase(phrase2);

		if (vector1 == null || vector2 == null)
			return 0;
		for (int i = 0; i < vector1.length; i++) {
			square1 += vector1[i] * vector1[i];
			square2 += vector2[i] * vector2[i];
			sim += vector1[i] * vector2[i];
		}
		if (sim == 0)
			return 0;
		if (!normalize)
			return sim / Math.sqrt(square1) / Math.sqrt(square2);
		else
			return (1 + sim / Math.sqrt(square1) / Math.sqrt(square2)) / 2;
	}

	public Map<String, float[]> getWordVector() {
		return wordVector;
	}

	public float[] getVectorForWord(String word){
		return wordVector.get(word);
	}
	public void loadTextModel(String filename) throws FileNotFoundException {
		Scanner br = null;
		try {
			br = new Scanner(new FileReader(filename));
			int vocabSize = br.nextInt();
			int layer1Size = br.nextInt();
			for (int a = 0; a < vocabSize; a++) {
				String word = br.next();
				float[] vector = new float[layer1Size];
				for (int b = 0; b < layer1Size; b++) {
					vector[b] = br.nextFloat();
				}
				wordVector.put(word, vector);
				// if ((a + 1) % (vocabSize / 100) == 0) {
				// System.out.println("progress " + (a + 1) * 100 / vocabSize
				// + "%");
				//
				// System.out.print(word);
				// for (int b = 0; b < layer1Size; b++) {
				// System.out.print(" " + vector[b]);
				// }
				// System.out.println();
				// }
			}

		} finally {
			if (br != null)
				br.close();

		}
	}

	public double cosineSimilarityForWords(String word1, String word2,
			boolean normalize) {
		double sim = 0, square1 = 0, square2 = 0;
		float[] vector1 = wordVector.get(word1);
		float[] vector2 = wordVector.get(word2);
		if (vector1 == null || vector2 == null)
			return 0;
		for (int i = 0; i < vector1.length; i++) {
			square1 += vector1[i] * vector1[i];
			square2 += vector2[i] * vector2[i];
			sim += vector1[i] * vector2[i];
		}
		if (!normalize)
			return sim / Math.sqrt(square1) / Math.sqrt(square2);
		else
			return (1 + sim / Math.sqrt(square1) / Math.sqrt(square2)) / 2;
	}

	// public static void main(String[] args) {
	// WordVec wordvec = new WordVec();
	// try {
	// wordvec.loadTextModel("ReviewVectors.txt");
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// Scanner scanner = new Scanner(System.in);
	// while(true){
	// System.out.println("Input your first word:");
	// String word1 = scanner.next();
	// System.out.println("Input your second word:");
	// String word2 = scanner.next();
	// System.out.println("Similarity = " + wordvec.cosineSimilarity(word1,
	// word2));
	// }
	// }

}
