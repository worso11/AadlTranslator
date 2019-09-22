package lukowicz.application;

import lukowicz.application.aadl.ElementSearcher;
import lukowicz.application.memory.Cache;
import lukowicz.application.petrinet.PetriNetGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;


//mapowanie processor, memmory???

//wg dokumentu bus to arc
public class Parser {

    private ElementSearcher elementSearcher = new ElementSearcher();
    private PetriNetGenerator petriNetGenerator = new PetriNetGenerator();

    private File aadlXmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatAADL-XML2.xml");

    public void parseFile() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document loadedDocument = builder.parse(aadlXmlFile);

        loadedDocument.getDocumentElement().normalize();

        NodeList componentInstances = loadedDocument.getElementsByTagName("componentInstance");
        elementSearcher.searchElements(componentInstances, null);
        Cache.moveProcesses();

        NodeList connections = loadedDocument.getElementsByTagName("connectionInstance");
        elementSearcher.searchConnections(connections);
        petriNetGenerator.generatePetriNet();

    }
}


