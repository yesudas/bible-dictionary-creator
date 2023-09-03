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
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * 
 */
public class BibleDictionaryCreator {

	public static final String INFORMATION_FILE_NAME = "INFORMATION.txt";
	public static boolean writeToFile = false;
	public static boolean formatXML = true;
	public static String folderPath;
	public static String outputFile;
	public static Properties DICTIONARY_DETAILS = null;
	public static boolean WRITE_LOGS_TO_FILE = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws ParserConfigurationException, TransformerException {

		if (!validateInput(args)) {
			return;
		}

		initSystemOutSettings();

		loadDictionaryDetails();

		MapWithBible.buildMap();

		if ("yes".equalsIgnoreCase(DICTIONARY_DETAILS.getProperty("createZefaniaXML"))) {
			ZefaniaXML.build();
		}
		if ("yes".equalsIgnoreCase(DICTIONARY_DETAILS.getProperty("createWordDocument"))) {
			WordDocument.build();
		}
	}

	private static boolean validateInput(String[] args) {
		if (args.length == 0) {
			System.out.println("Please input source folder path....");
			return false;
		} else {
			folderPath = args[0];

			File folder = new File(folderPath);
			if (!folder.exists() || !folder.isDirectory()) {
				System.out.println("Folder " + folderPath + " Does not exists");
				return false;
			}

			if (folder.listFiles().length == 0) {
				System.out.println("Folder " + folderPath
						+ " does not have any files. Please use one file per dictionary word. First line hould always be the word to which dictionary is written.");
				return false;
			}

			if (folderPath.contains("\\")) {
				outputFile = folderPath.substring(folderPath.lastIndexOf("\\"), folderPath.length());
			} else {
				outputFile = folderPath.substring(0, folderPath.length());
			}

			if (folderPath == null) {
				System.out.println("folderPath is null");
				return false;
			}
		}
		writeToFile = true;
		return true;
	}

	private static void loadDictionaryDetails() {
		DICTIONARY_DETAILS = new Properties();
		BufferedReader propertyReader;
		try {
			File infoFile = new File(folderPath + "//" + INFORMATION_FILE_NAME);
			propertyReader = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile), "UTF8"));
			DICTIONARY_DETAILS.load(propertyReader);
			propertyReader.close();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void initSystemOutSettings() {
		if (WRITE_LOGS_TO_FILE) {
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

}
