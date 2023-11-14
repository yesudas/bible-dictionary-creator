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
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

/**
 * 
 */
public class MapWithBible {

	private static String[] tamilVowelArrAll = { "்", "ா", "ி", "ீ", "ு", "ூ", "ெ", "ே", "ை", "ொ", "ோ", "ௌ" };
	private static String[] tamilVowelArr = { "ா", "ி", "ீ", "ு", "ூ", "ெ", "ே", "ை", "ொ", "ோ", "ௌ" };

	// Total dictionary words given as input
	private static SortedSet<String> dictionaryWords = new TreeSet<String>();
	// Dictionary words + additional words by adding vowels (for languages like
	// tamil)
	// key = dictionary word; value = list of words with vowels
	public static Map<String, List<String>> dictionaryWordsVsVowelsMap = new TreeMap<String, List<String>>();
	// Dictionary words + additional words by adding vowels (for languages like
	// tamil)
	// Key = vowels; Value = Dictionary word, used for easy search for matching
	// bible words
	public static Map<String, String> uniqueDictionaryWords = new TreeMap<String, String>(Collections.reverseOrder());
	// All unique words from bible
	private static SortedSet<String> bibleWords = new TreeSet<String>(Collections.reverseOrder());
	// key = bible word; value = dictionary word
	public static Map<String, String> bibleWordsVsDictionaryWordsMap = new TreeMap<String, String>();
	// key = dictionary word; value = list of matching bible words
	public static Map<String, List<String>> dictionaryWordsVsBibleWordsMap = new TreeMap<String, List<String>>();

	// Without vowel, to match the words in the bible as startsWith
	// For any matches, need to build the mapping into dictionaryWordsMap
	// private static Map<String, String> allWordsWithoutVowelMap = new
	// TreeMap<String, String>();
	// With vowel, to match the words in the bible as equals/exact match
	// private static Map<String, String> allWordsVowelMap = new TreeMap<String,
	// String>();

	private static int totalWordsIncludingVowels = 0;

	public static void buildMap() {
		System.out.println("Mapping with the Bible Text is started");

		// dictionary words index
		buildWordsIndex();

		// build the mapping | key = actual dictionary word; value = possible list of
		// words including vowels or words with same/similar meaning
		buildWordsMap();

		buildWordsMapFromMappingFile();

		System.out.println("Mapping with the Bible Text is completed");
	}

	private static void buildWordsMapFromMappingFile() {
		System.out.println("Building Words Map From " + BibleDictionaryCreator.MAPPING_FILE_NAME + " File is started");
		Properties properties = loadMappingFile(
				BibleDictionaryCreator.folderPath + "\\" + BibleDictionaryCreator.MAPPING_FILE_NAME);

		if (!BibleDictionaryCreator.mapRelatedWordsAutomatically) {
			properties.forEach((k, v) -> {
				if (!dictionaryWordsVsBibleWordsMap.containsKey(v)) {
					dictionaryWordsVsBibleWordsMap.put(v.toString(), new ArrayList<String>());
					dictionaryWordsVsBibleWordsMap.get(v.toString()).add(k.toString());
				} else {
					dictionaryWordsVsBibleWordsMap.get(v.toString()).add(k.toString());
				}
			});
			return;
		}

		switch (BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_LANGUAGE)) {
		case "ta": {
			buildWordsMapFromMappingFileForTamil(properties);
			break;
		}
		default:
			properties.forEach((k, v) -> {
				System.out.println("Addinal mappings : " + k.toString() + " = " + v.toString());
				addToMap(k.toString(), v.toString());
			});
		}
		System.out.println(
				"Including Mapping File: Total Words by Including Vowels in the mapping: " + totalWordsIncludingVowels);
		System.out.println("Including Mapping File: Total Words by actual dictionary words in the mapping: "
				+ dictionaryWordsVsVowelsMap.size());
		System.out.println("dictionaryWordsVsVowelsMap :: " + "\n" + dictionaryWordsVsVowelsMap);
		System.out.println(
				"Including Mapping File: Total Words by uniqueDictionaryWords: " + uniqueDictionaryWords.size());
		System.out.println("uniqueDictionaryWords :: " + "\n" + uniqueDictionaryWords);
		System.out
				.println("Building Words Map From " + BibleDictionaryCreator.MAPPING_FILE_NAME + " File is completed");
	}

	private static void buildWordsMapFromMappingFileForTamil(Properties properties) {
		System.out.println("Identified language as tamil");
		properties.forEach((k, v) -> {
			System.out.println("Addinal mappings : " + k.toString() + " = " + v.toString());
			applyLinguisticRulesForTamil(k.toString(), v.toString());
		});
	}

	private static void buildWordsMap() {
		System.out.println("Building words map is started");
		switch (BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_LANGUAGE)) {
		case "ta": {
			buildWordsForTamil();
			break;
		}
		default:
			for (String word : dictionaryWords) {
				addToMap(word, word);
			}
		}
		// System.out.println("allWordsWithoutVowelMap :: " + allWordsWithoutVowelMap);
		// System.out.println("Total Words without vowels based in the mapping: " +
		// allWordsWithoutVowelMap.size());
		// System.out.println("allWordsVowelMap :: " + allWordsVowelMap);
		// System.out.println("Total Words including vowels based in the mapping: " +
		// allWordsVowelMap.size());
		System.out
				.println("Total Words by actual dictionary words in the mapping: " + dictionaryWordsVsVowelsMap.size());
		System.out.println("Total Words by uniqueDictionaryWords: " + uniqueDictionaryWords.size());
		System.out.println("Total Words by Including Vowels in the mapping: " + totalWordsIncludingVowels);
		System.out.println("Building words map is completed");
	}

	private static void buildWordsForTamil() {
		System.out.println("Identified language as tamil");
		System.out.println("Building words map with additional vowels for tamil language is started");
		for (String dictionaryWord : dictionaryWords) {
			applyLinguisticRulesForTamil(dictionaryWord);
		}
		System.out.println("Building words map with additional vowels for tamil language is completed");
	}

	private static void applyLinguisticRulesForTamil(String dictionaryWord) {
		// த் - சேத், யாரேத், யாப்பேத்; ஆ - அகாயா, மெத்தூசலா, நோவா
		if (dictionaryWord.endsWith("த்") || dictionaryWord.endsWith("ா")) {
			addToMap(dictionaryWord, dictionaryWord);
			return;
		}
		for (String vowel : tamilVowelArrAll) {
			if (dictionaryWord.endsWith(vowel)) {
				String shorternedWord = removeLast(dictionaryWord, vowel);
				// addToMap(shorternedWord, dictionaryWord);Dont consider அ vowel
				for (String vowel1 : tamilVowelArr) {
					if (!uniqueDictionaryWords.containsKey(shorternedWord + vowel1)) {// அபியா, அபியு -> அபியா
						addToMap(shorternedWord + vowel1, dictionaryWord);
					}
				}
				break;
			}
		}
		// Add the word itself
		addToMap(dictionaryWord, dictionaryWord);
		return;
	}

	private static void applyLinguisticRulesForTamil(String additionalWord, String dictionaryWord) {
		// த் - சேத், யாரேத், யாப்பேத்; ஆ - அகாயா, மெத்தூசலா, நோவா
		if (additionalWord.endsWith("த்") || additionalWord.endsWith("ா")) {
			addToMap(additionalWord, dictionaryWord);
			return;
		}
		for (String vowel : tamilVowelArrAll) {
			if (additionalWord.endsWith(vowel)) {
				String shorternedWord = removeLast(additionalWord, vowel);
				// addToMap(shorternedWord, dictionaryWord);Dont consider அ vowel
				for (String vowel1 : tamilVowelArr) {
					addToMap(shorternedWord + vowel1, dictionaryWord);
				}
				break;
			}
		}
		// Add the word itself
		addToMap(additionalWord, dictionaryWord);
		return;
	}

	private static void addToVowelMap(String key, String dictionaryWord) {
		// allWordsVowelMap.put(key, dictionaryWord);
		if (!dictionaryWordsVsVowelsMap.containsKey(dictionaryWord)) {
			dictionaryWordsVsVowelsMap.put(dictionaryWord, new ArrayList<String>());
		}
		dictionaryWordsVsVowelsMap.get(dictionaryWord).add(key);
		uniqueDictionaryWords.put(key, dictionaryWord);
		totalWordsIncludingVowels++;
	}

	private static void addToMap(String key, String dictionaryWord) {
		// allWordsWithoutVowelMap.put(key, value);
		if (!dictionaryWordsVsVowelsMap.containsKey(dictionaryWord)) {
			dictionaryWordsVsVowelsMap.put(dictionaryWord, new ArrayList<String>());
		}
		dictionaryWordsVsVowelsMap.get(dictionaryWord).add(key);
		uniqueDictionaryWords.put(key, dictionaryWord);
		totalWordsIncludingVowels++;
	}

	/**
	 * Replace last occurrence of given sub string
	 *
	 * @param s
	 * @param search
	 * @return
	 */
	public static String removeLast(String s, String search) {
		int pos = s.lastIndexOf(search);

		if (pos > -1) {
			return s.substring(0, pos) + s.substring(pos + search.length(), s.length());
		}

		return s;
	}

	public static void buildWordsIndex() {
		System.out.println("Building words index is started");
		System.out.println("Reading the files/words from the folder " + BibleDictionaryCreator.folderPath);

		File folder = new File(BibleDictionaryCreator.folderPath);
		String temp = null;
		for (File file : folder.listFiles()) {
			if (Utils.checkForInValidFile(file)) {
				continue;
			}
			// System.out.println("Reading the file: " + file.getName());

			temp = Utils.trimDictionaryWord(file.getName());
			if (temp.split(" ").length > 1) {
				System.out.println("Two Words found - file name should be single word : " + temp);
			}
			temp = temp.substring(0, temp.lastIndexOf("."));
			dictionaryWords.add(Utils.trimDictionaryWord(temp));
		}
		System.out.println(dictionaryWords);
		System.out.println("Total Unique Dictionary Words: " + dictionaryWords.size());
		System.out.println("Building words index is completed");
	}

	public static void buildMapWithBible() throws IOException {
		System.out.println("Building Map With Bible is started");
		for (String dictionaryWord : dictionaryWords) {
			if (!uniqueDictionaryWords.containsValue(dictionaryWord)) {
				System.out.println("Dictionary word not found: " + dictionaryWord
						+ " in uniqueDictionaryWords, verify MAPPING.txt, added automatically for now");
				uniqueDictionaryWords.put(dictionaryWord, dictionaryWord);
			}
		}

		if (BibleDictionaryCreator.mapRelatedWordsAutomatically) {
			String bibleSourceDirectory = BibleDictionaryCreator.DICTIONARY_DETAILS
					.getProperty(Constants.STR_BIBLE_SOURCE_DIRECTORY);
			String bibleVersions = BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_BIBLE_VERSIONS);
			buildUniqueBibleWords(bibleSourceDirectory, bibleVersions);

			System.out.println("Looping through uniqueDictionaryWords & bibleWords to build bibleWordsVsDictionaryWordsMap");
			System.out.println("Please wait, it may take several minutes based on number of bibleVersions given in INFORMATION.txt");
			uniqueDictionaryWords.forEach((k, v) -> {
				for (String bibleWord : bibleWords) {
					if (bibleWordsVsDictionaryWordsMap.containsKey(bibleWord)) {
						continue;
					}
					if (bibleWord.startsWith(k)) {
						bibleWordsVsDictionaryWordsMap.put(bibleWord, v);
					}
				}
			});
		}

		bibleWordsVsDictionaryWordsMap.forEach((bibleWord, dictionaryWord) -> {
			if (!dictionaryWordsVsBibleWordsMap.containsKey(dictionaryWord.toString())) {
				dictionaryWordsVsBibleWordsMap.put(dictionaryWord.toString(), new ArrayList<String>());
			}
			dictionaryWordsVsBibleWordsMap.get(dictionaryWord.toString()).add(bibleWord.toString());
		});

		for (String dictionaryWord : dictionaryWords) {
			if (!dictionaryWordsVsBibleWordsMap.containsKey(dictionaryWord)) {
				System.out.println("Dictionary word not found: " + dictionaryWord
						+ " in dictionaryWordsVsBibleWordsMap, verify MAPPING.txt, for now added automatically");
				dictionaryWordsVsBibleWordsMap.put(dictionaryWord, new ArrayList<String>());
				dictionaryWordsVsBibleWordsMap.get(dictionaryWord).add(dictionaryWord);
			}
		}

		System.out.println("Total bibleWordsVsDictionaryWordsMap: " + bibleWordsVsDictionaryWordsMap.size());
		System.out.println("Total dictionaryWordsVsBibleWordsMap: " + dictionaryWordsVsBibleWordsMap.size());
		System.out.println("Total bibleWords is " + bibleWordsVsDictionaryWordsMap.size() + "; "
				+ " Total Matching dictionaryWords is " + dictionaryWordsVsBibleWordsMap.size());
		System.out.println("dictionaryWordsVsBibleWordsMap:\n" + dictionaryWordsVsBibleWordsMap);
		System.out.println("Building Map With Bible is completed");
	}

	private static void buildUniqueBibleWords(String bibleSourceDirectory, String bibleVersions) throws IOException {
		System.out.println("Building Unique Bible Words index is started");
		File dir = new File(bibleSourceDirectory);
		String[] versions = bibleVersions.split(",");
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".ont") || file.getName().endsWith(".ot") || file.getName().endsWith(".nt")) {
				for (String version : versions) {
					int wordsCount = 0;
					if (file.getName().startsWith(version)) {
						System.out.println("Started building unique bible words index for the version: " + version);
						Bible bible = TheWord.getBible(file.getAbsolutePath(),
								file.getParentFile().getAbsolutePath() + "\\" + version + "-information.ini");
						for (Book book : bible.getBooks()) {
							for (Chapter chapter : book.getChapters()) {
								for (Verse verse : chapter.getVerses()) {
									String verseText = removeHTMLTags(verse.getText());
									String[] words = verseText.split("[\\s']");
									for (String word : words) {
										word = word.replaceAll("[\\\"\\(\\)\\-\\.\\:\\,\\;]", "");
										word = word.trim().strip();
										if (!word.equals("")) {
											bibleWords.add(word);
											wordsCount++;
										}
									}
								}
							}
						}
						System.out.println("Total Words (wordsCount) in the Bible " + version + " is: " + wordsCount);
					}
				}
			}
		}
		System.out.println("Building Unique Bible Words index is completed");
		System.out.println("Total Unique Words in " + bibleVersions + " is: " + bibleWords.size());
	}

	private static String removeHTMLTags(String text) {
		while (text.contains("<")) {
			int startPos = text.indexOf("<");
			int endPos = text.indexOf(">");
			String htmlTag = text.substring(startPos, endPos + 1);
			text = text.replace(htmlTag, "");
		}
		return text;
	}

	private static Properties loadMappingFile(String mappingFilePath) {
		Properties bookNames = null;

		BufferedReader propertyReader;
		try {
			propertyReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File(mappingFilePath)), "UTF8"));
			bookNames = new Properties();
			bookNames.load(propertyReader);
			propertyReader.close();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bookNames == null) {
				bookNames = new Properties();
			}
		}
		return bookNames;
	}

	public static void createMappingFileForReview() {
		int dictionaryWordCount = 0;
		int mappingWordCount = 0;
		StringBuilder sb = new StringBuilder();
		for (String dictionaryWord : dictionaryWordsVsBibleWordsMap.keySet()) {
			dictionaryWordCount++;
			for (String mappingWord : dictionaryWordsVsBibleWordsMap.get(dictionaryWord)) {
				// Ignore all the mapping words which are already found as dictionary words
				if (!MapWithBible.uniqueDictionaryWords.containsKey(mappingWord)) {
					mappingWordCount++;
					sb.append(mappingWord).append("=").append(dictionaryWord).append("\n");
				}
			}
		}
		String filePath = BibleDictionaryCreator.folderPath + "\\"
				+ BibleDictionaryCreator.MAPPING_FILE_NAME_FOR_REVIEW;
		createFile(filePath, sb.toString());
		System.out.println(
				"Mappings have been generated. Please review manually and then continue using this program with the option mapRelatedWordsAutomatically=no");
		System.out.println(
				"After your review, rename this file to MAPPING.txt or move this file's content to MAPPING.txt file");
		System.out.println("Total Dictionary Words: " + dictionaryWordCount);
		System.out.println("Total Related Mapping Words: " + mappingWordCount);
		System.out.println("Mappings have been stored in the file for your review at: " + filePath);
	}

	private static void createFile(String filePath, String text) {
		try {
			Files.writeString(Path.of(filePath), text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
