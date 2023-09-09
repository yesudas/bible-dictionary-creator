/**
 * 
 */
package in.wordofgod.bible.dictionary.creator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class ZefaniaXML {

	private static final String EXTENSION = ".xml";

	public static void build() throws ParserConfigurationException, TransformerException {
		System.out.println("ZefaniaXML Bible Dictionary Creation started");

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = buildRootElement(doc);
		doc.appendChild(rootElement);

		// add Information
		rootElement.appendChild(buildInformation(doc));

		// add Items
		buildItems(doc, rootElement);

		if (BibleDictionaryCreator.writeToFile) {
			// write dom document to a file
			File file = new File(BibleDictionaryCreator.folderPath.replace(BibleDictionaryCreator.outputFile, "") + "/"
					+ BibleDictionaryCreator.outputFile + EXTENSION);
			try {
				FileOutputStream output = new FileOutputStream(file);
				writeXml(doc, output);
				System.out.println("Dictionary created with the name: " + file.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// print XML to system console
			writeXml(doc, System.out);
		}
		System.out.println("ZefaniaXML Bible Dictionary Creation completed");
	}

	public static void buildItems(Document doc, Element rootElement) {

		System.out.println("Reading the files/words from the folder " + BibleDictionaryCreator.folderPath);

		String strItemID = null;
		Element item = null;
		File folder = new File(BibleDictionaryCreator.folderPath);
		for (File file : folder.listFiles()) {
			if (BibleDictionaryCreator.INFORMATION_FILE_NAME.equalsIgnoreCase(file.getName())
					|| BibleDictionaryCreator.MAPPING_FILE_NAME.equalsIgnoreCase(file.getName())) {
				continue;
			}
			System.out.println("Reading the file: " + file.getName());

			strItemID = file.getName().substring(0, file.getName().lastIndexOf("."));
			strItemID = strItemID.trim().strip().trim();
			String sb = null;
			if(MapWithBible.dictionaryWordsVsVowelsMap.get(strItemID)==null) {
				continue;
			}
			
			MapWithBible.bibleWordsVsDictionaryWordsMap.forEach((k,v)->{
				
			});
			
			for (String word : MapWithBible.dictionaryWordsVsVowelsMap.get(strItemID)) {
				if (sb == null) {
					sb = buildDescriptionFromFile(file);
				}
				item = doc.createElement("item");
				rootElement.appendChild(item);
				item.setAttribute("id", word);
				Element description = doc.createElement("description");
				item.appendChild(description);
				CDATASection cdataSection = doc.createCDATASection(sb);
				description.appendChild(cdataSection);

			}
		}
	}

	public static void buildItems1(Document doc, Element rootElement) {

		System.out.println("Reading the files/words from the folder " + BibleDictionaryCreator.folderPath);

		String strItemID = null;
		Element item = null;
		File folder = new File(BibleDictionaryCreator.folderPath);
		for (File file : folder.listFiles()) {
			if (BibleDictionaryCreator.INFORMATION_FILE_NAME.equalsIgnoreCase(file.getName())
					|| BibleDictionaryCreator.MAPPING_FILE_NAME.equalsIgnoreCase(file.getName())) {
				continue;
			}
			System.out.println("Reading the file: " + file.getName());

			strItemID = file.getName().substring(0, file.getName().lastIndexOf("."));
			strItemID = strItemID.trim().strip().trim();
			String sb = null;
			if(MapWithBible.dictionaryWordsVsVowelsMap.get(strItemID)==null) {
				continue;
			}
			for (String word : MapWithBible.dictionaryWordsVsVowelsMap.get(strItemID)) {
				if (sb == null) {
					sb = buildDescriptionFromFile(file);
				}
				item = doc.createElement("item");
				rootElement.appendChild(item);
				item.setAttribute("id", word);
				Element description = doc.createElement("description");
				item.appendChild(description);
				CDATASection cdataSection = doc.createCDATASection(sb);
				description.appendChild(cdataSection);

			}
		}
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

	public static Element buildInformation(Document doc) {
		Element information = doc.createElement("INFORMATION");

		Element element = doc.createElement("subject");
		CDATASection cdataSection = doc
				.createCDATASection(BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_SUBJECT));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("publisher");
		cdataSection = doc
				.createCDATASection(BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_PUBLISHER));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("format");
		cdataSection = doc.createCDATASection("Zefania XML Dictionary Markup Language");
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("date");
		element.setTextContent(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		information.appendChild(element);

		element = doc.createElement("title");
		cdataSection = doc
				.createCDATASection(BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_TITLE));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("creator");
		cdataSection = doc
				.createCDATASection(BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_CREATOR));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("description");
		cdataSection = doc
				.createCDATASection(BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_DESCRIPTION));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("identifier");
		cdataSection = doc
				.createCDATASection(BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_IDENTIFIER));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("language");
		element.setTextContent(BibleDictionaryCreator.DICTIONARY_DETAILS.getProperty(Constants.STR_LANGUAGE));
		information.appendChild(element);

		return information;
	}

	public static Element buildRootElement(Document doc) {
		Element rootElement = doc.createElement("dictionary");
		rootElement.setAttribute("type", "x-dictionary");
		rootElement.setAttribute("refbible", "any");
		rootElement.setAttribute("revision", "0");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		return rootElement;
	}

	/**
	 * write doc to output stream
	 * 
	 * @param doc
	 * @param output
	 * @throws TransformerException
	 */
	public static void writeXml(Document doc, OutputStream output) throws TransformerException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		// pretty print
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		// set xml encoding
		// <?xml version="1.0" encoding="UTF-8"?>
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		// hide or display the xml declaration
		// hide or display <?xml version="1.0" encoding="UTF-8" standalone="no"?>
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

		// hide the standalone="no"
		doc.setXmlStandalone(true);

		// set xml version
		// <?xml version="1.0"?>
		doc.setXmlVersion("1.0");

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(output);

		transformer.transform(source, result);

	}

}
