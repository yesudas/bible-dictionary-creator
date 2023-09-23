/**
 * 
 */
package in.wordofgod.bible.dictionary.creator;

import java.io.File;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * 
 */
public class BibleDictionaryCreator {

	public static final String INFORMATION_FILE_NAME = "INFORMATION.txt";
	public static final String MAPPING_FILE_NAME = "MAPPING.txt";
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

		Utils.initSystemOutSettings();

		MapWithBible.buildMap();

		MapWithBible.buildMapWithBible();

		if ("yes".equalsIgnoreCase(DICTIONARY_DETAILS.getProperty("createZefaniaXML"))) {
			ZefaniaXML.build();
		}
		if ("yes".equalsIgnoreCase(DICTIONARY_DETAILS.getProperty("createWordDocument"))) {
			WordDocument.build();
		}
		if ("yes".equalsIgnoreCase(DICTIONARY_DETAILS.getProperty("createMyBibleModule"))) {
			MyBibleZone.build();
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

			Utils.loadDictionaryDetails();

			File dir = new File(
					BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_BIBLE_SOURCE_DIRECTORY));
			if (!dir.exists()) {
				System.out.println("Unable to find the bible versions in the given path: "
						+ BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_BIBLE_SOURCE_DIRECTORY));
				return false;
			}
		}
		return true;
	}

}
