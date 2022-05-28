package lukowicz.application;

import lukowicz.application.aadl.ElementSearcher;
import lukowicz.application.petrinet.PetriNetGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;


public class Parser {

    private ElementSearcher elementSearcher;
    private PetriNetGenerator petriNetGenerator;

    public Parser(ElementSearcher elementSearcher, PetriNetGenerator petriNetGenerator) {
        this.elementSearcher = elementSearcher;
        this.petriNetGenerator = petriNetGenerator;
    }

    public void parseFile(File file) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File aadlXmlFile = file;

        Document loadedDocument = builder.parse(aadlXmlFile);

        loadedDocument.getDocumentElement().normalize();

        NodeList componentInstances = loadedDocument.getElementsByTagName("componentInstance");
        Element systemInstance = loadedDocument.getDocumentElement();
        elementSearcher.searchSystemName(systemInstance);
        elementSearcher.searchElements(componentInstances, null);

        NodeList connections = loadedDocument.getElementsByTagName("connectionInstance");
        elementSearcher.searchConnections(connections);
        petriNetGenerator.generatePetriNet(file.getParentFile().getPath());
    }
}


