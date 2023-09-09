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
import java.util.ArrayList;
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

	private static String[] vowelArr = { "்", "ா", "ி", "ீ", "ு", "ூ", "ெ", "ே", "ை", "ொ", "ோ", "ௌ" };

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
	public static Map<String, String> uniqueDictionaryWords = new TreeMap<String, String>();
	// All unique words from bible
	private static SortedSet<String> bibleWords = new TreeSet<String>();
	// key = bible word; value = dictionary word
	public static Map<String, String> bibleWordsVsDictionaryWordsMap = new TreeMap<String, String>();

	// Without vowel, to match the words in the bible as startsWith
	// For any matches, need to build the mapping into dictionaryWordsMap
	private static Map<String, String> allWordsWithoutVowelMap = new TreeMap<String, String>();
	// With vowel, to match the words in the bible as equals/exact match
	private static Map<String, String> allWordsVowelMap = new TreeMap<String, String>();

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
		properties.forEach((k, v) -> {
			System.out.println("Addinal mappings : " + k.toString() + " = " + v.toString());
			String word = k.toString();
			String shorternedWord = word;
			String endsWith = endsWith(word);
			if (endsWith != null) {
				shorternedWord = removeLast(word, endsWith);
				if (!dictionaryWordsVsVowelsMap.containsKey(v.toString())) {
					dictionaryWordsVsVowelsMap.put(v.toString(), new ArrayList<String>());
				}
				dictionaryWordsVsVowelsMap.get(v.toString()).add(k.toString());
				uniqueDictionaryWords.put(k.toString(), v.toString());
				totalWordsIncludingVowels++;
				for (String vowel : vowelArr) {
					dictionaryWordsVsVowelsMap.get(v.toString()).add(shorternedWord + vowel);
				}
				dictionaryWordsVsVowelsMap.get(v.toString()).add(word);
				uniqueDictionaryWords.put(word, v.toString());
				totalWordsIncludingVowels++;
			}
		});
		System.out
				.println("Total Words by actual dictionary words in the mapping: " + dictionaryWordsVsVowelsMap.size());
		System.out.println("Total Words by uniqueDictionaryWords: " + uniqueDictionaryWords.size());
		System.out.println("Total Words by Including Vowels in the mapping: " + totalWordsIncludingVowels);
		System.out
				.println("Building Words Map From " + BibleDictionaryCreator.MAPPING_FILE_NAME + " File is completed");
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
		System.out.println("allWordsWithoutVowelMap :: " + allWordsWithoutVowelMap);
		System.out.println("Total Words without vowels based in the mapping: " + allWordsWithoutVowelMap.size());
		System.out.println("allWordsVowelMap :: " + allWordsVowelMap);
		System.out.println("Total Words including vowels based in the mapping: " + allWordsVowelMap.size());
		System.out.println("dictionaryWordsMap :: " + dictionaryWordsVsVowelsMap);
		System.out
				.println("Total Words by actual dictionary words in the mapping: " + dictionaryWordsVsVowelsMap.size());
		System.out.println("Total Words by uniqueDictionaryWords: " + uniqueDictionaryWords.size());
		System.out.println("Total Words by Including Vowels in the mapping: " + totalWordsIncludingVowels);
		System.out.println("Building words map is completed");
	}

	private static void buildWordsForTamil() {
		System.out.println("Identified language as tamil");
		System.out.println("Building words map with additional vowels for tamil language is completed");
		for (String word : dictionaryWords) {
			String shorternedWord = word;
			String endsWith = endsWith(word);
			if (endsWith != null) {
				shorternedWord = removeLast(word, endsWith);
				addToVowelMap(shorternedWord, word);
				for (String vowel : vowelArr) {
					addToVowelMap(shorternedWord + vowel, word);
				}
				addToVowelMap(word, word);
			} else {
				addToMap(word, word);
			}
		}
	}

	private static String endsWith(String word) {
		for (String vowel : vowelArr) {
			if (word.endsWith(vowel)) {
				return vowel;
			}
		}
		return null;
	}

	private static void addToVowelMap(String key, String value) {
		allWordsVowelMap.put(key, value);
		if (!dictionaryWordsVsVowelsMap.containsKey(value)) {
			dictionaryWordsVsVowelsMap.put(value, new ArrayList<String>());
		}
		dictionaryWordsVsVowelsMap.get(value).add(key);
		uniqueDictionaryWords.put(key, value);
		totalWordsIncludingVowels++;
	}

	private static void addToMap(String key, String value) {
		allWordsWithoutVowelMap.put(key, value);
		if (!dictionaryWordsVsVowelsMap.containsKey(value)) {
			dictionaryWordsVsVowelsMap.put(value, new ArrayList<String>());
		}
		dictionaryWordsVsVowelsMap.get(value).add(key);
		uniqueDictionaryWords.put(key, value);
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
			if (BibleDictionaryCreator.INFORMATION_FILE_NAME.equalsIgnoreCase(file.getName())
					|| BibleDictionaryCreator.MAPPING_FILE_NAME.equalsIgnoreCase(file.getName())) {
				continue;
			}
			// System.out.println("Reading the file: " + file.getName());

			temp = file.getName().trim().strip();
			if (temp.split(" ").length > 1) {
				System.out.println("Two Words found - file name should be single word : " + temp);
			}
			temp = temp.substring(0, temp.lastIndexOf("."));
			dictionaryWords.add(temp.trim().strip());
		}
		System.out.println(dictionaryWords);
		System.out.println("Total Unique Words: " + dictionaryWords.size());
		System.out.println("Building words index is completed");
	}

	public static void buildMapWithBible(String bibleSourceDirectory, String bibleVersions) {
		System.out.println("Building Map With Bible is started");
		buildUniqueBibleWords(bibleSourceDirectory, bibleVersions);

		for (String bibleWord : bibleWords) {
			uniqueDictionaryWords.forEach((k, v) -> {
				if (bibleWord.startsWith(k)) {
					bibleWordsVsDictionaryWordsMap.put(bibleWord, k);
				}
			});
		}
		System.out.println("Total bibleWordsVsDictionaryWordsMap: " + bibleWordsVsDictionaryWordsMap.size());
		System.out.println("Building Map With Bible is completed");
	}

	private static void buildUniqueBibleWords(String bibleSourceDirectory, String bibleVersions) {
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
									String[] words = verse.getText().split("[\\s']");
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

}
