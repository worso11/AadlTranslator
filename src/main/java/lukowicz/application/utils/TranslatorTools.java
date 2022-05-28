package lukowicz.application.utils;

import lukowicz.application.aadl.ElementSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;

public class TranslatorTools {

    private static Logger LOG = LoggerFactory.getLogger(TranslatorTools.class);

    public static String generateUUID() {
        return String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16));
    }

    public static ArrayList<Integer> preparePorts(String source) {
        String[] sourceSplitted = source.split(" ");
        ArrayList<Integer> sourceList = new ArrayList<>();
        for (String element : sourceSplitted) {
            sourceList.add(Integer.valueOf(element));
        }
        return sourceList;
    }

    public static Document createDocumentFile() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.newDocument();
    }

    public static void saveFile(Document pnmlDocument, File petriNetXmlFile) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource domSource = new DOMSource(pnmlDocument);
        StreamResult streamResult = new StreamResult(new File(String.valueOf(petriNetXmlFile)));

        transformer.transform(domSource, streamResult);

        LOG.debug("Done creating XML File");
    }
}
