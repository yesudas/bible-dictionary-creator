/**
 * 
 */
package in.wordofgod.bible.dictionary.creator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

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
public class BibleDictionaryCreatorFromFolder {

	private static final String INFORMATION_FILE_NAME = "INFORMATION.txt";
	public static boolean writeToFile = false;
	public static boolean formatXML = true;
	public static String folderPath;
	public static String outputFile;
	private static Properties dictionaryDetails = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws ParserConfigurationException, TransformerException {

		if (args.length == 0) {
			System.out.println("Please input source folder path....");
			// return;
		} else {
			folderPath = args[0];

			if (folderPath.contains("\\")) {
				outputFile = folderPath.substring(folderPath.lastIndexOf("\\"), folderPath.length());
			} else {
				outputFile = folderPath.substring(0, folderPath.length());
			}

			if (folderPath == null) {
				System.out.println("folderPath is null");
				return;
			}

			File folder = new File(folderPath);
			if (!folder.exists() || !folder.isDirectory()) {
				System.out.println("Folder " + folderPath + " Does not exists");
				return;
			}

			if (folder.listFiles().length == 0) {
				System.out.println("Folder " + folderPath
						+ " does not have any files. Please use one file per dictionary word. First line hould always be the word to which dictionary is written.");
				return;
			}

			//writeToFile = true;
			loadDictionaryDetails();
		}

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

		if (writeToFile) {
			// write dom document to a file
			File file = new File(outputFile + ".xml");
			try {
				FileOutputStream output = new FileOutputStream(file);
				writeXml(doc, output);
				System.out.println("Dictionary created with the name: " + folderPath + ".xml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// print XML to system console
			writeXml(doc, System.out);
		}
	}

	private static void buildItems(Document doc, Element rootElement) {

		System.out.println("Reading the files/words from the folder " + folderPath);

		BufferedReader reader;
		String strItemID;
		Element item;
		File folder = new File(folderPath);
		for (File file : folder.listFiles()) {
			if(INFORMATION_FILE_NAME.equalsIgnoreCase(file.getName())) {
				continue;
			}
			System.out.println("Reading the file: " + file.getName());

			strItemID = file.getName().substring(0, file.getName().lastIndexOf("."));
			item = doc.createElement("item");
			rootElement.appendChild(item);
			item.setAttribute("id", strItemID);
			

			try {
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
				reader = new BufferedReader(isr);
				String line = reader.readLine();

				Element description = doc.createElement("description");
				item.appendChild(description);
				String sb = "";

				while (line != null) {

					line = line.strip();
					if (!line.equals("")) {

						if (line.contains("[H1]")) {
							sb = sb + buildH1Description(doc, item, line);
						} else if (line.contains("[H2]")) {
							sb = sb + buildH2Description(doc, item, line);
						} else if (line.contains("[H3]")) {
							sb = sb + buildH3Description(doc, item, line);
						} else {
							sb = sb + buildDescription(doc, item, line);
						}
					}
					line = reader.readLine();
				}
				CDATASection cdataSection = doc.createCDATASection(sb);
				description.appendChild(cdataSection);

				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String buildDescription(Document doc, Element item, String line) {
		line = line + "<p/>";
		return line;
	}

	private static String buildH3Description(Document doc, Element item, String line) {
		// Remove the tag [H3]
		line = line.replaceAll("\\[H3\\]", "").strip();

		line = "<b>" + line + "</b><p/>";
		return line;
	}

	private static String buildH2Description(Document doc, Element item, String line) {
		// Remove the tag [H2]
		line = line.replaceAll("\\[H2\\]", "").strip();

		line = "<b>" + line + "</b><p/>";
		return line;
	}

	private static String buildH1Description(Document doc, Element item, String line) {
		// Remove prefix text like 0001 used for identifying unique no of words
		line = line.replace(line.substring(0, line.indexOf("[H1]")), "");
		// Remove the tag [H1]
		line = line.replaceAll("\\[H1\\]", "").strip();

		line = "<b><el>" + line + "</el></b><p/>";

		return line;
	}

	private static Element buildInformation(Document doc) {
		Element information = doc.createElement("INFORMATION");

		Element element = doc.createElement("subject");
		CDATASection cdataSection = doc.createCDATASection(dictionaryDetails.getProperty("subject"));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("publisher");
		cdataSection = doc.createCDATASection(dictionaryDetails.getProperty("publisher"));
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
		cdataSection = doc.createCDATASection(dictionaryDetails.getProperty("title"));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("creator");
		cdataSection = doc.createCDATASection(dictionaryDetails.getProperty("creator"));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("description");
		cdataSection = doc.createCDATASection(dictionaryDetails.getProperty("description"));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("identifier");
		cdataSection = doc.createCDATASection(dictionaryDetails.getProperty("identifier"));
		element.appendChild(cdataSection);
		information.appendChild(element);

		element = doc.createElement("language");
		element.setTextContent(dictionaryDetails.getProperty("language"));
		information.appendChild(element);

		return information;
	}

	private static Element buildRootElement(Document doc) {
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
	private static void writeXml(Document doc, OutputStream output) throws TransformerException {

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

	private static void loadDictionaryDetails() {
		dictionaryDetails = new Properties();
		BufferedReader propertyReader;
		try {
			File infoFile = new File(folderPath + "//" + INFORMATION_FILE_NAME);
			propertyReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(infoFile), "UTF8"));
			dictionaryDetails.load(propertyReader);
			propertyReader.close();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
