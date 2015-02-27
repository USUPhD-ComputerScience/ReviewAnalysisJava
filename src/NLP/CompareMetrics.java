package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CompareMetrics {
	public static HashSet<String> likelihood = new HashSet<>();
	public static HashSet<String> ttest = new HashSet<>();

	public static void main(String[] args) throws Throwable {
		loadTests(new File("E:\\AndroidAnalysis\\ReviewData\\data\\v5\\"
				+ "likelyHoodRatioManning.csv"), likelihood);
		loadTests(new File("E:\\AndroidAnalysis\\ReviewData\\data\\v5\\"
				+ "ttest.csv"), ttest);
		HashSet<String> notTtest = new HashSet<>(likelihood);
		notTtest.removeAll(ttest);
		writeSet(new File("E:\\AndroidAnalysis\\ReviewData\\data\\v5\\"
				+ "notTtest.csv"), notTtest);
	}

	private static void loadTests(File file, HashSet<String> set)
			throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNextLine()) {
			String[] values = br.nextLine().split(",");
			if (values.length == 3)
				set.add(values[0]);
		}
		br.close();
	}

	private static void writeSet(File file, HashSet<String> set) throws Throwable {
		PrintWriter pw = new PrintWriter(new FileWriter(file));
		for (String str : set) {
			pw.println(str);
		}
		pw.close();
	}
}
