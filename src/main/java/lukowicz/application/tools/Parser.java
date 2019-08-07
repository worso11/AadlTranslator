package lukowicz.application.tools;

import lukowicz.application.model.Category;
import lukowicz.application.model.ComponentInstance;
import lukowicz.application.model.Connection;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

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
import java.io.IOException;
import java.util.*;

public class Parser {
    private List<ComponentInstance> COMPONENT_INSTANCES = new ArrayList<>();
    private List<ComponentInstance> PROCESSES = new ArrayList<>();
    private List<Connection> CONNECTIONS = new ArrayList<>();
    private Set<String> uniqueComponents = new HashSet<>();
    private Set<String> contexts = new HashSet<>();
   // private ArrayList<String> uniqueFeature = new ArrayList<>();  // nie Set bo dopuszczamy nie unikalne

    private File fXmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatAADL-XML2.xml");
    private File pnmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatPetriNet-Output.xml");


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
        moveProcesses();
        System.out.println("//////////////////////---------------//////////////////////");

        for (ComponentInstance cmpI : COMPONENT_INSTANCES) {
            System.out.println("Nazwa elementu " + cmpI.getName());
        }
        NodeList connections = loadedDocument.getElementsByTagName("connectionReference");
        searchConnections(connections);
        generatePetriNet();


        System.out.println("Liczba elementow " + COMPONENT_INSTANCES.size());
    }

    private void moveProcesses() {
        for(int i = 0; i<COMPONENT_INSTANCES.size(); ++i){
            if(COMPONENT_INSTANCES.get(i).getCategory().equals(Category.PROCESS.getValue())){
                PROCESSES.add(COMPONENT_INSTANCES.get(i));
            }
        }
    }

    private void generatePetriNet() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document pnmlDocument = builder.newDocument();
        Element root = pnmlDocument.createElement("cpnet");
        pnmlDocument.appendChild(root);


        Element page = pnmlDocument.createElement("page");
        Attr pageId = pnmlDocument.createAttribute("id");
        pageId.setValue("1");
        page.setAttributeNode(pageId);
        Element pageAttr = pnmlDocument.createElement("pageattr");
        Attr pageAttrName = pnmlDocument.createAttribute("name");
        pageAttrName.setValue("pageName");
        root.appendChild(page);

        for(ComponentInstance componentInstance: COMPONENT_INSTANCES){
           String componentInstanceName = componentInstance.getName();
           String componentInstanceCategory = componentInstance.getCategory();
           if(componentInstanceCategory.equals(Category.DEVICE.getValue())){
               Element transition = pnmlDocument.createElement("trans");
               Element transitionText = pnmlDocument.createElement("text");
               transitionText.appendChild(pnmlDocument.createTextNode(componentInstance.getName()));
               transition.appendChild(transitionText);

               Attr transitionId = pnmlDocument.createAttribute("id");
               transitionId.setValue("1");
               transition.setAttributeNode(transitionId);
               page.appendChild(transition);
           }
           List<String> featureInstances = componentInstance.getFeatureInstance();
           for(String feature:featureInstances){
               Element place = pnmlDocument.createElement("place");
               Element placeText = pnmlDocument.createElement("text");
               placeText.appendChild(pnmlDocument.createTextNode(feature));
               place.appendChild(placeText);

               Attr placeId = pnmlDocument.createAttribute("id");
               placeId.setValue("1");
               place.setAttributeNode(placeId);
               page.appendChild(place);
           }



        }




        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource domSource = new DOMSource(pnmlDocument);
        StreamResult streamResult = new StreamResult(new File(String.valueOf(pnmlFile)));


        transformer.transform(domSource, streamResult);

        System.out.println("Done creating XML File");


    }

    private void searchConnections(NodeList connections) {
        for (int i = 0; i < connections.getLength(); i++) {
            Node connection = connections.item(i);
            System.out.println("\nCurrent Element :" + connection.getNodeName());
            Element actualConnection = (Element) connection;
            System.out.println("Name of  connection : " + actualConnection.getAttribute("name"));
            System.out.println("context of  connection : " + actualConnection.getAttribute("context"));
            System.out.println("source of  connection : " + actualConnection.getAttribute("source"));
            System.out.println("destination of  connection : " + actualConnection.getAttribute("destination"));
            Connection newConnection = new Connection();
            newConnection.setContext(actualConnection.getAttribute("context").replaceAll("\\D+"," ").trim());
            newConnection.setSource(actualConnection.getAttribute("source").replaceAll("\\D+"," ").trim());
            newConnection.setDestination(actualConnection.getAttribute("destination").replaceAll("\\D+"," ").trim());
            CONNECTIONS.add(newConnection);

        }
        CONNECTIONS.sort(Comparator.comparing(Connection::getContext));

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
                        componentInstance.getFeatureInstance().remove(featureElement.getAttribute("name"));
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