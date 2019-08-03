package lukowicz.application.tools;

import lukowicz.application.model.ComponentInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parser {
    private List<ComponentInstance> COMPONENT_INSTANCES = new ArrayList<>();
    private HashMap<String,String> uniqueComponent = new HashMap<>();
    File fXmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatAADL-XML2.xml");
    File pnmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatPnml-Output.xml");


    public void parseFile() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();


        Document loadedDocument = builder.parse(fXmlFile);
        Document pnmlDocument = builder.newDocument();
        Element root = pnmlDocument.createElement("pnml:pnml");

        Element module = pnmlDocument.createElement("pnml:module");
        root.appendChild(module);
        pnmlDocument.appendChild(root);

        Element pnmlName = pnmlDocument.createElement("pnml:name");
        module.appendChild(pnmlName);


        Element pnmlText = pnmlDocument.createElement("pnml:text");
        pnmlText.appendChild(pnmlDocument.createTextNode("New_page"));
        pnmlName.appendChild(pnmlText);

        loadedDocument.getDocumentElement().normalize();

        NodeList componentInstances = loadedDocument.getElementsByTagName("componentInstance");
        searchElements(componentInstances, null);
        System.out.println("//////////////////////---------------//////////////////////");

        for (ComponentInstance cmpI:COMPONENT_INSTANCES) {
            System.out.println("Nazwa elementu "+cmpI.getName());
        }
        System.out.println("Liczba elementow "+COMPONENT_INSTANCES.size());
    }

    private void searchElements(NodeList componenentInstances, ComponentInstance processingElement) {
        for (int i = 0; i < componenentInstances.getLength(); i++) {

            Node component = componenentInstances.item(i);

            System.out.println("\nCurrent Element :" + component.getNodeName());

            if (component.getNodeType() == Node.ELEMENT_NODE) {

                Element componentElement = (Element) component;
                System.out.println("Name of componenet : " + componentElement.getAttribute("name"));


                System.out.println("Categroy of componenet : " + componentElement.getAttribute("category"));
                ComponentInstance componentInstance = processingElement != null ? processingElement : new ComponentInstance(componentElement.getAttribute("name"), componentElement.getAttribute("category"));
                NodeList featureInstances = componentElement.getElementsByTagName("featureInstance");

                ComponentInstance componentInstanceNested = processingElement != null ? new ComponentInstance(componentElement.getAttribute("name"), componentElement.getAttribute("category")) : null;

                for (int j = 0; j < featureInstances.getLength(); j++) {
                    Node featureInstance = featureInstances.item(j);

                    Element featureElement = (Element) featureInstance;
                    System.out.println("Name of feature : " + featureElement.getAttribute("name"));
                    if(componentInstanceNested != null ){
                        componentInstanceNested.getFeatureInstance().add(featureElement.getAttribute("name"));
                    }else {
                        componentInstance.getFeatureInstance().add(featureElement.getAttribute("name"));
                    }
                }
                if (componentInstanceNested != null ){
                    processingElement.getComponentInstancesNested().add(componentInstanceNested);
                    uniqueComponent.put(componentInstanceNested.getName(),"");
                }

                // zagniezdzone komponenenty
                NodeList nestedComponents = componentElement.getElementsByTagName("componentInstance");
                if (nestedComponents.getLength() != 0) {
                    //wywlaj siebie
                    searchElements(nestedComponents, componentInstance);

                } else {
                    System.out.println("Brak elementów do przetworzenia  ");
                    if (!uniqueComponent.containsKey(componentInstance.getName())) {
                        COMPONENT_INSTANCES.add(componentInstance);
                        uniqueComponent.put(componentInstance.getName(), "");
                    }
                }


            }
        }
    }
}