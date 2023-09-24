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
	public static final String MAPPING_FILE_NAME_FOR_REVIEW = "MAPPING-For-Review.txt";
	public static boolean formatXML = true;
	public static String folderPath;
	public static String outputFile;
	public static Properties DICTIONARY_DETAILS = null;
	public static boolean WRITE_LOGS_TO_FILE = true;
	public static boolean generateMappingsForReview = false;
	public static boolean mapRelatedWordsAutomatically = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws ParserConfigurationException, TransformerException {

		if (!validateInput(args)) {
			return;
		}

		Utils.initSystemOutSettings();

		initConfigurations();

		MapWithBible.buildMap();

		MapWithBible.buildMapWithBible();

		if (generateMappingsForReview) {
			MapWithBible.createMappingFileForReview();
			return;
		}

		String format = DICTIONARY_DETAILS.getProperty("createZefaniaXML");
		if ("yes".equalsIgnoreCase(format) || "true".equalsIgnoreCase(format)) {
			ZefaniaXML.build();
		}
		format = DICTIONARY_DETAILS.getProperty("createWordDocument");
		if ("yes".equalsIgnoreCase(format) || "true".equalsIgnoreCase(format)) {
			WordDocument.build();
		}
		format = DICTIONARY_DETAILS.getProperty("createMyBibleModule");
		if ("yes".equalsIgnoreCase(format) || "true".equalsIgnoreCase(format)) {
			MyBibleZone.build();
		}
		format = DICTIONARY_DETAILS.getProperty("createTheWordModule");
		if ("yes".equalsIgnoreCase(format) || "true".equalsIgnoreCase(format)) {
			TheWord.build();
		}
	}

	private static void initConfigurations() {
		String temp = DICTIONARY_DETAILS.getProperty("generateMappingsForReview");
		if ("yes".equalsIgnoreCase(temp) || "true".equalsIgnoreCase(temp)) {
			generateMappingsForReview = true;
		}
		temp = DICTIONARY_DETAILS.getProperty("mapRelatedWordsAutomatically");
		if ("no".equalsIgnoreCase(temp) || "false".equalsIgnoreCase(temp)) {
			mapRelatedWordsAutomatically = false;
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
