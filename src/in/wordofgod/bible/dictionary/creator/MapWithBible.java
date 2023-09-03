/**
 * 
 */
package in.wordofgod.bible.dictionary.creator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 */
public class MapWithBible {

	private static SortedSet<String> dictionaryWords = new TreeSet<String>();
	// Without vowel, to match the words in the bible as startsWith
	// For any matches, need to build the mapping into dictionaryWordsMap
	private static Map<String, String> allWordsWithoutVowelMap = new TreeMap<String, String>();
	// With vowel, to match the words in the bible as equals/exact match
	private static Map<String, String> allWordsVowelMap = new TreeMap<String, String>();
	// Final map to be used for dictionary creation
	public static Map<String, List<String>> dictionaryWordsMap = new TreeMap<String, List<String>>();

	public static void buildMap() {
		System.out.println("Mapping with the Bible Text is started");

		// dictionary words index
		buildWordsIndex();

		// build the mapping | key = actual dictionary word; value = possible list of
		// words including vowels or words with same/similar meaning
		buildWordsMapIndex();

		System.out.println("Mapping with the Bible Text is completed");
	}

	private static void buildWordsMapIndex() {
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
		System.out.println("dictionaryWordsMap :: " + dictionaryWordsMap);
		System.out.println("Total Words by actual dictionary words in the mapping: " + dictionaryWordsMap.size());
		System.out.println("Building words map is completed");
	}

	private static void buildWordsForTamil() {
		System.out.println("Identified language as tamil");
		System.out.println("Building words map with additional vowels for tamil language is completed");
		String[] vowelArr = { "்", "ா", "ி", "ீ", "ு", "ூ", "ெ", "ே", "ை", "ொ", "ோ", "ௌ" };
		for (String word : dictionaryWords) {
			String shorternedWord = word;
			if (word.endsWith("்")) {
				shorternedWord = removeLast(word, "்");
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

	private static void addToVowelMap(String key, String value) {
		allWordsVowelMap.put(key, value);
		if (!dictionaryWordsMap.containsKey(value)) {
			dictionaryWordsMap.put(value, new ArrayList<String>());
		}
		dictionaryWordsMap.get(value).add(key);
	}

	private static void addToMap(String key, String value) {
		allWordsWithoutVowelMap.put(key, value);
		if (!dictionaryWordsMap.containsKey(value)) {
			dictionaryWordsMap.put(value, new ArrayList<String>());
		}
		dictionaryWordsMap.get(value).add(key);
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
			if (BibleDictionaryCreator.INFORMATION_FILE_NAME.equalsIgnoreCase(file.getName())) {
				continue;
			}
			//System.out.println("Reading the file: " + file.getName());

			temp = file.getName().strip();
			if(temp.split(" ").length>1) {
				System.out.println("Two Words found - file name should be single word : " + temp);
			}
			dictionaryWords.add(temp.substring(0, temp.lastIndexOf(".")));
		}
		System.out.println(dictionaryWords);
		System.out.println("Total Unique Words: " + dictionaryWords.size());
		System.out.println("Building words index is completed");
	}

}
