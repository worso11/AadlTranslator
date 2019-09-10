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
    private File pnmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatPetriNet-OutputTeeest.xml");


    public void parseFile() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document loadedDocument = builder.parse(fXmlFile);

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

        Element workspaceElements = pnmlDocument.createElement("workspaceElements");
        Element generator = pnmlDocument.createElement("generator");
        Attr toolAttr = pnmlDocument.createAttribute("tool");
        toolAttr.setValue("CPN Tools");
        generator.setAttributeNode(toolAttr);
        Attr versionAttr = pnmlDocument.createAttribute("version");
        versionAttr.setValue("4.0.1");
        generator.setAttributeNode(versionAttr);

        Attr formatAttr = pnmlDocument.createAttribute("format");
        formatAttr.setValue("6");
        generator.setAttributeNode(formatAttr);

        workspaceElements.appendChild(generator);

        Element root = pnmlDocument.createElement("cpnet");
        workspaceElements.appendChild(root);
        pnmlDocument.appendChild(workspaceElements);

        generateGlobBox(pnmlDocument, root);

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
        workspaceElements.appendChild(root);

    }

    private void generateGlobBox(Document pnmlDocument, Element root) {
        Element globbox = pnmlDocument.createElement("globbox");
        Element block = pnmlDocument.createElement("block");
        Attr attrId = pnmlDocument.createAttribute("id");
        attrId.setValue(UUID.randomUUID().toString().replace("-", ""));
        block.setAttributeNode(attrId);

        Element idElement = pnmlDocument.createElement("id");
        idElement.setTextContent("Standard priorities");

        block.appendChild(idElement);
        createMlElement(pnmlDocument, block, "val P_HIGH = 100;");
        createMlElement(pnmlDocument, block, "val P_NORMAL = 1000;");
        createMlElement(pnmlDocument, block, "val P_LOW = 10000;");
        globbox.appendChild(block);

        //---
        generateStandardUnits(pnmlDocument, globbox);


        root.appendChild(globbox);
    }

    private void generateStandardUnits(Document pnmlDocument, Element globbox) {
        Element block = pnmlDocument.createElement("block");
        Attr attrId = pnmlDocument.createAttribute("id");
        attrId.setValue(UUID.randomUUID().toString().replace("-", ""));
        block.setAttributeNode(attrId);
        Element idElement = pnmlDocument.createElement("id");
        idElement.setTextContent("Standard priorities");
        block.appendChild(idElement);



        generateSimpleType(pnmlDocument, block,"UNIT","colset UNIT = unit");
        generateSimpleType(pnmlDocument, block,"BOOL",null);
        generateSimpleType(pnmlDocument, block,"INTINF","colset INTINF = intinf;");
        generateSimpleType(pnmlDocument, block,"TIME","colset TIME = time;");
        generateSimpleType(pnmlDocument, block,"REAL","colset REAL = real;");
        generateSimpleType(pnmlDocument, block,"String",null);


        globbox.appendChild(block);
    }

    private void generateSimpleType(Document pnmlDocument, Element block, String colorId, String layoutText) {
        Element colorElement = pnmlDocument.createElement("color");
        Attr attrIdColor = pnmlDocument.createAttribute("id");
        attrIdColor.setValue(UUID.randomUUID().toString().replace("-", ""));
        colorElement.setAttributeNode(attrIdColor);
        Element idColorElement = pnmlDocument.createElement("id");
        idColorElement.setTextContent(colorId);
        colorElement.appendChild(idColorElement);
        Element colorUnitElement = pnmlDocument.createElement(colorId.toLowerCase());
        colorElement.appendChild(colorUnitElement);
        if(layoutText != null ) {
            Element layoutElement = pnmlDocument.createElement("layout");
            layoutElement.setTextContent(layoutText);
            colorElement.appendChild(layoutElement);
        }

        block.appendChild(colorElement);
    }

    private void createMlElement(Document pnmlDocument, Element block, String s) {
        Element mlElement = pnmlDocument.createElement("ml");
        Attr mlAttrId = pnmlDocument.createAttribute("id");
        mlAttrId.setValue(UUID.randomUUID().toString().replace("-", ""));
        mlElement.setAttributeNode(mlAttrId);
        mlElement.setTextContent(s);
        Element layoutElement = pnmlDocument.createElement("layout");
        layoutElement.setTextContent(s);
        mlElement.appendChild(layoutElement);
        block.appendChild(mlElement);
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

                setArcGraphicsProperties(pnmlDocument, arc1, connection.getPos_X(), connection.getPos_Y());

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

                    setArcGraphicsProperties(pnmlDocument, arc2, connection.getPos_X(), connection.getPos_Y());

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

    private void setArcGraphicsProperties(Document pnmlDocument, Element arc1, String pos_x, String pos_y) {
        Element arcPosition = pnmlDocument.createElement("posattr");
        Attr positionX = pnmlDocument.createAttribute("x");
        positionX.setValue(pos_x);
        Attr positionY = pnmlDocument.createAttribute("y");
        positionY.setValue(pos_y);
        arcPosition.setAttributeNode(positionX);
        arcPosition.setAttributeNode(positionY);
        arc1.appendChild(arcPosition);


        Element fillProperty = pnmlDocument.createElement("fillattr");
        Attr colorFill = pnmlDocument.createAttribute("colour");
        colorFill.setValue("White");
        fillProperty.setAttributeNode(colorFill);
        Attr pattern = pnmlDocument.createAttribute("pattern");
        pattern.setValue("");
        fillProperty.setAttributeNode(pattern);
        Attr filled = pnmlDocument.createAttribute("filled");
        pattern.setValue("false");
        fillProperty.setAttributeNode(filled);
        arc1.appendChild(fillProperty);


        Element lineProperty = pnmlDocument.createElement("lineattr");
        Attr colorLine = pnmlDocument.createAttribute("colour");
        colorLine.setValue("Black");
        lineProperty.setAttributeNode(colorLine);
        Attr thick = pnmlDocument.createAttribute("thick");
        thick.setValue("1");
        lineProperty.setAttributeNode(thick);
        Attr type = pnmlDocument.createAttribute("type");
        type.setValue("solid");
        lineProperty.setAttributeNode(type);
        arc1.appendChild(lineProperty);

        Element textProperty = pnmlDocument.createElement("textattr");
        Attr colorText = pnmlDocument.createAttribute("colour");
        colorText.setValue("Black");
        textProperty.setAttributeNode(colorText);
        Attr isBold = pnmlDocument.createAttribute("bold");
        isBold.setValue("false");
        textProperty.setAttributeNode(isBold);
        arc1.appendChild(textProperty);
    }

    private void translateElements(Document pnmlDocument, Element page, List<ComponentInstance> componentInstances  ) {
        for(ComponentInstance componentInstance: componentInstances){
           String componentInstanceCategory = componentInstance.getCategory();
           if(componentInstanceCategory.equals(Category.DEVICE.getValue()) || componentInstanceCategory.equals(Category.PROCESS.getValue()) || componentInstanceCategory.equals(Category.THREAD.getValue()) ){
               Element transition = generateTransition(pnmlDocument, "trans", componentInstance);
               page.appendChild(transition);
           }
           if (componentInstanceCategory.equals(Category.BUS.getValue()) ){
               Element transition = generateTransition(pnmlDocument, "trans", componentInstance);
               page.appendChild(transition);
           }
           List<FeatureInstance> featureInstances = componentInstance.getFeatureInstance();
           for(FeatureInstance feature:featureInstances){
               Element place = generatePlace(pnmlDocument, feature);
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

    private Element generatePlace(Document pnmlDocument, FeatureInstance featureInstance) {
        Element place = pnmlDocument.createElement("place");
        Attr placeId = pnmlDocument.createAttribute("id");
        placeId.setValue(featureInstance.getId());
        place.setAttributeNode(placeId);

        Element placePosition = pnmlDocument.createElement("posattr");
        Attr positionX = pnmlDocument.createAttribute("x");
        positionX.setValue(featureInstance.getPos_X().toString());
        Attr positionY = pnmlDocument.createAttribute("y");
        positionY.setValue(featureInstance.getPos_Y().toString());
        placePosition.setAttributeNode(positionX);
        placePosition.setAttributeNode(positionY);
        place.appendChild(placePosition);


        Element fillProperty = pnmlDocument.createElement("fillattr");
        Attr colorFill = pnmlDocument.createAttribute("colour");
        colorFill.setValue("White");
        fillProperty.setAttributeNode(colorFill);
        Attr pattern = pnmlDocument.createAttribute("pattern");
        pattern.setValue("");
        fillProperty.setAttributeNode(pattern);
        Attr filled = pnmlDocument.createAttribute("filled");
        pattern.setValue("false");
        fillProperty.setAttributeNode(filled);
        place.appendChild(fillProperty);


        Element lineProperty = pnmlDocument.createElement("lineattr");
        Attr colorLine = pnmlDocument.createAttribute("colour");
        colorLine.setValue("Black");
        lineProperty.setAttributeNode(colorLine);
        Attr thick = pnmlDocument.createAttribute("thick");
        thick.setValue("1");
        lineProperty.setAttributeNode(thick);
        Attr type = pnmlDocument.createAttribute("type");
        type.setValue("solid");
        lineProperty.setAttributeNode(type);
        place.appendChild(lineProperty);

        Element textProperty = pnmlDocument.createElement("textattr");
        Attr colorText = pnmlDocument.createAttribute("colour");
        colorText.setValue("Black");
        textProperty.setAttributeNode(colorText);
        Attr isBold = pnmlDocument.createAttribute("bold");
        isBold.setValue("false");
        textProperty.setAttributeNode(isBold);
        place.appendChild(textProperty);


        Element placeText = pnmlDocument.createElement("text");
        placeText.appendChild(pnmlDocument.createTextNode(featureInstance.getName()));
        place.appendChild(placeText);

        Element ellipseProperty = pnmlDocument.createElement("ellipse");
        Attr weight = pnmlDocument.createAttribute("w");
        weight.setValue("60.000000");
        ellipseProperty.setAttributeNode(weight);
        Attr height = pnmlDocument.createAttribute("h");
        height.setValue("40.000000");
        ellipseProperty.setAttributeNode(height);
        place.appendChild(ellipseProperty);


        return place;
    }

    private Element generateTransition(Document pnmlDocument, String trans,ComponentInstance componentInstance) {
        Element transition = pnmlDocument.createElement(trans);

        Element transitionPosition = pnmlDocument.createElement("posattr");
        Attr positionX = pnmlDocument.createAttribute("x");
        positionX.setValue(componentInstance.getPos_X().toString());
        Attr positionY = pnmlDocument.createAttribute("y");
        positionY.setValue(componentInstance.getPos_Y().toString());
        transitionPosition.setAttributeNode(positionX);
        transitionPosition.setAttributeNode(positionY);
        transition.appendChild(transitionPosition);


        Element fillProperty = pnmlDocument.createElement("fillattr");
        Attr colorFill = pnmlDocument.createAttribute("colour");
        colorFill.setValue("White");
        fillProperty.setAttributeNode(colorFill);
        Attr pattern = pnmlDocument.createAttribute("pattern");
        pattern.setValue("");
        fillProperty.setAttributeNode(pattern);
        Attr filled = pnmlDocument.createAttribute("filled");
        pattern.setValue("false");
        fillProperty.setAttributeNode(filled);
        transition.appendChild(fillProperty);


        Element lineProperty = pnmlDocument.createElement("lineattr");
        Attr colorLine = pnmlDocument.createAttribute("colour");
        colorLine.setValue("Black");
        lineProperty.setAttributeNode(colorLine);
        Attr thick = pnmlDocument.createAttribute("thick");
        thick.setValue("1");
        lineProperty.setAttributeNode(thick);
        Attr type = pnmlDocument.createAttribute("type");
        type.setValue("solid");
        lineProperty.setAttributeNode(type);
        transition.appendChild(lineProperty);

        Element textProperty = pnmlDocument.createElement("textattr");
        Attr colorText = pnmlDocument.createAttribute("colour");
        colorText.setValue("Black");
        textProperty.setAttributeNode(colorText);
        Attr isBold = pnmlDocument.createAttribute("bold");
        isBold.setValue("false");
        textProperty.setAttributeNode(isBold);
        transition.appendChild(textProperty);

        Element transitionText = pnmlDocument.createElement("text");
        transitionText.appendChild(pnmlDocument.createTextNode(componentInstance.getName()));
        transition.appendChild(transitionText);


        Element boxProperty = pnmlDocument.createElement("box");
        Attr weight = pnmlDocument.createAttribute("w");
        weight.setValue("152.000000");
        boxProperty.setAttributeNode(weight);
        Attr height = pnmlDocument.createAttribute("h");
        height.setValue("40.000000");
        boxProperty.setAttributeNode(height);
        transition.appendChild(boxProperty);


        Attr transitionId = pnmlDocument.createAttribute("id");
        transitionId.setValue(componentInstance.getId());
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