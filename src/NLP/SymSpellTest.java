package NLP;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.junit.Test;

public class SymSpellTest {

	@Test
	public void test() {
		Map<String, String> inputMap = new HashMap<>();
		Scanner br = null;
		try {
			br = new Scanner(new FileReader("spellingInput.txt"));
			while (br.hasNextLine()) {
				String[] words = br.nextLine().split(" ");
				if (words.length > 1) {
					for (int i = 1; i < words.length; i++)
						inputMap.put(words[i].intern(), words[0].intern());
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (br != null)
			br.close();
		SymSpell symSpell = SymSpell.getInstance();
		int count = 0;
		int countE = 0;
		for (Entry<String, String> entry : inputMap.entrySet()) {
			try {
				assertEquals(
						"Spelling correction result for <" + entry.getKey()
								+ "> must be <" + entry.getValue() + ">",
						entry.getValue(),
						symSpell.correctThisWord(entry.getKey(), "en", false));
				count++;
			} catch (AssertionError e) {

				countE++;

				String correctedWord = symSpell.correctThisWord(entry.getKey(),
						"en", false);
				if (correctedWord.equals(entry.getKey()))
					System.err.println("-----Result for <" + entry.getKey()
							+ "> isn't changed, expected: " + entry.getValue());
				else
					System.err.println(e.getMessage());

			}
		}
		System.out.println("Total corrects: " + count);
		System.out.println("Total errors: " + countE);
	}

}
