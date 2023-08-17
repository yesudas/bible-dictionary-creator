/**
 * 
 */
package in.wordofgod.bible.dictionary.creator;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 */
public class BibleDictionaryCreatorFromFolder2 {

	public static boolean writeToFile = false;
	public static String folderPath;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws ParserConfigurationException, TransformerException {

		if (args.length == 0) {
			System.out.println("Please input source folder path....");
			// return;
		}else {
			folderPath = args[0];
			writeToFile = true;
		}

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("company");
		doc.appendChild(rootElement);

		// staff 1001

		// add xml elements
		Element staff = doc.createElement("staff");
		// add staff to root
		rootElement.appendChild(staff);
		// add xml attribute
		staff.setAttribute("id", "1001");

		// alternative
		// Attr attr = doc.createAttribute("id");
		// attr.setValue("1001");
		// staff.setAttributeNode(attr);

		Element name = doc.createElement("name");
		// JDK 1.4
		// name.appendChild(doc.createTextNode("mkyong"));
		// JDK 1.5
		name.setTextContent("mkyong");
		staff.appendChild(name);

		Element role = doc.createElement("role");
		role.setTextContent("support");
		staff.appendChild(role);

		Element salary = doc.createElement("salary");
		salary.setAttribute("currency", "USD");
		salary.setTextContent("5000");
		staff.appendChild(salary);

		// add xml comment
		Comment comment = doc.createComment("for special characters like < &, need CDATA");
		staff.appendChild(comment);

		Element bio = doc.createElement("bio");
		// add xml CDATA
		CDATASection cdataSection = doc.createCDATASection("HTML tag <code>testing</code>");
		bio.appendChild(cdataSection);
		staff.appendChild(bio);

		// staff 1002
		Element staff2 = doc.createElement("staff");
		// add staff to root
		rootElement.appendChild(staff2);
		staff2.setAttribute("id", "1002");

		Element name2 = doc.createElement("name");
		name2.setTextContent("yflow");
		staff2.appendChild(name2);

		Element role2 = doc.createElement("role");
		role2.setTextContent("admin");
		staff2.appendChild(role2);

		Element salary2 = doc.createElement("salary");
		salary2.setAttribute("currency", "EUD");
		salary2.setTextContent("8000");
		staff2.appendChild(salary2);

		Element bio2 = doc.createElement("bio");
		// add xml CDATA
		bio2.appendChild(doc.createCDATASection("a & b"));
		staff2.appendChild(bio2);

		if (writeToFile) {
			// write dom document to a file
			try (FileOutputStream output = new FileOutputStream("c:\\test\\staff-dom.xml")) {
				writeXml(doc, output);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// print XML to system console
			writeXml(doc, System.out);
		}
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
		// <?xml version="1.0" encoding="utf-8"?>
		transformer.setOutputProperty(OutputKeys.ENCODING,"utf-8");
		
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
