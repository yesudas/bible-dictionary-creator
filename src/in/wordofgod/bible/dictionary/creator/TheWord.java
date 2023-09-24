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

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 * 
 */
public class TheWord {

	private static final String EXTENSION = ".dct.twm";

	public static void build() {
		System.out.println("TheWord Bible Dictionary Creation started...");

		String outfile = deleteOutputFileIfAlreadyExists();

		SqlJetDb db;
		try {
			db = openDB(new File(outfile), true);
			configureDB(db);
			createTablesInDB(db);

			buildConfigTable(db);
			buildTopicsTable(db);
			buildContentTable(db);
			buildTopicsWordsIndexTable(db);

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

		System.out.println("TheWord Bible Dictionary Creation completed...");
		System.out.println("Result is stored in: " + new File(outfile).getAbsolutePath());
	}

	private static void createTablesInDB(SqlJetDb db) throws SqlJetException {
		db.createTable("CREATE TABLE config(name text, value text)");
		db.createTable("CREATE TABLE content(topic_id integer primary key, data text, data2 blob)");
		db.createTable(
				"CREATE TABLE topics(id integer primary key, pid integer default 0, subject text, rel_order, content_type text)");
		db.createTable("CREATE TABLE topics_wordindex(id integer, word text, priority integer)");
		db.createIndex("CREATE INDEX idx_topics_rel_order  on topics(rel_order)");
		db.createIndex("CREATE INDEX idx_topics_subject on topics(subject)");
		db.createIndex("CREATE INDEX idx_topics_wordindex_word on topics_wordindex(word, priority)");

	}

	private static void buildConfigTable(SqlJetDb db) throws FileNotFoundException, IOException, SqlJetException {
		System.out.println("Config Table Creation started...");
		ISqlJetTable configTable = db.getTable("config");

		configTable.insert("lang", BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_LANGUAGE));
		configTable.insert("abbrev", BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_IDENTIFIER));
		configTable.insert("title", BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_TITLE));
		configTable.insert("title.english",
				BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_TITLE_IN_ENGLISH));
		configTable.insert("author", BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_AUTHOR));
		configTable.insert("publisher", BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_PUBLISHER));
		configTable.insert("publish.date",
				BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_PUBLISHED_DATE));
		configTable.insert("description",
				BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_DESCRIPTION));
		configTable.insert("description.english",
				BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_DESCRIPTION_IN_ENGLISH));
		// about & description are same content
		configTable.insert("about", BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_DESCRIPTION));
		configTable.insert("creator", BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_CREATOR));

		configTable.insert("type", "1");
		configTable.insert("schema.version", "1");
		configTable.insert("search.index.ver", "4");
		configTable.insert("search.topics.index.ver", "4");
		configTable.insert("content.type", "rtf");
		configTable.insert("categories", "Dictionary\\Tamil");
		// configTable.insert("id", null);
		// configTable.insert("keywords", "");
		// configTable.insert("user", "0");
		// configTable.insert("strong", "0");
		// configTable.insert("source", null);
		// configTable.insert("images.allow.autoresize", "1");
		// configTable.insert("compressed", "1");
		// configTable.insert("compressed.search.data", "0");
		// configTable.insert("do.encrypt", "1");
		// configTable.insert("secure", "1");
		// configTable.insert("isbn", null);
		// configTable.insert("preserve.fonts", "0");
		// configTable.insert("preserve.fonts.list", "");

		configTable.insert("version.major", "1");
		configTable.insert("version.minor", "0");
		configTable.insert("editorial.comments", "version 1.0: first version");
		configTable.insert("version.date", "2023-9-23");

		System.out.println("Config Table Creation completed...");
	}

	public static void buildContentTable(SqlJetDb db) throws SqlJetException {
		System.out.println("Dictionary Table Creation started...");
		System.out.println("Reading the files/words from the folder " + BibleDictionaryCreator.folderPath);
		ISqlJetTable contentTable = db.getTable("content");
		String strItemID = null;
		File folder = new File(BibleDictionaryCreator.folderPath);
		int count = 0;
		for (File file : folder.listFiles()) {
			if (Utils.checkForInValidFile(file)) {
				continue;
			}
			System.out.println("Reading the file: " + file.getName());

			strItemID = Utils.trimDictionaryWord(file.getName().substring(0, file.getName().lastIndexOf(".")));
			if (MapWithBible.dictionaryWordsVsBibleWordsMap.get(strItemID) == null) {
				System.out.println("Word not found: " + strItemID);
				continue;
			}
			count++;
			contentTable.insert(count, buildDescriptionFromFile(file), null);
		}
		System.out.println("Dictionary Table Creation completed...");
		System.out.println("Total Words created in content table is: " + count);
	}

	private static void buildTopicsTable(SqlJetDb db) throws FileNotFoundException, IOException, SqlJetException {
		System.out.println("Topics Table Creation started...");
		int count = 0;
		ISqlJetTable topicsTable = db.getTable("topics");
		for (String dictionaryWord : MapWithBible.dictionaryWordsVsBibleWordsMap.keySet()) {
			count++;
			topicsTable.insert(count, 0, dictionaryWord, count, null);
		}
		System.out.println("Topics Table Creation completed...");
		System.out.println("Total Words created in topics table is: " + count);
	}

	private static void buildTopicsWordsIndexTable(SqlJetDb db)
			throws FileNotFoundException, IOException, SqlJetException {
		System.out.println("topics_wordindex Table Creation started...");
		int dictionaryWordCount = 0;
		int mappingWordCount = 0;
		ISqlJetTable topics_wordindexTable = db.getTable("topics_wordindex");
		for (String dictionaryWord : MapWithBible.dictionaryWordsVsBibleWordsMap.keySet()) {
			dictionaryWordCount++;
			topics_wordindexTable.insert(dictionaryWordCount, dictionaryWord, 1);
			for (String mappingWord : MapWithBible.dictionaryWordsVsBibleWordsMap.get(dictionaryWord)) {
				// Ignore all the mapping words which are already found as dictionary words
				if (!MapWithBible.uniqueDictionaryWords.containsKey(mappingWord)) {
					mappingWordCount++;
					topics_wordindexTable.insert(dictionaryWordCount, mappingWord, 1);
				}
			}
		}
		System.out.println("topics_wordindex Table Creation completed...");
		System.out.println(
				"Total Words created in topics_wordindex table is: " + (dictionaryWordCount + mappingWordCount));
	}

	private static void configureDB(SqlJetDb db) throws SqlJetException {
		db.getOptions().setAutovacuum(true);
		db.beginTransaction(SqlJetTransactionMode.WRITE);
		db.getOptions().setUserVersion(0);
	}

	private static String deleteOutputFileIfAlreadyExists() {
		String outfile = BibleDictionaryCreator.folderPath.replace(BibleDictionaryCreator.outputFile, "") + "/"
				+ BibleDictionaryCreator.outputFile + EXTENSION;
		new File(outfile).delete();
		return outfile;
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
					line = Utils.convertToDecimalNCR(line);
					line = line.replaceAll("&#([0-9]+);", "\\\\u$1\\?");
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
		return sb;
	}

	public static String buildDescription(String line) {
		line = line + "\\par ";
		return line;
	}

	public static String buildH3Description(String line) {
		// Remove the tag [H3]
		line = line.replaceAll("\\[H3\\]", "").strip();

		line = "{\\b {" + line + "}}\\par \\par ";
		return line;
	}

	public static String buildH2Description(String line) {
		// Remove the tag [H2]
		line = line.replaceAll("\\[H2\\]", "").strip();

		line = "{\\b {" + line + "}}\\par \\par ";
		return line;
	}

	public static String buildH1Description(String line) {
		// Remove prefix text like 0001 used for identifying unique no of words
		line = line.replace(line.substring(0, line.indexOf("[H1]")), "");
		// Remove the tag [H1]
		line = line.replaceAll("\\[H1\\]", "").strip();

		line = "{\\b {" + line + "}}\\par \\par ";

		return line;
	}
}
