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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.wordofgod.bible.parser.BookIdentifier;
import in.wordofgod.bible.parser.BookInfo;

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
			File infoFile = new File(
					BibleDictionaryCreator.folderPath + "//" + BibleDictionaryCreator.INFORMATION_FILE_NAME);
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

	static Set<String> getAllReferences(String sb, boolean secondTime) {
		// மத்.அதி. 12; லூக்கா 1:26-35; 2:1-39
		sb = removeHTMLTags(sb);
		Set<String> finalList = new HashSet<String>();
		// மத். அதி. 10; ; ;<p/>மத். அதி. 12; மத். அதி. 13; மத். அதி. 14;
		Pattern p0 = Pattern.compile("[^\\(-> a-zA-Z][\\S]+. அதி. [0-9]+;");
		// அப். 6:5, 8-15
		Pattern p1 = Pattern.compile(
				"[0-9I ]*[\\S]+\\. [0-9]+:[0-9]+[-]*[0-9]*, [0-9]*[-]*[0-9]*[;, ]*[0-9]*[:]*[0-9]*[-]*[0-9]*[;, ]*[0-9]*[:]*[0-9]*[-]*[0-9]*[;, ]*[0-9]*[:]*[0-9]*[-]*[0-9]*[;, ]*[0-9]*[:]*[0-9]*[-]*[0-9]*[;, ]*[0-9]*[:]*[0-9]*[-]*[0-9]*");
		// 1 இரா. 22:51-53 and 1 இரா 22:51-53 and ஆதி. 15:1- 4
		Pattern p2 = Pattern.compile("[0-9I ]*[\\S]+\\. [0-9]+:[0-9]+[- ]*[0-9]*");
		// ஆதி. 1:26; 2:7
		Pattern p3 = Pattern.compile(
				"[0-9I ]*[\\S]+\\. [0-9]+:[0-9]+[-]*[0-9]*;[ ]*[0-9I ]*[\\S]*[\\. ]*[0-9]+:[0-9]+[-]*[0-9]*[; ]*[0-9]*[:]*[0-9]*[-]*[0-9]*[; ]*[0-9]*[:]*[0-9]*[-]*[0-9]*[; ]*[0-9]*[:]*[0-9]*[-]*[0-9]*[; ]*[0-9]*[:]*[0-9]*[-]*[0-9]*[; ]*[0-9]*[:]*[0-9]*[-]*[0-9]*");
		// 2இரா.1:1
		Pattern p4 = Pattern.compile(
				"[0-9I ]*[\\S]+\\.[0-9]+:[0-9]+[-]*[0-9]*[, ]*[0-9]*[-]*[0-9]*[, ]*[0-9]*[-]*[0-9]*[, ]*[0-9]*[-]*[0-9]*[, ]*[0-9]*[-]*[0-9]*[, ]*[0-9]*[-]*[0-9]*");
		// 1 நாளா 12:23; ஆதி 13:24; யாத் 14:25;யோவா 12:5
		Pattern p5 = Pattern.compile("[0-9I]*[ ]*[\\S]+[\\.]*[ ]+[0-9]+:[0-9]+[-]*[0-9]*[;]*");
		// 1 நாளா 12:23; 13:24; 14;25;15:24; 65:12 65:12 65:12 65:12 65:12 65:12
		Pattern p6 = Pattern.compile("[0-9I]*[ ]*[\\S]+[\\.]*[ ]+[0-9]+:[0-9]+[ 0-9:0-9-,;]*");
		// அப். 14:1; 18:4; 19:10; ரோம. 1:16; 10:12; 20:21; 1 கொரி. 1:22-24; கலா. 3:28;
		// கொலோ. 3:11
		Pattern p7 = Pattern.compile("[0-9I]*[ ]*[\\S]+[\\.]*[ ]*[0-9]+:[0-9]+[-,]*[0-9]*");
		// மத். அதி. 5-7; 13:1-53 OR மத். அதி. 10,11, 12, 13 OR மத். அதி. 5-7
		Pattern p8 = Pattern.compile(
				"[0-9I]*[ ]*[\\S]+[\\. ]+[அதி]+[\\. ]+[0-9]+[-, ;]*[0-9]*[-, ;:]*[0-9]*[-, ;:]*[0-9]*[-, ;:]*[0-9]*");
		Matcher m;
		String str = null;
		if (secondTime) {
			m = p0.matcher(sb);
			while (m.find()) {
				str = m.group();
				str = cleanUpUnwantedCharacters(str);
				System.out.println("Pattern 0: " + str);
				finalList.add(str.trim());
			}
		}
		if (!secondTime) {
			m = p1.matcher(sb);
			while (m.find()) {
				str = m.group();
				str = cleanUpUnwantedCharacters(str);
				System.out.println("Pattern 1: " + str);
				finalList.add(str.trim());
			}
		}
		if (!secondTime) {
			m = p2.matcher(sb);
			while (m.find()) {
				str = m.group();
				str = cleanUpUnwantedCharacters(str);
				System.out.println("Pattern 2: " + str);
				finalList.add(str.trim());
			}
		}
		if (!secondTime) {
			m = p3.matcher(sb);
			while (m.find()) {
				str = m.group();
				str = cleanUpUnwantedCharacters(str);
				System.out.println("Pattern 3: " + str);
				finalList.add(str.trim());
			}
		}
		if (!secondTime) {
			m = p4.matcher(sb);
			while (m.find()) {
				str = m.group();
				str = cleanUpUnwantedCharacters(str);
				System.out.println("Pattern 4: " + str);
				finalList.add(str.trim());
			}
		}
		if (!secondTime) {
			m = p5.matcher(sb);
			while (m.find()) {
				str = m.group();
				str = cleanUpUnwantedCharacters(str);
				System.out.println("Pattern 5: " + str);
				finalList.add(str.trim());
			}
		}
		if (!secondTime) {
			m = p6.matcher(sb);
			while (m.find()) {
				str = m.group();
				str = cleanUpUnwantedCharacters(str);

				boolean found = false;
				for (String temp : finalList) {
					if (temp.endsWith(str)) {
						found = true;
						break;
					}
				}
				if (!found) {
					for (String temp : finalList) {
						if (temp.startsWith(str) || str.contains(temp)) {
							found = true;
							break;
						}
					}
					if (!found) { // ரோமர் 15:26;1
						Pattern temp = Pattern.compile(".+(;[0-9]+)");
						m = temp.matcher(str);
						if (m.find()) {
							if (m.groupCount() > 1) {
								str = str.replace(m.group(2), "");
							}
						}
						finalList.add(str.trim());
						System.out.println("Pattern 6: " + str);
					}
				}
			}
		}
		m = p7.matcher(sb);
		while (m.find()) {
			str = m.group();
			str = cleanUpUnwantedCharacters(str);
			System.out.println("Pattern 7: " + str);
			finalList.add(str.trim());
		}
		if (!secondTime) {
			m = p8.matcher(sb);
			while (m.find()) {
				str = m.group();
				str = cleanUpUnwantedCharacters(str);
				System.out.println("Pattern 8: " + str);
				finalList.add(str.trim());
			}
		}
		System.out.println("Final list: " + finalList);
		return finalList;
	}

	private static String cleanUpUnwantedCharacters(String str) {
		str = str.replaceAll("\\(", "").trim();
		if (str.endsWith(",")) {
			str = str.substring(0, str.length() - 1);
		}
		if (str.endsWith(";")) {
			str = str.substring(0, str.length() - 1);
		}
		if (str.endsWith("-")) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	public static String removeHTMLTags(String text) {
		while (text.contains("<")) {
			int startPos = text.indexOf("<");
			int endPos = text.indexOf(">");
			String htmlTag = text.substring(startPos, endPos + 1);
			text = text.replace(htmlTag, " ");
		}
		return text;
	}

	static String getLinkForVersesList(String portion, String outputFormat) {
		String BOOK_NAME;
		String CHAPTER_NUMBER;
		String VERSE_NUMBER;
		String link = "";
		String[] chapterAndVersesArray;
		String REFERENCE_LINK_FORMAT_1 = "";

		if (portion.startsWith("1 ") || portion.startsWith("2 ") || portion.startsWith("3 ") || portion.startsWith("I ")
				|| portion.startsWith("II ")) {
			String[] tempArr = portion.split(" ");
			if (tempArr.length > 2) {// 2 இரா. 8:26
				chapterAndVersesArray = tempArr[2].split(":");
				BOOK_NAME = tempArr[0] + " " + tempArr[1];
				CHAPTER_NUMBER = chapterAndVersesArray[0];
			} else if (tempArr[1].contains(".")) {// 2 நாளா.22:3
				String[] tempArrBookNme = tempArr[1].split("\\.");
				chapterAndVersesArray = tempArrBookNme[1].split(":");
				BOOK_NAME = tempArr[0] + " " + tempArrBookNme[0];
				CHAPTER_NUMBER = chapterAndVersesArray[0];
			} else {// 2 நாளா22:3
				String tempBookName = "";
				for (int i = 0; i < tempArr[1].length(); i++) {
					char character = tempArr[1].charAt(i);
					if (!Character.isDigit(character)) {
						tempBookName = tempBookName + character;
					}
				}
				BOOK_NAME = tempArr[0] + " " + tempBookName;
				chapterAndVersesArray = tempArr[1].replace(tempBookName, "").split(":");
				CHAPTER_NUMBER = chapterAndVersesArray[0];
			}
		} else {
			String[] tempArr = portion.split(" ");
			if (tempArr[0].contains(".") && !portion.contains(". ")) {// 2இரா.1:1 OR நெகே. 7:2 or அப். 7:5
				String[] tempArrBookNme = tempArr[0].split("\\.");
				chapterAndVersesArray = tempArrBookNme[1].split(":");
				BOOK_NAME = tempArrBookNme[0];
				CHAPTER_NUMBER = chapterAndVersesArray[0];
			} else {
				chapterAndVersesArray = tempArr[1].split(":");
				BOOK_NAME = tempArr[0];
				CHAPTER_NUMBER = chapterAndVersesArray[0];
			}
		}

		if (ZefaniaXML.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			REFERENCE_LINK_FORMAT_1 = ZefaniaXML.REFERENCE_LINK_FORMAT_1;
		} else if (TheWord.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			REFERENCE_LINK_FORMAT_1 = TheWord.REFERENCE_LINK_FORMAT_1;
		} else if (MyBibleZone.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			REFERENCE_LINK_FORMAT_1 = MyBibleZone.REFERENCE_LINK_FORMAT_1;
		}

		if (chapterAndVersesArray.length > 1) {
			String verses = chapterAndVersesArray[1];
			String[] verseArray = verses.split(",");
			if (verseArray.length > 1) {
				StringBuilder sbVerse = new StringBuilder();
				for (String arrObj : verseArray) {
					VERSE_NUMBER = arrObj;
					String temp = getBookIdentifier(BOOK_NAME, outputFormat);
					if (temp != null) {
						sbVerse.append(REFERENCE_LINK_FORMAT_1.replace("BOOK_NUMBER", temp)
								.replace("CHAPTER_NUMBER", CHAPTER_NUMBER).replace("VERSE_NUMBER", VERSE_NUMBER)
								.replace("BIBLE_PORTION", BOOK_NAME + " " + CHAPTER_NUMBER + ":" + VERSE_NUMBER))
								.append("; ");
					}
				}
				link = sbVerse.toString();
			} else {
				VERSE_NUMBER = verseArray[0];
				String temp = getBookIdentifier(BOOK_NAME, outputFormat);
				if (temp != null) {
					link = REFERENCE_LINK_FORMAT_1.replace("BOOK_NUMBER", temp)
							.replace("CHAPTER_NUMBER", CHAPTER_NUMBER).replace("VERSE_NUMBER", VERSE_NUMBER)
							.replace("BIBLE_PORTION", portion);
				}
			}
		}
		System.out.println(outputFormat + " : Generated Reference link: " + link);
		return link;
	}

	private static String getBookIdentifier(String bookName, String outputFormat) {
		BookInfo bookInfo = null;
		if (ZefaniaXML.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			bookInfo = BookIdentifier.getBookInfo(bookName.replace(".", ""));
			if (bookInfo != null) {
				return bookInfo.getZefID() + "";
			}
		} else if (TheWord.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			bookInfo = BookIdentifier.getBookInfo(bookName.replace(".", ""));
			if (bookInfo != null) {
				return bookInfo.getZefID() + "";
			}
		} else if (MyBibleZone.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			bookInfo = BookIdentifier.getBookInfo(bookName.replace(".", ""));
			if (bookInfo != null) {
				return bookInfo.getMyBibleZoneId() + "";
			}
		}
		System.out.println(outputFormat + " : Invalid book name: " + bookName);
		return null;
	}

	static String getLinkForVersesRange(String portion, String outputFormat) {
		String BOOK_NAME;
		String CHAPTER_NUMBER;
		String VERSE_NUMBER;
		String STARTING_VERSE_NUMBER;
		String ENDING_VERSE_NUMBER;
		String REFERENCE_LINK_FORMAT_1 = "";
		String REFERENCE_LINK_FORMAT_2 = "";
		String[] portionArr = new String[2];
		String[] chapterAndVersesArray;
		String[] versesArray;
		if (portion.startsWith("1 ") || portion.startsWith("2 ") || portion.startsWith("3 ")) {
			String[] tempArr = portion.split(" ");
			if (tempArr.length > 2) {// 1 நாளா 3:16-17
				chapterAndVersesArray = tempArr[2].split(":");
				BOOK_NAME = tempArr[0] + " " + tempArr[1];
				CHAPTER_NUMBER = chapterAndVersesArray[0];
			} else if (tempArr[1].contains(".")) {// 1 நாளா.3:16-17
				String[] tempArrBookNme = tempArr[1].split("\\.");
				chapterAndVersesArray = tempArrBookNme[1].split(":");
				BOOK_NAME = tempArr[0] + " " + tempArrBookNme[0];
				CHAPTER_NUMBER = chapterAndVersesArray[0];
			} else {// 1 நாளா3:16-17
				String tempBookName = "";
				for (int i = 0; i < tempArr[1].length(); i++) {
					char character = tempArr[1].charAt(i);
					if (!Character.isDigit(character)) {
						tempBookName = tempBookName + character;
					}
				}
				BOOK_NAME = tempArr[0] + " " + tempBookName;
				chapterAndVersesArray = tempArr[1].replace(tempBookName, "").split(":");
				CHAPTER_NUMBER = chapterAndVersesArray[0];
			}
		} else {
			if (portion.contains(".")) {
				String[] temp = portion.split("\\. ");
				if (temp.length > 1) {
					portionArr[0] = temp[0] + ".";
					portionArr[1] = temp[1];
				} else {
					temp = portion.split("\\.");
					portionArr[0] = temp[0] + ".";
					portionArr[1] = temp[1];
				}
			} else {
				portionArr = portion.trim().split(" ");
			}
			BOOK_NAME = portionArr[0];
			chapterAndVersesArray = portionArr[1].trim().split(":");
			CHAPTER_NUMBER = chapterAndVersesArray[0];
		}
		String verses = chapterAndVersesArray[1];
		versesArray = verses.trim().split("-");
		STARTING_VERSE_NUMBER = versesArray[0];
		String link = "";
		if (ZefaniaXML.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			REFERENCE_LINK_FORMAT_1 = ZefaniaXML.REFERENCE_LINK_FORMAT_1;
			REFERENCE_LINK_FORMAT_2 = ZefaniaXML.REFERENCE_LINK_FORMAT_2;
		} else if (TheWord.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			REFERENCE_LINK_FORMAT_1 = TheWord.REFERENCE_LINK_FORMAT_1;
			REFERENCE_LINK_FORMAT_2 = TheWord.REFERENCE_LINK_FORMAT_2;
		} else if (MyBibleZone.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			REFERENCE_LINK_FORMAT_1 = MyBibleZone.REFERENCE_LINK_FORMAT_1;
			REFERENCE_LINK_FORMAT_2 = MyBibleZone.REFERENCE_LINK_FORMAT_2;
		}

		if (versesArray.length > 1) {
			ENDING_VERSE_NUMBER = versesArray[1];
			String temp = getBookIdentifier(BOOK_NAME, outputFormat);
			if (temp != null) {
				link = REFERENCE_LINK_FORMAT_2.replace("BOOK_NUMBER", temp).replace("CHAPTER_NUMBER", CHAPTER_NUMBER)
						.replace("STARTING_VERSE_NUMBER", STARTING_VERSE_NUMBER)
						.replace("ENDING_VERSE_NUMBER", ENDING_VERSE_NUMBER).replace("BIBLE_PORTION", portion);
			}
		} else {
			VERSE_NUMBER = versesArray[0];
			String temp = getBookIdentifier(BOOK_NAME, outputFormat);
			if (temp != null) {
				link = REFERENCE_LINK_FORMAT_1.replace("BOOK_NUMBER", temp).replace("CHAPTER_NUMBER", CHAPTER_NUMBER)
						.replace("VERSE_NUMBER", VERSE_NUMBER)
						.replace("BIBLE_PORTION", portionArr[0] + " " + CHAPTER_NUMBER + ":" + VERSE_NUMBER);
			}
		}
		System.out.println(outputFormat + " : Generated Reference link: " + link);
		return link;
	}

	static String prepareReferences(String sb, String OUTPUT_FORMAT) {
		System.out.println(OUTPUT_FORMAT + " : Preparing the References started...");
		System.out.println(OUTPUT_FORMAT + " : Data before Splitting the bible portions: " + sb);
		Set<String> biblePortions = getAllReferences(sb, false);
		for (String portion : biblePortions) {
			System.out.println(OUTPUT_FORMAT + " : Trying to process the bible Portion for splitting: " + portion);
			try {
				if (!portion.contains(":")) {// மத். அதி. 10,11, 12, 13; OR மத். அதி. 14, 12-13. OR மத். அதி. 10;
					String portion2 = processChapters(portion);
					System.out.println(OUTPUT_FORMAT + " : Splitting the bible portions : " + portion
							+ " is splitted into " + portion2);
					sb = sb.replace(portion, portion2);
				} else if (portion.contains("-") && !portion.contains(",") && !portion.contains(";")) {// Gen 10:1-20;
					System.out.println(OUTPUT_FORMAT + " : Do not anything, bible portion is fine");
				} else if (portion.contains(";")) {// 2 நாளா. 29:12; 31:18 OR ஆதி. 17:1; யாத். 6:3
					String[] portionArr = portion.split(";");
					String portion1 = portionArr[0].trim();
					if (!portion1.contains(":")) {// மத். அதி. 5-7; 13:1-53
						String portionNew = processChapters(portion1);
						System.out.println(OUTPUT_FORMAT + " : Splitting the bible portions : " + portion1
								+ " is splitted into " + portionNew);
						sb = sb.replace(portion1, portionNew);
						String portion2 = portionArr[1].trim();
						boolean startsWithNumber = startsWithNumber(portion2);
						if (!portion2.contains(":")) {
							String[] portionArrTemp = portion1.trim().split(" ");
							String bookName = portionArrTemp[0] + " " + portionArrTemp[1];
							portionNew = processChapters(bookName + " " + portion2);
							System.out.println(OUTPUT_FORMAT + " : Splitting the bible portions : " + portion2
									+ " is splitted into " + portionNew);
							sb = sb.replace(portion2, portionNew);
							continue;
						} else if (startsWithNumber) {
							String[] portionArrTemp = portion1.trim().split(" ");
							String bookName = portionArrTemp[0] + " " + portionArrTemp[1];
							portionArr[1] = bookName + " " + portion2;
							System.out.println(OUTPUT_FORMAT + " : Splitting the bible portions : " + portion2
									+ " is splitted into " + portionArr[1]);
							sb = sb.replace(portion2, portionArr[1]);
							continue;
						}
					}
					String portion2 = null;
					if (portion1.startsWith("1 ") || portion1.startsWith("2 ") || portion1.startsWith("3 ")
							|| portion1.startsWith("I ") || portion1.startsWith("II ")) {// 1 பேதுரு. 1:19; யோவான் 21:15
						String[] tempArr = portion1.split(" ");
						String BOOK_NAME;
						if (tempArr.length > 2) {// 2 இரா. 8:26
							BOOK_NAME = tempArr[0] + " " + tempArr[1];
						} else if (tempArr[1].contains(".")) {// 2 நாளா.22:3
							String[] tempArrBookNme = tempArr[1].split("\\.");
							BOOK_NAME = tempArr[0] + " " + tempArrBookNme[0];
						} else {// 2 நாளா22:3
							String tempBookName = "";
							for (int i = 0; i < tempArr[1].length(); i++) {
								char character = tempArr[1].charAt(i);
								if (!Character.isDigit(character)) {
									tempBookName = tempBookName + character;
								}
							}
							BOOK_NAME = tempArr[0] + " " + tempBookName;
						}
						portionArr[1] = portionArr[1].trim();
						boolean startsWithNumber = false;
						if (portionArr[1].startsWith("1 ") || portionArr[1].startsWith("2 ")
								|| portionArr[1].startsWith("I ") || portionArr[1].startsWith("II ")) {
							startsWithNumber = false;
						} else {
							startsWithNumber = startsWithNumber(portionArr[1]);
						}
						if (startsWithNumber) {
							portion2 = BOOK_NAME + " " + portionArr[1];
						} else {
							portion2 = portionArr[1];
						}
					} else if (!portionArr[1].contains(":")) {// ஆதி.3:4-6, 23 OR எரே. 43:7, 11-13
						String BOOK_NAME;
						String CHAPTER_NUMBER;
						String[] chapterAndVersesArray;
						if (portion1.contains(".")) {
							String[] tempArrBookNme = portion1.split("\\.");
							BOOK_NAME = tempArrBookNme[0];
							chapterAndVersesArray = tempArrBookNme[1].split(":");
							CHAPTER_NUMBER = chapterAndVersesArray[0];
						} else if (portion1.contains(" ")) {
							String[] tempArrBookNme = portion1.split(" ");
							BOOK_NAME = tempArrBookNme[0];
							chapterAndVersesArray = tempArrBookNme[1].split(":");
							CHAPTER_NUMBER = chapterAndVersesArray[0];
						} else {
							String tempBookName = "";
							for (int i = 0; i < portion1.length(); i++) {
								char character = portion1.charAt(i);
								if (!Character.isDigit(character)) {
									tempBookName = tempBookName + character;
								}
							}
							BOOK_NAME = tempBookName;
							chapterAndVersesArray = portion1.replace(tempBookName, "").split(":");
							CHAPTER_NUMBER = chapterAndVersesArray[0];
						}
						portion2 = BOOK_NAME + " " + CHAPTER_NUMBER.trim() + ":" + portionArr[1].trim();
					} else {
						String[] tempArr = portion1.split(" ");
						portion2 = "";
						for (int i = 1; i < portionArr.length; i++) {
							portionArr[i] = portionArr[i].trim();
							boolean startsWithNumber = false;
							if (portionArr[i].startsWith("1 ") || portionArr[i].startsWith("2 ")
									|| portionArr[i].startsWith("I ") || portionArr[i].startsWith("II ")) {
								startsWithNumber = false;
							} else {
								startsWithNumber = startsWithNumber(portionArr[i]);
							}
							if (portionArr[i].contains(":") && startsWithNumber) {
								portion2 = portion2 + tempArr[0] + " " + portionArr[i] + "; ";
							} else if (startsWithNumber) {
								String[] chapterAndVersesArray = tempArr[1].split(":");
								portion2 = portion2 + tempArr[0] + " " + chapterAndVersesArray[0] + ":" + portionArr[i]
										+ "; ";
							} else {
								portion2 = portion2 + portionArr[i] + "; ";
							}

						}
					}
					System.out.println(OUTPUT_FORMAT + " : Splitting the bible portions : " + portion
							+ " is splitted into " + portion1 + " and " + portion2);
					sb = sb.replace(portion, portion1 + "; " + portion2);
				} else {// Gen 12:2,3,4; // அப். 6:5, 8-15
					if (portion.contains("-")) {
						String[] portionArr = portion.split(",");
						String portion1 = portionArr[0];
						String portion2 = null;
						if (portion1.startsWith("1 ") || portion1.startsWith("2 ") || portion1.startsWith("3 ")
								|| portion1.startsWith("I ") || portion1.startsWith("II ")) {
							String[] tempArr = portion1.split(" ");
							String BOOK_NAME;
							String CHAPTER_NUMBER;
							String[] chapterAndVersesArray;
							if (tempArr.length > 2) {// 2 இரா. 8:26
								chapterAndVersesArray = tempArr[2].split(":");
								BOOK_NAME = tempArr[0] + " " + tempArr[1];
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							} else if (tempArr[1].contains(".")) {// 2 நாளா.22:3
								String[] tempArrBookNme = tempArr[1].split("\\.");
								chapterAndVersesArray = tempArrBookNme[1].split(":");
								BOOK_NAME = tempArr[0] + " " + tempArrBookNme[0];
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							} else {// 2 நாளா22:3
								String tempBookName = "";
								for (int i = 0; i < tempArr[1].length(); i++) {
									char character = tempArr[1].charAt(i);
									if (!Character.isDigit(character)) {
										tempBookName = tempBookName + character;
									}
								}
								BOOK_NAME = tempArr[0] + " " + tempBookName;
								chapterAndVersesArray = tempArr[1].replace(tempBookName, "").split(":");
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							}
							portion2 = BOOK_NAME + " " + CHAPTER_NUMBER + ":" + portionArr[1].trim();
							System.out.println(OUTPUT_FORMAT + " : Splitting the bible portions : " + portion
									+ " is splitted into " + portion1 + " and " + portion2);
							sb = sb.replace(portion, portion1 + "; " + portion2);
						} else if (!portionArr[1].contains(":")) {// ஆதி.3:4-6, 23 OR எரே. 43:7, 11-13
							String BOOK_NAME;
							String CHAPTER_NUMBER;
							String[] chapterAndVersesArray;
							if (portion1.contains(".")) {
								String[] tempArrBookNme = portion1.split("\\.");
								BOOK_NAME = tempArrBookNme[0];
								chapterAndVersesArray = tempArrBookNme[1].split(":");
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							} else if (portion1.contains(" ")) {
								String[] tempArrBookNme = portion1.split(" ");
								BOOK_NAME = tempArrBookNme[0];
								chapterAndVersesArray = tempArrBookNme[1].split(":");
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							} else {
								String tempBookName = "";
								for (int i = 0; i < portion1.length(); i++) {
									char character = portion1.charAt(i);
									if (!Character.isDigit(character)) {
										tempBookName = tempBookName + character;
									}
								}
								BOOK_NAME = tempBookName;
								chapterAndVersesArray = portion1.replace(tempBookName, "").split(":");
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							}
							portion2 = BOOK_NAME + " " + CHAPTER_NUMBER.trim() + ":" + portionArr[1].trim();
							System.out.println(OUTPUT_FORMAT + " : Splitting the bible portions : " + portion
									+ " is splitted into " + portion1 + " and " + portion2);
							sb = sb.replace(portion, portion1 + "; " + portion2);
						} else {
							String[] tempArr = portion1.split(" ");
							String[] chapterAndVersesArray = tempArr[1].split(":");
							portion2 = tempArr[0] + " " + chapterAndVersesArray[0] + ":" + portionArr[1].trim();
							System.out.println(OUTPUT_FORMAT + " : Splitting the bible portions : " + portion
									+ " is splitted into " + portion1 + " and " + portion2);
							sb = sb.replace(portion, portion1 + "; " + portion2);
						}
					} else {
						System.out.println(OUTPUT_FORMAT + " : Do not anything, bible portion is fine");
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				//TODO display these logs to now the various use cases where the program is not able to apply links
				//e.printStackTrace();
				//System.out.println(OUTPUT_FORMAT + " : ArrayIndexOutOfBoundsException for: " + portion);
			}

		}
		System.out.println(OUTPUT_FORMAT + " : Final Data after Splitting the bible portions: " + sb);
		System.out.println(OUTPUT_FORMAT + " : Preparing the References completed...");
		return sb;
	}

	private static boolean startsWithNumber(String str) {
		boolean startsWithNumber = false;
		char c = str.charAt(0);
		startsWithNumber = c >= '0' && c <= '9';
		return startsWithNumber;
	}

	private static String processChapters(String portion) {
		String[] portionArr = portion.trim().split(" ");
		String bookName = portionArr[0] + " " + portionArr[1];
		Set<String> chapterSet = new HashSet<String>();
		for (int i = 2; i < portionArr.length; i++) {
			try {
			String chapter = portionArr[i].trim();
			if (chapter.contains(",")) {
				String[] chapterArr = chapter.split(",");
				chapterSet.addAll(Arrays.asList(chapterArr));
			} else if (chapter.contains("-")) {
				String[] chapterArr = chapter.split("-");
				try {
					for (int j = Integer.parseInt(chapterArr[0].trim()); j < Integer
							.parseInt(chapterArr[1].trim()); j++) {
						chapterSet.add("" + j);
					}
				} catch (NumberFormatException e) {
					//TODO display these logs to now the various use cases where the program is not able to apply links
					//e.printStackTrace();
				}
				chapterSet.addAll(Arrays.asList(chapterArr));
			} else {
				chapterSet.add(chapter.trim());
			}
			}catch (NumberFormatException e) {
				//TODO display these logs to now the various use cases where the program is not able to apply links
				//e.printStackTrace();
				//System.out.println(
					//	OUTPUT_FORMAT + " : Unable to create reference link for the bible Portion: " + portion);
			}
		}
		String portion2 = "";
		for (String chapter : chapterSet) {
			portion2 = portion2 + bookName + " " + chapter + "; ";
		}
		return portion2;
	}

	static String applyLinksToReferences(String sb, String OUTPUT_FORMAT) {
		sb = prepareReferences(sb, OUTPUT_FORMAT);
		System.out.println(OUTPUT_FORMAT + " : Applying Links To References started...");
		Set<String> biblePortions = getAllReferences(sb, true);
		for (String portion : biblePortions) {
			System.out.println(OUTPUT_FORMAT + " : Trying to process the bible Portion: " + portion);
			try {
				if (!portion.contains(":")) {// மத். அதி. 10; மத். அதி. 12;
					String[] portionArr = portion.trim().split(" ");
					String bookName = portionArr[0];
					String[] chapterArray = portionArr[1].trim().split(",");
					String link = getLinkForChapter(portion, bookName, portionArr[2], OUTPUT_FORMAT);
					sb = sb.replace(portion, link).replace("; ;", ";").replace("; ;", ";");
				} else if (portion.contains("-") && !portion.contains(",")) {// Gen 10:1-20;
					String link = getLinkForVersesRange(portion, OUTPUT_FORMAT);
					sb = sb.replace(portion, link);
				} else if (portion.contains(";")) {// 2 நாளா. 29:12; 31:18
					String[] portionArr = portion.split(";");
					sb = sb.replace(portionArr[0], getLinkForVersesList(portionArr[0], OUTPUT_FORMAT));
					String portion1 = portionArr[0];
					if (portion1.startsWith("1 ") || portion1.startsWith("2 ") || portion1.startsWith("3 ")) {
						String[] tempArr = portion1.split(" ");
						String BOOK_NAME;
						if (tempArr.length > 2) {// 2 இரா. 8:26
							BOOK_NAME = tempArr[0] + " " + tempArr[1];
						} else if (tempArr[1].contains(".")) {// 2 நாளா.22:3
							String[] tempArrBookNme = tempArr[1].split("\\.");
							BOOK_NAME = tempArr[0] + " " + tempArrBookNme[0];
						} else {// 2 நாளா22:3
							String tempBookName = "";
							for (int i = 0; i < tempArr[1].length(); i++) {
								char character = tempArr[1].charAt(i);
								if (!Character.isDigit(character)) {
									tempBookName = tempBookName + character;
								}
							}
							BOOK_NAME = tempArr[0] + " " + tempBookName;
						}
						String derivedPortion = BOOK_NAME + " " + portionArr[1].trim();
						sb = sb.replace(portionArr[1], getLinkForVersesList(derivedPortion, OUTPUT_FORMAT));
					}
				} else {// Gen 12:2,3,4; // அப். 6:5, 8-15
					if (portion.contains("-")) {
						String[] portionArr = portion.split(",");
						String portion1 = portionArr[0];
						sb = sb.replace(portion1, getLinkForVersesList(portion1, OUTPUT_FORMAT));
						String derivedPortion = null;
						if (portion1.startsWith("1 ") || portion1.startsWith("2 ") || portion1.startsWith("3 ")) {
							String[] tempArr = portion1.split(" ");
							// String[] chapterAndVersesArray = tempArr[2].split(":");// 1 நாளா.3:16-17
							// derivedPortion = tempArr[0] + " " + tempArr[1] + " " +
							// chapterAndVersesArray[0] + ":"
							// + portionArr[1].trim();
							String BOOK_NAME;
							String CHAPTER_NUMBER;
							String[] chapterAndVersesArray;
							if (tempArr.length > 2) {// 2 இரா. 8:26
								chapterAndVersesArray = tempArr[2].split(":");
								BOOK_NAME = tempArr[0] + " " + tempArr[1];
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							} else if (tempArr[1].contains(".")) {// 2 நாளா.22:3
								String[] tempArrBookNme = tempArr[1].split("\\.");
								chapterAndVersesArray = tempArrBookNme[1].split(":");
								BOOK_NAME = tempArr[0] + " " + tempArrBookNme[0];
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							} else {// 2 நாளா22:3
								String tempBookName = "";
								for (int i = 0; i < tempArr[1].length(); i++) {
									char character = tempArr[1].charAt(i);
									if (!Character.isDigit(character)) {
										tempBookName = tempBookName + character;
									}
								}
								BOOK_NAME = tempArr[0] + " " + tempBookName;
								chapterAndVersesArray = tempArr[1].replace(tempBookName, "").split(":");
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							}
							derivedPortion = BOOK_NAME + " " + CHAPTER_NUMBER + ":" + chapterAndVersesArray[1];
							System.out.println(OUTPUT_FORMAT + " : New Bible portion derived: " + derivedPortion);
							sb = sb.replace(portion, getLinkForVersesRange(derivedPortion, OUTPUT_FORMAT));
						} else if (!portionArr[1].contains(":")) {// ஆதி.3:4-6, 23 OR எரே. 43:7, 11-13
							String BOOK_NAME;
							String CHAPTER_NUMBER;
							String[] chapterAndVersesArray;
							if (portion1.contains(".")) {
								String[] tempArrBookNme = portion1.split("\\.");
								BOOK_NAME = tempArrBookNme[0];
								chapterAndVersesArray = tempArrBookNme[1].split(":");
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							} else if (portion1.contains(" ")) {
								String[] tempArrBookNme = portion1.split(" ");
								BOOK_NAME = tempArrBookNme[0];
								chapterAndVersesArray = tempArrBookNme[1].split(":");
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							} else {
								String tempBookName = "";
								for (int i = 0; i < portion1.length(); i++) {
									char character = portion1.charAt(i);
									if (!Character.isDigit(character)) {
										tempBookName = tempBookName + character;
									}
								}
								BOOK_NAME = tempBookName;
								chapterAndVersesArray = portion1.replace(tempBookName, "").split(":");
								CHAPTER_NUMBER = chapterAndVersesArray[0];
							}
							derivedPortion = BOOK_NAME + " " + CHAPTER_NUMBER.trim() + ":" + portionArr[1].trim();
							System.out.println(OUTPUT_FORMAT + " : New Bible portion derived: " + derivedPortion);
							sb = sb.replace(portionArr[1], getLinkForVersesRange(derivedPortion, OUTPUT_FORMAT));
						} else {
							String[] tempArr = portion1.split(" ");
							String[] chapterAndVersesArray = tempArr[1].split(":");
							derivedPortion = tempArr[0] + " " + chapterAndVersesArray[0] + ":" + portionArr[1].trim();
							System.out.println(OUTPUT_FORMAT + " : New Bible portion derived: " + derivedPortion);
							sb = sb.replace(portion, getLinkForVersesRange(derivedPortion, OUTPUT_FORMAT));
						}
					} else {
						sb = sb.replace(portion, getLinkForVersesList(portion, OUTPUT_FORMAT));
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				//TODO display these logs to now the various use cases where the program is not able to apply links
				//e.printStackTrace();
				//System.out.println(
					//	OUTPUT_FORMAT + " : Unable to create reference link for the bible Portion: " + portion);
			}
		}
		System.out.println(OUTPUT_FORMAT + " : Final Data: " + sb);
		System.out.println(OUTPUT_FORMAT + " : Applying Links To References completed...");
		return sb;
	}

	private static String getLinkForChapter(String portion, String BOOK_NAME, String CHAPTER_NUMBER,
			String outputFormat) {
		String VERSE_NUMBER = "1";
		String REFERENCE_LINK_FORMAT_1 = "";

		String link = "";
		if (ZefaniaXML.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			REFERENCE_LINK_FORMAT_1 = ZefaniaXML.REFERENCE_LINK_FORMAT_1;
		} else if (TheWord.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			REFERENCE_LINK_FORMAT_1 = TheWord.REFERENCE_LINK_FORMAT_1;
		} else if (MyBibleZone.OUTPUT_FORMAT.equalsIgnoreCase(outputFormat)) {
			REFERENCE_LINK_FORMAT_1 = MyBibleZone.REFERENCE_LINK_FORMAT_1;
		}

		String temp = getBookIdentifier(BOOK_NAME, outputFormat);
		if (temp != null) {
			link = REFERENCE_LINK_FORMAT_1.replace("BOOK_NUMBER", temp).replace("CHAPTER_NUMBER", CHAPTER_NUMBER)
					.replace("VERSE_NUMBER", VERSE_NUMBER).replace("BIBLE_PORTION", portion);
		}

		System.out.println(outputFormat + " : Generated Reference link: " + link);
		return link;

	}

	static String deleteOutputFileIfAlreadyExists(String extension) {
		String outfile = BibleDictionaryCreator.folderPath.replace(BibleDictionaryCreator.outputFile, "")
				+ BibleDictionaryCreator.outputFile + extension;
		new File(outfile).delete();
		return outfile;
	}

}
