package NLP;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.junit.Test;

public class CustomStemmerTest {

	@Test
	public void testStem() {
		Map<String[], String> inputMap = new HashMap<>();
		Scanner br = null;
		try {
			br = new Scanner(new FileReader("stemmingInput.txt"));
			while (br.hasNextLine()) {
				String[] words = br.nextLine().split(" ");
				if (words.length == 3) {
					if (words[2].intern().equals("NNS")
							|| words[2].intern().equals("NNPS")
							|| words[2].intern().equals("VBZ"))
						inputMap.put(
								new String[] { words[1].intern(),
										words[2].intern() }, words[0].intern());
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CustomStemmer stemmer = CustomStemmer.getInstance();
		CharacterTriGramTrainer trainer = CharacterTriGramTrainer.getInstance();
		if (br != null)
			br.close();
		int count = 0;
		int countE = 0;
		for (Entry<String[], String> entry : inputMap.entrySet()) {
			try {
				assertEquals("Stemming result for <" + entry.getKey()[0] + "_"
						+ entry.getKey()[1] + "> must be <" + entry.getValue()
						+ ">", entry.getValue(),
						stemmer.stem(entry.getKey())[0]);
				count++;
			} catch (AssertionError e) {

				countE++;
				char[] seq = entry.getKey()[0].substring(0,
						entry.getKey()[0].length() - 3).toCharArray();
				if (seq.length >= 5) {
					char c1 = seq[seq.length - 1];
					char c2 = seq[seq.length - 2];
					char c3 = seq[seq.length - 3];
					double c1c2e = trainer.getProbability(c3, c2, c1, 'e');
					double c2eSpace = trainer.getProbability(c2, c1, 'e', ' ');
					double c1c2Space = trainer.getProbability(c3, c2, c1, ' ');

					System.err.println("" + c3 + c2 + c1 + "e =" + c1c2e
							+ "\t\t" + c2 + c1 + "e[Space]=" + c2eSpace
							+ "\t\t" + c3 + c2 + c1 + "[Space]=" + c1c2Space
							+ "\t\t" + e.getMessage());
				} else {
					System.err.println(e.getMessage());
				}
			}
		}
		System.out.println("Total corrects: " + count);
		System.out.println("Total errors: " + countE);
	}
}
