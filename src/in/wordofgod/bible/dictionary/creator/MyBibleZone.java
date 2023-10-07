/**
 * 
 */
package in.wordofgod.bible.dictionary.creator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 * 
 */
public class MyBibleZone {

	public static final String OUTPUT_FORMAT = "MyBibleZone";
	static final String EXTENSION = ".dictionary.SQLite3";

	// <a href='S:Alpha'>ALPHA</a>
	public static String WORD_LINK_FORMAT = "<a href='S:DICTIONARY_WORD'>DICTIONARY_WORD</a>";
	// <a href='B:120 19:37'>2Kings 19:37</a> OR <a href='B:490 14:11'>Luk 14:11</a>
	public static String REFERENCE_LINK_FORMAT_1 = "<a href='B:BOOK_NUMBER CHAPTER_NUMBER:VERSE_NUMBER'>BIBLE_PORTION</a>";
	// <a href='B:10 1:8-4'>Gen 1:8-4</a>
	public static String REFERENCE_LINK_FORMAT_2 = "<a href='B:BOOK_NUMBER CHAPTER_NUMBER:STARTING_VERSE_NUMBER-ENDING_VERSE_NUMBER'>BIBLE_PORTION</a>";

	public static void build() {
		System.out.println("MyBible Bible Dictionary Creation started...");

		String outfile = Utils.deleteOutputFileIfAlreadyExists(EXTENSION);

		SqlJetDb db;
		try {
			db = openDB(new File(outfile), true);
			configureDB(db);
			createTablesInDB(db);

			buildInfoTable(db);
			buildWordsTable(db);
			buildDictionaryTable(db);

			db.commit();
			db.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (SqlJetException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("MyBible Bible Dictionary Creation completed...");
		System.out.println("MyBibleZone : Result is stored in: " + new File(outfile).getAbsolutePath());
	}

	private static void buildInfoTable(SqlJetDb db) throws FileNotFoundException, IOException, SqlJetException {
		System.out.println("MyBibleZone : Info Table Creation started...");
		Map<String, String> infoValues = new LinkedHashMap<>();

		infoValues.put("language", BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_LANGUAGE));
		infoValues.put("description", BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_DESCRIPTION));
		infoValues.put("detailed_info",
				BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_DESCRIPTION));
		infoValues.put("russian_numbering", "false");
		infoValues.put("is_strong", "false");
		infoValues.put("is_word_forms", "false");
		infoValues.put("morphology_topic_reference", "");

		ISqlJetTable infoTable = db.getTable("info");
		for (Map.Entry<String, String> entry : infoValues.entrySet()) {
			infoTable.insert(entry.getKey(), entry.getValue());
		}
		System.out.println("MyBibleZone : Info Table Creation completed...");
	}

	private static void buildWordsTable(SqlJetDb db) throws FileNotFoundException, IOException, SqlJetException {
		System.out.println("MyBibleZone : Words Table Creation started...");
		int count = 0;
		ISqlJetTable wordsTable = db.getTable("words");
		for (String dictionaryWord : MapWithBible.dictionaryWordsVsBibleWordsMap.keySet()) {
			for (String mappingWord : MapWithBible.dictionaryWordsVsBibleWordsMap.get(dictionaryWord)) {
				// Ignore all the mapping words which are already found as dictionary words
				if (!MapWithBible.uniqueDictionaryWords.containsKey(mappingWord)) {
					wordsTable.insert(mappingWord, dictionaryWord);
					count++;
				}
			}
		}
		System.out.println("MyBibleZone : Words Table Creation completed...");
		System.out.println("MyBibleZone : Total Mapping Words created is: " + count);
	}

	private static void configureDB(SqlJetDb db) throws SqlJetException {
		db.getOptions().setAutovacuum(true);
		db.beginTransaction(SqlJetTransactionMode.WRITE);
		db.getOptions().setUserVersion(0);
	}

	private static void createTablesInDB(SqlJetDb db) throws SqlJetException {
		db.createTable("CREATE TABLE info (name TEXT, value TEXT)");
		db.createTable("CREATE TABLE dictionary (topic TEXT, definition TEXT)");
		db.createIndex("CREATE UNIQUE INDEX dictionary_topic ON dictionary(topic ASC)");
		db.createTable(
				"CREATE TABLE words(variation TEXT, standard_form TEXT, book_number NUMERIC NOT NULL DEFAULT 0, chapter_number NUMERIC NOT NULL DEFAULT 0, verse_number NUMERIC NOT NULL DEFAULT 0, PRIMARY KEY (standard_form, variation, book_number, chapter_number, verse_number))");
	}

	public static SqlJetDb openDB(File file, boolean write) throws SqlJetException {
		try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
			byte[] header = new byte[20];
			in.readFully(header);
			if (new String(header, 0, 16, StandardCharsets.ISO_8859_1).equals("SQLite format 3\0")
					&& (header[18] > 1 || header[19] > 1)) {
				System.err.println("WARNING: SQLite version of " + file.getName() + " is too new.");
				System.err.println(
						"To convert SQLite file to version 1, open it in a SQLite editor and run SQL 'PRAGMA journal_mode=DELETE;'.");
				System.err.println();
			}
		} catch (IOException ex) {
			// ex.printStackTrace();
			// Ignore this exception as the file will get created newly
		}
		return SqlJetDb.open(file, write);
	}

	public static void buildDictionaryTable(SqlJetDb db) throws SqlJetException {
		System.out.println("MyBibleZone : Dictionary Table Creation started...");
		System.out.println("MyBibleZone : Reading the files/words from the folder " + BibleDictionaryCreator.folderPath);
		ISqlJetTable dictionaryTable = db.getTable("dictionary");
		String strItemID = null;
		File folder = new File(BibleDictionaryCreator.folderPath);
		int count = 0;
		for (File file : folder.listFiles()) {
			if (Utils.checkForInValidFile(file)) {
				continue;
			}
			System.out.println("MyBibleZone : Reading the file: " + file.getName());

			strItemID = Utils.trimDictionaryWord(file.getName().substring(0, file.getName().lastIndexOf(".")));
			if (MapWithBible.dictionaryWordsVsBibleWordsMap.get(strItemID) == null) {
				System.out.println("MyBibleZone : Word not found: " + strItemID);
				continue;
			}

			// for (String word :
			// MapWithBible.dictionaryWordsVsBibleWordsMap.get(strItemID)) {
			// dictionaryTable.insert(word, buildDescriptionFromFile(file));
			// count++;
			// }
			dictionaryTable.insert(strItemID, buildDescriptionFromFile(file));
			count++;
		}
		System.out.println("MyBibleZone : Dictionary Table Creation completed...");
		System.out.println("MyBibleZone : Total Dictionary Words created is: " + count);
	}

	private static String buildDescriptionFromFile(File file) {
		BufferedReader reader;
		String sb = "";
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
			reader = new BufferedReader(isr);
			String line = reader.readLine();
			while (line != null) {
				line = line.strip();
				if (!line.equals("")) {
					if (line.contains("[H1]")) {
						sb = sb + buildH1Description(line);
					} else if (line.contains("[H2]")) {
						sb = sb + buildH2Description(line);
					} else if (line.contains("[H3]")) {
						sb = sb + buildH3Description(line);
					} else {
						sb = sb + buildDescription(line);
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sb = Utils.applyLinksToReferences(sb, OUTPUT_FORMAT);
		return sb;
	}

	public static String buildDescription(String line) {
		line = line + "<p/>";
		return line;
	}

	public static String buildH3Description(String line) {
		// Remove the tag [H3]
		line = line.replaceAll("\\[H3\\]", "").strip();

		line = "<b>" + line + "</b><p/>";
		return line;
	}

	public static String buildH2Description(String line) {
		// Remove the tag [H2]
		line = line.replaceAll("\\[H2\\]", "").strip();

		line = "<b>" + line + "</b><p/>";
		return line;
	}

	public static String buildH1Description(String line) {
		// Remove prefix text like 0001 used for identifying unique no of words
		line = line.replace(line.substring(0, line.indexOf("[H1]")), "");
		// Remove the tag [H1]
		line = line.replaceAll("\\[H1\\]", "").strip();

		line = "<b><el>" + line + "</el></b><p/>";

		return line;
	}
}
