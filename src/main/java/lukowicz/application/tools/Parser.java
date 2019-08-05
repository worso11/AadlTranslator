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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {
    private List<ComponentInstance> COMPONENT_INSTANCES = new ArrayList<>();
    private Set<String> uniqueComponents = new HashSet<>();
   // private ArrayList<String> uniqueFeature = new ArrayList<>();  // nie Set bo dopuszczamy nie unikalne

    private File fXmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatAADL-XML2.xml");
    private File pnmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatPnml-Output.xml");


    public void parseFile() throws ParserConfigurationException, IOException, SAXException{
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

        for (ComponentInstance cmpI : COMPONENT_INSTANCES) {
            System.out.println("Nazwa elementu " + cmpI.getName());
        }
        System.out.println("Liczba elementow " + COMPONENT_INSTANCES.size());
    }

    private void searchElements(NodeList componenentInstances, ComponentInstance processingElement) {
        for (int i = 0; i < componenentInstances.getLength(); i++) {

            Node component = componenentInstances.item(i);
            System.out.println("\nCurrent Element :" + component.getNodeName());

            if (component.getNodeType() == Node.ELEMENT_NODE) {
                ComponentInstance componentInstance;
                Element actualComponent = (Element) component;
                System.out.println("Name of componenet : " + actualComponent.getAttribute("name"));
                System.out.println("Categroy of componenet : " + actualComponent.getAttribute("category"));
                if (processingElement != null){
                    componentInstance = processingElement;

                }else {
                    componentInstance = new ComponentInstance(actualComponent.getAttribute("name"), actualComponent.getAttribute("category"));
                   // uniqueFeature.clear();
                }

                if (uniqueComponents.contains(actualComponent.getAttribute("name"))){
                    continue;
                }

                ComponentInstance componentInstanceNested = processingElement != null ? new ComponentInstance(actualComponent.getAttribute("name"), actualComponent.getAttribute("category")) : null;

                NodeList featureInstances = actualComponent.getElementsByTagName("featureInstance");

                for (int j = 0; j < featureInstances.getLength(); j++) {
                    Node featureInstance = featureInstances.item(j);
                    Element featureElement = (Element) featureInstance;
                    System.out.println("Name of feature : " + featureElement.getAttribute("name"));
                    if (componentInstanceNested != null) {
                        componentInstanceNested.getFeatureInstance().add(featureElement.getAttribute("name"));
                        //uniqueFeature.remove(featureElement.getAttribute("name"));
                    } else {
                       // uniqueFeature.add(featureElement.getAttribute("name"));
                        componentInstance.getFeatureInstance().add(featureElement.getAttribute("name"));
                    }
                }
                if (componentInstanceNested != null) {
                    //czy nie mozna lepiej??
                    processingElement.getComponentInstancesNested().add(componentInstanceNested);
                    uniqueComponents.add(componentInstanceNested.getName());
                }

                // zagniezdzone komponenenty
                NodeList nestedComponents = actualComponent.getElementsByTagName("componentInstance");
                if (nestedComponents.getLength() != 0) {
                    searchElements(nestedComponents, componentInstance);

                } else {
                    if (!uniqueComponents.contains(componentInstance.getName())) {
                        //componentInstance.getFeatureInstance().addAll(uniqueFeature);
                        COMPONENT_INSTANCES.add(componentInstance);
                        uniqueComponents.add(componentInstance.getName());

                    }
                }

            }
        }
    }
}