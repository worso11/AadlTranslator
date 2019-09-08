package lukowicz.application.tools;

import lukowicz.application.model.Category;
import lukowicz.application.model.ComponentInstance;
import lukowicz.application.model.Connection;
import lukowicz.application.model.FeatureInstance;
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



//mapowanie processor, memmory???

//wg dokumentu bus to arc

public class Parser {
    private List<ComponentInstance> COMPONENT_INSTANCES = new ArrayList<>();
    private List<ComponentInstance> PROCESSES = new ArrayList<>();
    private List<Connection> CONNECTIONS = new ArrayList<>();
    private Set<String> uniqueComponents = new HashSet<>();
    private Set<String> usedFeature = new HashSet<>();
    private Set<String> contexts = new HashSet<>();
   // private ArrayList<String> uniqueFeature = new ArrayList<>();  // nie Set bo dopuszczamy nie unikalne

    private File fXmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatAADL-XML2.xml");
    private File pnmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatPetriNet-Output.xml");


    private Double PLACE_X_START_POSITION = -504.000000;
    private Double PLACE_Y_START_POSITION = 312.000000;
    private Double TRANSITION_X_START_POSITION = 444.000000;
    private Double TRANSITION_Y_START_POSITION = 314.000000;


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
            System.out.println("Id" + cmpI.getId());
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
        int numberPage = 1;
        String actualContext = "";

        Document pnmlDocument = builder.newDocument();
        Element root = pnmlDocument.createElement("cpnet");
        pnmlDocument.appendChild(root);

        //page startowy
        Element page = generateNewPage(numberPage, pnmlDocument, root);
        List<Node> arcs = generateConnections(actualContext, pnmlDocument, page);
        translateElements(pnmlDocument, page,COMPONENT_INSTANCES);
        insertArcToPNet(page, arcs);


        //pageForProcess           konteksty!!!!!!!!!!!!!!!!!  zrobic odniesienia do stron!!
        for(ComponentInstance pageProcess: PROCESSES){
            Element pageForProcess = generateNewPage(++numberPage, pnmlDocument, root);
            List<Node> arcs2 = generateConnections("6",pnmlDocument,pageForProcess);
            translateElements(pnmlDocument, pageForProcess,pageProcess.getComponentInstancesNested());
            insertArcToPNet(pageForProcess, arcs2);
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

    private void insertArcToPNet(Element page, List<Node> arcs) {
        for (Node arc : arcs) {
            page.appendChild(arc);
        }
    }

    private List<Node> generateConnections(String actualContext, Document pnmlDocument, Element page) {
        List<Node> arcs = new ArrayList<>();
        for(Connection connection : CONNECTIONS){
            if(actualContext.equals(connection.getContext())){

                ArrayList<Integer> source = preparePorts(connection.getSource());
                ArrayList<Integer> dst = preparePorts(connection.getDestination());

                ConnectionNode sourceNode = getConnectionNode(source,pnmlDocument,null);
                ConnectionNode dstNode = getConnectionNode(dst, pnmlDocument, null);

                Element arc1 = pnmlDocument.createElement("arc");
                Attr arcId = pnmlDocument.createAttribute("id");
                arcId.setValue(connection.getId());
                arc1.setAttributeNode(arcId);

                Element transend = pnmlDocument.createElement("transend");
                Attr transendIdRef = pnmlDocument.createAttribute("idref");

                Element placeend = pnmlDocument.createElement("placeend");
                Attr placeendIdRef = pnmlDocument.createAttribute("idref");
                Attr arcOrientation = pnmlDocument.createAttribute("orientation");

                if(sourceNode.getCategory().equals(Category.BUS.getValue())){
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(dstNode.getPlaceId());
                    usedFeature.add(dstNode.getPlaceId()); // dodane

                }
                else {
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(sourceNode.getPlaceId()); //było sourceNode.getPlaceId jak chcemy miejsce z wyjsciowego
                    usedFeature.add(sourceNode.getPlaceId());  // było sourceNode.getPlaceId jak chcemy miejsce z wyjsciowego
                }


                arcOrientation.setValue("TtoP");
                arc1.setAttributeNode(arcOrientation);


                transend.setAttributeNode(transendIdRef);
                placeend.setAttributeNode(placeendIdRef);

                arc1.appendChild(transend);
                arc1.appendChild(placeend);

                //page.appendChild(arc1);   było

                arcs.add(arc1);



                if(!sourceNode.getCategory().equals(Category.BUS.getValue())) {

                    Element arc2 = pnmlDocument.createElement("arc");
                    Attr arcId2 = pnmlDocument.createAttribute("id");
                    arcId2.setValue(connection.getId() + "#");
                    arc2.setAttributeNode(arcId2);

                    Element transend2 = pnmlDocument.createElement("transend");
                    Attr transendIdRef2 = pnmlDocument.createAttribute("idref");

                    Element placeend2 = pnmlDocument.createElement("placeend");
                    Attr placeendIdRef2 = pnmlDocument.createAttribute("idref");

                    transendIdRef2.setValue(dstNode.getTransId());
                    placeendIdRef2.setValue(sourceNode.getPlaceId()); //było sourceNode.getPlaceId jak chcemy miejsce z wyjsciowego

                   // usedFeature.add(sourceNode.getPlaceId()); // nie musi byc

                    Attr arcOrientation2 = pnmlDocument.createAttribute("orientation");
                    arcOrientation2.setValue("PtoT");
                    arc2.setAttributeNode(arcOrientation2);


                    transend2.setAttributeNode(transendIdRef2);
                    placeend2.setAttributeNode(placeendIdRef2);

                    arc2.appendChild(transend2);
                    arc2.appendChild(placeend2);

                    //page.appendChild(arc2); // było
                    arcs.add(arc2);
                }

            }

        }
        return arcs;

    }

    private void translateElements(Document pnmlDocument, Element page, List<ComponentInstance> componentInstances  ) {
        for(ComponentInstance componentInstance: componentInstances){
           String componentInstanceCategory = componentInstance.getCategory();
           if(componentInstanceCategory.equals(Category.DEVICE.getValue()) || componentInstanceCategory.equals(Category.PROCESS.getValue()) || componentInstanceCategory.equals(Category.THREAD.getValue()) ){
               Element transition = generateTransition(pnmlDocument, "trans", componentInstance.getName(), componentInstance.getId());
               page.appendChild(transition);
           }
           if (componentInstanceCategory.equals(Category.BUS.getValue()) ){
               Element transition = generateTransition(pnmlDocument, "trans", componentInstance.getName(), componentInstance.getId());
               page.appendChild(transition);
           }
           List<FeatureInstance> featureInstances = componentInstance.getFeatureInstance();
           for(FeatureInstance feature:featureInstances){
               Element place = generatePlace(pnmlDocument, "place", feature.getName(), feature.getId());
               if(usedFeature.contains(feature.getId())){
                   page.appendChild(place);
               }

           }

        }
        usedFeature.clear();
    }

    private Element generateNewPage(int numberPage, Document pnmlDocument, Element root) {
        Element page = pnmlDocument.createElement("page");
        Attr pageId = pnmlDocument.createAttribute("id");
        pageId.setValue(String.valueOf(numberPage));
        page.setAttributeNode(pageId);
        Element pageAttr = pnmlDocument.createElement("pageattr");
        Attr pageAttrName = pnmlDocument.createAttribute("name");
        pageAttrName.setValue("pageName");
        pageAttr.setAttributeNode(pageAttrName);
        page.appendChild(pageAttr);
        root.appendChild(page);
        return page;
    }

    private Element generatePlace(Document pnmlDocument, String place2, String name, String id) {
        Element place = pnmlDocument.createElement(place2);
        Element placeText = pnmlDocument.createElement("text");
        placeText.appendChild(pnmlDocument.createTextNode(name));
        place.appendChild(placeText);

        Attr placeId = pnmlDocument.createAttribute("id");
        placeId.setValue(id);
        place.setAttributeNode(placeId);
        return place;
    }

    private Element generateTransition(Document pnmlDocument, String trans, String name, String id) {
        Element transition = pnmlDocument.createElement(trans);
        Element transitionText = pnmlDocument.createElement("text");
        transitionText.appendChild(pnmlDocument.createTextNode(name));
        transition.appendChild(transitionText);

        Attr transitionId = pnmlDocument.createAttribute("id");
        transitionId.setValue(id);
        transition.setAttributeNode(transitionId);
        return transition;
    }

    private String setArcOrientation(ConnectionNode sourceNode, ConnectionNode dstNode) {
        if(sourceNode.getCategory().equals(Category.FEATURE.getValue())){
            return "PtoT";
        }
        else {
            return "TtoP";
        }

    }

    private ConnectionNode getConnectionNode(List<Integer> path, Document pnmlDocument, ComponentInstance actualComponentInstance) {

        for (int j=0; j<path.size(); ++j){
            ComponentInstance processingComponent = actualComponentInstance != null ? actualComponentInstance : COMPONENT_INSTANCES.get(path.get(j));
            if(j == path.size() - 1){
                return new ConnectionNode(processingComponent.getId(),null,processingComponent.getCategory());
            }
            else if(j == path.size()- 2){
                return new ConnectionNode(processingComponent.getId(),processingComponent.getFeatureInstance().get(path.get(j+1)).getId(),processingComponent.getCategory());
            }
            else {
                return getConnectionNode(path.subList(j+1,path.size()), pnmlDocument, processingComponent.getComponentInstancesNested().get(path.get(j+1)));
            }
        }
        return null;
    }

    private ArrayList<Integer> preparePorts(String source) {
        String[] sourceSplitted =  source.split(" ");
        ArrayList<Integer> sourceList = new ArrayList<>();
        for (String element: sourceSplitted) {
            sourceList.add(Integer.valueOf(element));
        }
        return sourceList;

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
            System.out.println("destination of  connection : " + actualConnection.getAttribute("destination"));

            String context = actualConnection.getAttribute("context").replaceAll("\\D+"," ").trim();
            String source = actualConnection.getAttribute("source").replaceAll("\\D+"," ").trim();
            String destination = actualConnection.getAttribute("destination").replaceAll("\\D+"," ").trim();
            Connection newConnection = new Connection(context,source,destination);


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
               //System.out.println("Name of componenet : " + actualComponent.getAttribute("name"));
               // System.out.println("Categroy of componenet : " + actualComponent.getAttribute("category"));
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
                        componentInstance.getReverseFeatureInstances().remove(new FeatureInstance(featureElement.getAttribute("name")));
                        componentInstance.getReverseFeatureInstances();//wroc do starego porzadku
                        componentInstanceNested.getFeatureInstance().add(new FeatureInstance(featureElement.getAttribute("name")));
                        //uniqueFeature.remove(featureElement.getAttribute("name"));
                    } else {
                       // uniqueFeature.add(featureElement.getAttribute("name"));
                        componentInstance.getFeatureInstance().add(new FeatureInstance(featureElement.getAttribute("name")));
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

class ConnectionNode{

    private String transId;
    private String placeId;
    private String category;

    public ConnectionNode(String transId, String placeId, String category) {
        this.transId = transId;
        this.placeId = placeId;
        this.category = category;
    }

    public String getTransId() {
        return transId;
    }


    public String getPlaceId() {
        return placeId;
    }



    public String getCategory() {
        return category;
    }


}