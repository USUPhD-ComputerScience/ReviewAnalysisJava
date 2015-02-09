package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Util {
	/**
	 * Generate a hash number
	 * 
	 * @param input
	 *            - a String
	 * @return the hash number generated
	 */
	public static long generateHash(final CharSequence input) {
		long hash = 1610612741l;
		for (int i = 0; i < input.length(); i++) {
			hash = hash * 31 + input.charAt(i);
		}
		return hash;
	}

	/**
	 * Standardize the input text:
	 *
	 * <pre>
	 * 1. Remove non-characters, retain numbers and DOT
	 * 2. Replace ? ! : ; with DOT
	 * 3. Replace multiple dot with 1 dot
	 * </pre>
	 * 
	 * @param input
	 *            - a String of text
	 * @return the standardized text
	 */
	public static String standardizeText(final CharSequence input) {
		final StringBuilder sb = new StringBuilder(input.length());
		char lastChar = 0;
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c == 39 || c == 32 || (c > 47 && c < 58) || (c > 64 && c < 91)
					|| (c > 96 && c < 123)) {
				sb.append(c);
			} else if (c == 33 || c == 58 || c == 63 || c == 59 || c == 46) {
				if (lastChar != 46)
					sb.append(" ");
			} else {
				sb.append(" ");
			}
			lastChar = c;
		}
		return sb.toString();
	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static String removeNonChars(final CharSequence input) {
		final StringBuilder sb = new StringBuilder(input.length());
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if ((c > 64 && c < 91) || (c > 96 && c < 123) || (c == ' ')) {
				sb.append(c);
			} else {
				if (c == '.' || c == ',')
					sb.append(' ');
				else
					sb.append(" ");
			}

		}
		return sb.toString();
	}

	public static List<String> listFilesForFolder(final String folderName)
			throws IOException {
		List<String> filePaths = new ArrayList<>();

		Files.walk(Paths.get(folderName)).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				filePaths.add(filePath.toString());
			}
		});
		return filePaths;
	}
}
