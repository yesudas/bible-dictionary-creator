/**
 * 
 */
package in.wordofgod.bible.dictionary.creator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * 
 */
public class Utils {

	public static String trimDictionaryWord(String word) {
		return word.trim().strip().trim();
	}

	static void initSystemOutSettings() {
		if (BibleDictionaryCreator.WRITE_LOGS_TO_FILE) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH mm ss");
			String timeStamp = dateFormat.format(new Date());
			timeStamp = timeStamp.replaceAll(" ", "-");
			File outputFile = new File(BibleDictionaryCreator.folderPath.replace(BibleDictionaryCreator.outputFile, "")
					+ "/" + BibleDictionaryCreator.outputFile + "-logs-" + timeStamp + ".txt");
			System.out.println("Output-Results are stored at :: " + outputFile.getAbsolutePath());
			try {
				System.setOut(new PrintStream(outputFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	static void loadDictionaryDetails() {
		BibleDictionaryCreator.DICTIONARY_DETAILS = new Properties();
		BufferedReader propertyReader;
		try {
			File infoFile = new File(BibleDictionaryCreator.folderPath + "//" + BibleDictionaryCreator.INFORMATION_FILE_NAME);
			propertyReader = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile), "UTF8"));
			BibleDictionaryCreator.DICTIONARY_DETAILS.load(propertyReader);
			propertyReader.close();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static String convertToDecimalNCR(String str) {
		String preserve = "ascii";
		int haut = 0;
		String cp;
		String CPstring = "";
		String before = "&#";
		String after = ";";
	
		for (int i = 0; i < str.length(); i++) {
			int b = str.codePointAt(i);
			if (b < 0 || b > 0xFFFF) {
				return null;
			}
			if (haut != 0) {
				if (0xDC00 <= b && b <= 0xDFFF) {
					cp = "" + 0x10000 + ((haut - 0xD800) << 10) + (b - 0xDC00);
					CPstring += before + cp + after;
					haut = 0;
					continue;
				} else {
					return null;
				}
			}
			if (0xD800 <= b && b <= 0xDBFF) {
				haut = b;
			} else {
				if (preserve.equals("ascii") && b <= 127) {
					CPstring += str.charAt(i);
				} else {
					cp = "" + b;
					CPstring += before + cp + after;
				}
			}
		}
		return CPstring;
	}

	static boolean checkForInValidFile(File file) {
		return BibleDictionaryCreator.INFORMATION_FILE_NAME.equalsIgnoreCase(file.getName())
				|| BibleDictionaryCreator.MAPPING_FILE_NAME.equalsIgnoreCase(file.getName())
				|| BibleDictionaryCreator.MAPPING_FILE_NAME_FOR_REVIEW.equalsIgnoreCase(file.getName())
				|| file.isDirectory();
	}

}
