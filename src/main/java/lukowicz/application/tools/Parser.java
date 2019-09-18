package lukowicz.application.tools;

import lukowicz.application.model.*;
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
    private ArrayList<String> INSTANCES_BINDERS = new ArrayList<>();
    private ArrayList<Socket> SOCKETS = new ArrayList<>();
    private Set<String> uniqueComponents = new HashSet<>();
    private Set<String> usedFeature = new HashSet<>();
    private List<Page> pages = new ArrayList<>();



    private File fXmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatAADL-XML2.xml");
    private File pnmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatPetriNet-OutputTeeest2.xml");


    public void parseFile() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document loadedDocument = builder.parse(fXmlFile);

        loadedDocument.getDocumentElement().normalize();

        NodeList componentInstances = loadedDocument.getElementsByTagName("componentInstance");
        searchElements(componentInstances, null);
        moveProcesses();

        for (ComponentInstance cmpI : COMPONENT_INSTANCES) {
            System.out.println("Nazwa elementu " + cmpI.getName());
            System.out.println("Id" + cmpI.getId());
        }
        NodeList connections = loadedDocument.getElementsByTagName("connectionInstance");
        searchConnections(connections);
        generatePetriNet();


        System.out.println("Liczba elementow " + COMPONENT_INSTANCES.size());
    }

    private void moveProcesses() {
        for (int i = 0; i < COMPONENT_INSTANCES.size(); ++i) {
            if (COMPONENT_INSTANCES.get(i).getCategory().equals(Category.PROCESS.getValue())) {
                PROCESSES.add(COMPONENT_INSTANCES.get(i));
            }
        }
    }

    private void generatePetriNet() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        int numberPage = 0;
        Page actualPage = getPageByIndex(numberPage);

        Document pnmlDocument = builder.newDocument();

        Element workspaceElements = pnmlDocument.createElement("workspaceElements");
        addGeneratorInfo(pnmlDocument, workspaceElements);

        Element root = pnmlDocument.createElement("cpnet");
        workspaceElements.appendChild(root);
        pnmlDocument.appendChild(workspaceElements);

        generateGlobBox(pnmlDocument, root);

        //page startowy
        Element page = generateNewPage(actualPage.getPageId(), pnmlDocument, root);
        List<Node> arcs = generateConnections(actualPage.getContext(), pnmlDocument, page);
        translateElements(pnmlDocument, page, COMPONENT_INSTANCES);
        insertArcToPNet(page, arcs);
        GraphicPosition.resetPositions();


        //pageForProcess   zrobic odniesienia do stron!!
        for (ComponentInstance pageProcess : PROCESSES) {
            actualPage = getPageByIndex(++numberPage);
            Element pageForProcess = generateNewPage(actualPage.getPageId(), pnmlDocument, root);
            List<Node> arcs2 = generateConnections(actualPage.getContext(), pnmlDocument, pageForProcess);
            translateElements(pnmlDocument, pageForProcess, pageProcess.getComponentInstancesNested());
            insertArcToPNet(pageForProcess, arcs2);
            GraphicPosition.resetPositions();
        }

        Element instances = generatePagesInstances(pnmlDocument);
        root.appendChild(instances);

        Element binders = generateBinders(pnmlDocument);

        root.appendChild(binders);


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

    private Element generateBinders(Document pnmlDocument) {
        Element binders = pnmlDocument.createElement("binders");
        Element cpnBinder = pnmlDocument.createElement("cpnbinder");

        Attr binderIdAttr = pnmlDocument.createAttribute("id");
        binderIdAttr.setValue(ParserTools.generateUUID());

        Attr xAttr = pnmlDocument.createAttribute("x");
        xAttr.setValue("571");

        Attr yAttr = pnmlDocument.createAttribute("y");
        yAttr.setValue("94");

        Attr widthAttr = pnmlDocument.createAttribute("width");
        widthAttr.setValue("600");

        Attr heightAttr = pnmlDocument.createAttribute("height");
        heightAttr.setValue("400");

        cpnBinder.setAttributeNode(binderIdAttr);
        cpnBinder.setAttributeNode(xAttr);
        cpnBinder.setAttributeNode(yAttr);
        cpnBinder.setAttributeNode(widthAttr);
        cpnBinder.setAttributeNode(heightAttr);

        Element sheets = pnmlDocument.createElement("sheets");

        for (String cpnSheetInstance: INSTANCES_BINDERS) {
            Element cpnSheet = pnmlDocument.createElement("cpnsheet");
            Attr cpnSheetIdAttr = pnmlDocument.createAttribute("id");
            cpnSheetIdAttr.setValue(ParserTools.generateUUID());
            Attr panXAttr = pnmlDocument.createAttribute("panx");
            panXAttr.setValue("-6.000000");
            Attr panYAttr = pnmlDocument.createAttribute("pany");
            panYAttr.setValue("-5.000000");
            Attr zoomAttr = pnmlDocument.createAttribute("zoom");
            zoomAttr.setValue("0.910000");
            Attr instanceAttr = pnmlDocument.createAttribute("instance");
            instanceAttr.setValue(cpnSheetInstance);

            cpnSheet.setAttributeNode(cpnSheetIdAttr);
            cpnSheet.setAttributeNode(panXAttr);
            cpnSheet.setAttributeNode(panYAttr);
            cpnSheet.setAttributeNode(zoomAttr);
            cpnSheet.setAttributeNode(instanceAttr);

            Element zorder = pnmlDocument.createElement("zorder");
            Element zorderPosition = pnmlDocument.createElement("position");
            Attr zorderPostionValueAttr = pnmlDocument.createAttribute("value");
            zorderPostionValueAttr.setValue("0");
            zorderPosition.setAttributeNode(zorderPostionValueAttr);
            zorder.appendChild(zorderPosition);
            cpnSheet.appendChild(zorder);

            sheets.appendChild(cpnSheet);
        }

        cpnBinder.appendChild(sheets);

        Element zorder = pnmlDocument.createElement("zorder");
        Element zorderPosition = pnmlDocument.createElement("position");
        Attr zorderPostionValueAttr = pnmlDocument.createAttribute("value");
        zorderPostionValueAttr.setValue("0");
        zorderPosition.setAttributeNode(zorderPostionValueAttr);
        zorder.appendChild(zorderPosition);


        binders.appendChild(cpnBinder);
        return binders;
    }

    private Element generatePagesInstances(Document pnmlDocument) {
        Element instances = pnmlDocument.createElement("instances");
        Element firstInstance = pnmlDocument.createElement("instance");
        Integer numberPage = 0;
        Attr idAttr = pnmlDocument.createAttribute("id");
        String idAttrValue = ParserTools.generateUUID();
        idAttr.setValue(idAttrValue);
        INSTANCES_BINDERS.add(idAttrValue);
        Attr pageAttr = pnmlDocument.createAttribute("page");
        String firstPageId = getPageIdByIndex(numberPage);
        pageAttr.setValue(firstPageId);
        firstInstance.setAttributeNode(idAttr);
        firstInstance.setAttributeNode(pageAttr);
        instances.appendChild(firstInstance);
        for (Page page : pages.subList(1, pages.size())) {
            Element instance = pnmlDocument.createElement("instance");
            Attr newPageIdAttr = pnmlDocument.createAttribute("id");
            String newPageIdAttrValue = ParserTools.generateUUID();
            newPageIdAttr.setValue(newPageIdAttrValue);
            INSTANCES_BINDERS.add(newPageIdAttrValue);
            Attr newTransAttr = pnmlDocument.createAttribute("trans");
            newTransAttr.setValue(getTransIdByIndex(++numberPage));
            instance.setAttributeNode(newPageIdAttr);
            instance.setAttributeNode(newTransAttr);
            //wtedy na tym samym poziomie procesy
            firstInstance.appendChild(instance);
        }
        return instances;
    }

    private Page getPageByIndex(int numberPage) {
        return pages.get(numberPage);
    }

    private String getPageIdByIndex(int numberPage) {
        return getPageByIndex(numberPage).getPageId();
    }

    private String getPageForTransId(String transId) {
        return pages.stream().filter(e -> transId.equals(e.getTransId())).findFirst().get().getPageId();
    }

    private String getTransIdByIndex(int numberPage) {
        return getPageByIndex(numberPage).getTransId();
    }

    private void addGeneratorInfo(Document pnmlDocument, Element workspaceElements) {
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
    }

    private void generateGlobBox(Document pnmlDocument, Element root) {
        Element globbox = pnmlDocument.createElement("globbox");
        Element block = pnmlDocument.createElement("block");
        Attr attrId = pnmlDocument.createAttribute("id");
        attrId.setValue(ParserTools.generateUUID());
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
        attrId.setValue(ParserTools.generateUUID());
        block.setAttributeNode(attrId);
        Element idElement = pnmlDocument.createElement("id");
        idElement.setTextContent("Standard priorities");
        block.appendChild(idElement);


        generateSimpleType(pnmlDocument, block, "UNIT", "colset UNIT = unit");
        generateSimpleType(pnmlDocument, block, "BOOL", null);
        generateSimpleType(pnmlDocument, block, "INTINF", "colset INTINF = intinf;");
        generateSimpleType(pnmlDocument, block, "TIME", "colset TIME = time;");
        generateSimpleType(pnmlDocument, block, "REAL", "colset REAL = real;");
        generateSimpleType(pnmlDocument, block, "String", null);


        globbox.appendChild(block);
    }

    private void generateSimpleType(Document pnmlDocument, Element block, String colorId, String layoutText) {
        Element colorElement = pnmlDocument.createElement("color");
        Attr attrIdColor = pnmlDocument.createAttribute("id");
        attrIdColor.setValue(ParserTools.generateUUID());
        colorElement.setAttributeNode(attrIdColor);
        Element idColorElement = pnmlDocument.createElement("id");
        idColorElement.setTextContent(colorId);
        colorElement.appendChild(idColorElement);
        Element colorUnitElement = pnmlDocument.createElement(colorId.toLowerCase());
        colorElement.appendChild(colorUnitElement);
        if (layoutText != null) {
            Element layoutElement = pnmlDocument.createElement("layout");
            layoutElement.setTextContent(layoutText);
            colorElement.appendChild(layoutElement);
        }

        block.appendChild(colorElement);
    }

    private void createMlElement(Document pnmlDocument, Element block, String s) {
        Element mlElement = pnmlDocument.createElement("ml");
        Attr mlAttrId = pnmlDocument.createAttribute("id");
        mlAttrId.setValue(ParserTools.generateUUID());
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
        for (Connection connection : CONNECTIONS) {
            if (actualContext.equals(connection.getContext())) {

                ArrayList<Integer> source = preparePorts(connection.getSource());
                ArrayList<Integer> dst = preparePorts(connection.getDestination());

                ConnectionNode sourceNode = getConnectionNode(source, null, null);
                ConnectionNode dstNode = getConnectionNode(dst, null, null);

                Element arc1 = pnmlDocument.createElement("arc");
                Attr arcId = pnmlDocument.createAttribute("id");
                arcId.setValue(connection.getId());
                arc1.setAttributeNode(arcId);

                Element transend = pnmlDocument.createElement("transend");
                Attr transendIdRef = pnmlDocument.createAttribute("idref");

                Element placeend = pnmlDocument.createElement("placeend");
                Attr placeendIdRef = pnmlDocument.createAttribute("idref");
                Attr arcOrientation = pnmlDocument.createAttribute("orientation");

                setArcGraphicsProperties(pnmlDocument, arc1, connection.getPos_X(), connection.getPos_Y());

                Element arc2 = pnmlDocument.createElement("arc");
                Attr arcId2 = pnmlDocument.createAttribute("id");
                arcId2.setValue(connection.getId() + "#");
                arc2.setAttributeNode(arcId2);

                Element transend2 = pnmlDocument.createElement("transend");
                Attr transendIdRef2 = pnmlDocument.createAttribute("idref");


                Element placeend2 = pnmlDocument.createElement("placeend");
                Attr placeendIdRef2 = pnmlDocument.createAttribute("idref");
                Attr arcOrientation2 = pnmlDocument.createAttribute("orientation");


                if (Category.PROCESS.getValue().equals(dstNode.getHeadCategory()) && !dstNode.getCategory().equals(sourceNode.getCategory())) {
                    SOCKETS.add(new Socket(dstNode.getHeadId(), dstNode.getPlaceId(), sourceNode.getPlaceId(), "In"));
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(sourceNode.getPlaceId());
                    arcOrientation.setValue("TtoP");

                    transendIdRef2.setValue(dstNode.getHeadId());
                    placeendIdRef2.setValue(sourceNode.getPlaceId());
                    arcOrientation2.setValue("PtoT");

                    usedFeature.add(sourceNode.getPlaceId());
                }

                //dla wygenerowanego
                else if (Category.PROCESS.getValue().equals(sourceNode.getHeadCategory()) && !dstNode.getCategory().equals(sourceNode.getCategory()) && "Out".equals(connection.getSocketType())) {
                    SOCKETS.add(new Socket(sourceNode.getHeadId(), sourceNode.getPlaceId(), dstNode.getPlaceId(), "Out"));
                    transendIdRef.setValue(sourceNode.getHeadId());
                    placeendIdRef.setValue(dstNode.getPlaceId());
                    arcOrientation.setValue("TtoP");

                    transendIdRef2.setValue(dstNode.getTransId());
                    placeendIdRef2.setValue(dstNode.getPlaceId());
                    arcOrientation2.setValue("PtoT");

                    usedFeature.add(dstNode.getPlaceId());
                } else if (Boolean.TRUE.equals(connection.getGenerate()) && "In".equals(connection.getSocketType())) {
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(sourceNode.getPlaceId());
                    arcOrientation.setValue("PtoT");
                    usedFeature.add(sourceNode.getPlaceId());

                } else if (Boolean.FALSE.equals(connection.getGenerate()) && connection.getSocketType() == null && !sourceNode.getCategory().equals(Category.BUS.getValue())) {
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(sourceNode.getPlaceId());
                    arcOrientation.setValue("TtoP");
                    //czy tak może byc?? Device jako odpowiednik komponentu z wyzszej warstwy
                    if (isFirstLayer(dstNode.getCategory())) {
                        transendIdRef2.setValue(dstNode.getTransId());
                        placeendIdRef2.setValue(sourceNode.getPlaceId());
                        arcOrientation2.setValue("PtoT");
                    }

                    usedFeature.add(sourceNode.getPlaceId());
                } else if (sourceNode.getCategory().equals(Category.BUS.getValue())) {
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(dstNode.getPlaceId());
                    usedFeature.add(dstNode.getPlaceId()); // dodane
                    arcOrientation.setValue("TtoP");

                    usedFeature.add(dstNode.getPlaceId());

                } else {
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(sourceNode.getPlaceId()); //było sourceNode.getPlaceId jak chcemy miejsce z wyjsciowego
                    arcOrientation.setValue("TtoP");

                    transendIdRef2.setValue(dstNode.getTransId());
                    placeendIdRef2.setValue(sourceNode.getPlaceId());
                    arcOrientation2.setValue("PtoT");

                    usedFeature.add(sourceNode.getPlaceId());  // było sourceNode.getPlaceId jak chcemy miejsce z wyjsciowego
                }
                transend.setAttributeNode(transendIdRef);
                placeend.setAttributeNode(placeendIdRef);
                arc1.setAttributeNode(arcOrientation);
                arc1.appendChild(transend);
                arc1.appendChild(placeend);
                arcs.add(arc1);

                if (!"".equals(transendIdRef2.getValue()) && !"".equals(placeendIdRef2.getValue())) {
                    transend2.setAttributeNode(transendIdRef2);
                    placeend2.setAttributeNode(placeendIdRef2);
                    arc2.setAttributeNode(arcOrientation2);
                    setArcGraphicsProperties(pnmlDocument, arc2, connection.getPos_X(), connection.getPos_Y());
                    arc2.appendChild(transend2);
                    arc2.appendChild(placeend2);
                    arcs.add(arc2);
                }

            }

        }
        return arcs;

    }

    private boolean isFirstLayer(String category) {
        return !Category.PROCESS.equals(category) && !Category.DEVICE.getValue().equals(category);
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

    private void translateElements(Document pnmlDocument, Element page, List<ComponentInstance> componentInstances) {
        for (ComponentInstance componentInstance : componentInstances) {
            String componentInstanceCategory = componentInstance.getCategory();
            if (componentInstanceCategory.equals(Category.DEVICE.getValue()) || componentInstanceCategory.equals(Category.PROCESS.getValue()) || componentInstanceCategory.equals(Category.THREAD.getValue())) {
                Element transition = generateTransition(pnmlDocument, "trans", componentInstance);
                page.appendChild(transition);
            }
            if (componentInstanceCategory.equals(Category.BUS.getValue())) {
                Element transition = generateTransition(pnmlDocument, "trans", componentInstance);
                page.appendChild(transition);
            }
            List<FeatureInstance> featureInstances = componentInstance.getFeatureInstance();
            for (FeatureInstance feature : featureInstances) {
                Element place = generatePlace(pnmlDocument, feature);
                if (usedFeature.contains(feature.getId())) { // unikalnośc miejsc
                    page.appendChild(place);
                }

            }

        }
        usedFeature.clear();
    }

    private Element generateNewPage(String pageId, Document pnmlDocument, Element root) {
        Element page = pnmlDocument.createElement("page");
        Attr pageIdAttr = pnmlDocument.createAttribute("id");
        pageIdAttr.setValue(pageId);
        page.setAttributeNode(pageIdAttr);
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
        Double placeXPosition = GraphicPosition.getPLACE_X_POSITION();
        positionX.setValue(placeXPosition.toString());
        Attr positionY = pnmlDocument.createAttribute("y");
        Double placeYPosition = GraphicPosition.getPLACE_Y_POSITION();
        positionY.setValue(placeYPosition.toString());
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


        Socket socket = isConnectingPort(featureInstance);

        if (socket != null) {

            Element port = pnmlDocument.createElement("port");
            Attr attrId = pnmlDocument.createAttribute("id");
            attrId.setValue(ParserTools.generateUUID());
            port.setAttributeNode(attrId);
            Attr attrType = pnmlDocument.createAttribute("type");
            attrType.setValue(socket.getDirection());
            port.setAttributeNode(attrType);

            Element portPosition = pnmlDocument.createElement("posattr");
            Attr portPositionX = pnmlDocument.createAttribute("x");
            Double portPositionXValue = placeXPosition - 24.0000;
            portPositionX.setValue(portPositionXValue.toString());
            Attr portPositionY = pnmlDocument.createAttribute("y");
            Double portPositionYValue = placeYPosition - 16.0000;
            portPositionY.setValue(portPositionYValue.toString());
            portPosition.setAttributeNode(portPositionX);
            portPosition.setAttributeNode(portPositionY);

            port.appendChild(portPosition);


            Element portFillProperty = pnmlDocument.createElement("fillattr");
            Attr portColorFill = pnmlDocument.createAttribute("colour");
            portColorFill.setValue("White");
            portFillProperty.setAttributeNode(portColorFill);
            Attr portPattern = pnmlDocument.createAttribute("pattern");
            portPattern.setValue("Solid");
            portFillProperty.setAttributeNode(portPattern);
            Attr portFilled = pnmlDocument.createAttribute("filled");
            portPattern.setValue("false");
            portFillProperty.setAttributeNode(portFilled);
            port.appendChild(portFillProperty);


            Element portLineProperty = pnmlDocument.createElement("lineattr");
            Attr portColorLine = pnmlDocument.createAttribute("colour");
            portColorLine.setValue("Black");
            portLineProperty.setAttributeNode(portColorLine);
            Attr portThick = pnmlDocument.createAttribute("thick");
            portThick.setValue("0");
            portLineProperty.setAttributeNode(portThick);
            Attr portType = pnmlDocument.createAttribute("type");
            portType.setValue("Solid");
            portLineProperty.setAttributeNode(portType);
            port.appendChild(portLineProperty);


            Element portTextProperty = pnmlDocument.createElement("textattr");
            Attr portColorText = pnmlDocument.createAttribute("colour");
            portColorText.setValue("Black");
            portTextProperty.setAttributeNode(portColorText);
            Attr portIsBold = pnmlDocument.createAttribute("bold");
            portIsBold.setValue("false");
            portTextProperty.setAttributeNode(portIsBold);
            port.appendChild(portTextProperty);


            place.appendChild(port);
        }

        return place;
    }

    private Socket isConnectingPort(FeatureInstance featureInstance) {
        for (int i = 0; i < SOCKETS.size(); ++i) {
            Socket socket = SOCKETS.get(i);
            if (featureInstance.getId().equals(socket.getPortId()) || featureInstance.getId().equals(socket.getSocketId())) {
                return socket;
            }
        }
        return null;
    }

    private Element generateTransition(Document pnmlDocument, String trans, ComponentInstance componentInstance) {
        Element transition = pnmlDocument.createElement(trans);

        Attr transitionId = pnmlDocument.createAttribute("id");
        transitionId.setValue(componentInstance.getId());
        transition.setAttributeNode(transitionId);

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

        if (Category.PROCESS.getValue().equals(componentInstance.getCategory())) {
            Element substElement = pnmlDocument.createElement("subst");
            Attr subpageAttr = pnmlDocument.createAttribute("subpage");
            String pageId = getPageForTransId(componentInstance.getId());
            subpageAttr.setValue(pageId);
            substElement.setAttributeNode(subpageAttr);
            Attr portSock = pnmlDocument.createAttribute("portsock");
            StringBuilder portSockValue = new StringBuilder();
            for (Socket socket : SOCKETS) {
                if (componentInstance.getId().equals(socket.getComponentId())) {
                    portSockValue.append("(" + socket.getPortId() + "," + socket.getSocketId() + ")");
                }
            }
            portSock.setValue(portSockValue.toString());
            substElement.setAttributeNode(portSock);


            Element subpageElement = pnmlDocument.createElement("subpageinfo");
            Attr subpageAttrId = pnmlDocument.createAttribute("id");
            subpageAttrId.setValue(ParserTools.generateUUID());
            subpageElement.setAttributeNode(subpageAttrId);


            Element subPageTransitionPosition = pnmlDocument.createElement("posattr");
            Attr subPageTransitionPositionX = pnmlDocument.createAttribute("x");
            Double subpagePositionX = componentInstance.getPos_X() - 24.0000;
            Double subpagePositionY = componentInstance.getPos_Y() - 16.0000;
            subPageTransitionPositionX.setValue(subpagePositionX.toString());
            Attr subPageTransitionPositionY = pnmlDocument.createAttribute("y");
            subPageTransitionPositionY.setValue(subpagePositionY.toString());
            subPageTransitionPosition.setAttributeNode(subPageTransitionPositionX);
            subPageTransitionPosition.setAttributeNode(subPageTransitionPositionY);
            subpageElement.appendChild(subPageTransitionPosition);


            Element subPageTransitionFillProperty = pnmlDocument.createElement("fillattr");
            Attr subPageTransitionColorFill = pnmlDocument.createAttribute("colour");
            subPageTransitionColorFill.setValue("White");
            subPageTransitionFillProperty.setAttributeNode(subPageTransitionColorFill);
            Attr subPageTransitionPattern = pnmlDocument.createAttribute("pattern");
            subPageTransitionPattern.setValue("Solid");
            subPageTransitionFillProperty.setAttributeNode(subPageTransitionPattern);
            Attr subPageTransitionFilled = pnmlDocument.createAttribute("filled");
            subPageTransitionFilled.setValue("false");
            subPageTransitionFillProperty.setAttributeNode(subPageTransitionFilled);
            subpageElement.appendChild(subPageTransitionFillProperty);


            Element subPageTransitionLineProperty = pnmlDocument.createElement("lineattr");
            Attr subPageTransitionColorLine = pnmlDocument.createAttribute("colour");
            subPageTransitionColorLine.setValue("Black");
            subPageTransitionLineProperty.setAttributeNode(subPageTransitionColorLine);
            Attr subPageTransitionThick = pnmlDocument.createAttribute("thick");
            subPageTransitionThick.setValue("0");
            subPageTransitionLineProperty.setAttributeNode(subPageTransitionThick);
            Attr subPageTransitionLineAttrType = pnmlDocument.createAttribute("type");
            subPageTransitionLineAttrType.setValue("Solid");
            subPageTransitionLineProperty.setAttributeNode(subPageTransitionLineAttrType);
            subpageElement.appendChild(subPageTransitionLineProperty);

            Element subPageTransitionTextProperty = pnmlDocument.createElement("textattr");
            Attr subPageTransitionColorText = pnmlDocument.createAttribute("colour");
            subPageTransitionColorText.setValue("Black");
            subPageTransitionTextProperty.setAttributeNode(subPageTransitionColorText);
            Attr subPageTransitionTextAttrBold = pnmlDocument.createAttribute("bold");
            subPageTransitionTextAttrBold.setValue("false");
            subPageTransitionTextProperty.setAttributeNode(subPageTransitionTextAttrBold);
            subpageElement.appendChild(subPageTransitionTextProperty);

            substElement.appendChild(subpageElement);

            transition.appendChild(substElement);
        }


        return transition;
    }

    private String setArcOrientation(ConnectionNode sourceNode, ConnectionNode dstNode) {
        if (sourceNode.getCategory().equals(Category.FEATURE.getValue())) {
            return "PtoT";
        } else {
            return "TtoP";
        }

    }

    private ConnectionNode getConnectionNode(List<Integer> path, ComponentInstance actualComponentInstance, ComponentInstance headComponent) {

        for (int j = 0; j < path.size(); ++j) {
            ComponentInstance processingComponent = actualComponentInstance != null ? actualComponentInstance : COMPONENT_INSTANCES.get(path.get(j));
            if (j == path.size() - 1) {
                return new ConnectionNode(processingComponent.getId(), null, processingComponent.getCategory(), null, null);
            } else if (j == path.size() - 2) {
                String headComponentId = headComponent != null ? headComponent.getId() : null;
                String headCategory = headComponent != null ? headComponent.getCategory() : null;

                return new ConnectionNode(processingComponent.getId(), processingComponent.getFeatureInstance().get(path.get(j + 1)).getId(), processingComponent.getCategory(), headComponentId, headCategory);
            } else {
                return getConnectionNode(path.subList(j + 1, path.size()), processingComponent.getComponentInstancesNested().get(path.get(j + 1)), processingComponent);
            }
        }
        return null;
    }

    private ArrayList<Integer> preparePorts(String source) {
        String[] sourceSplitted = source.split(" ");
        ArrayList<Integer> sourceList = new ArrayList<>();
        for (String element : sourceSplitted) {
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
            NodeList connectionReferences = actualConnection.getElementsByTagName("connectionReference");
            String contextRaw = connectionReferences.item(0).getAttributes().getNamedItem("context").getNodeValue();
            System.out.println("context of  connection : " + contextRaw);

            System.out.println("source of  connection : " + actualConnection.getAttribute("source"));
            System.out.println("destination of  connection : " + actualConnection.getAttribute("destination"));
            System.out.println("destination of  connection : " + actualConnection.getAttribute("destination"));

            String context = contextRaw.replaceAll("\\D+", " ").trim();
            String source = actualConnection.getAttribute("source").replaceAll("\\D+", " ").trim();
            String destination = actualConnection.getAttribute("destination").replaceAll("\\D+", " ").trim();

            Connection newConnection = new Connection(context, source, destination);


            ArrayList<Integer> destinationPath = preparePorts(destination);
            ArrayList<Integer> sourcePath = preparePorts(source);


            //dodanie połaczenia jesli to jest socket In wrzucenie na podstrone
            if (destinationPath.get(0) != null && Category.PROCESS.getValue().equals(COMPONENT_INSTANCES.get(destinationPath.get(0)).getCategory()) && !Category.PROCESS.getValue().equals(COMPONENT_INSTANCES.get(sourcePath.get(0)).getCategory())) {
                String additionalConnContext = destinationPath.get(0).toString();
                String additionalConnSource = destination;
                String additionalConnDestination = destination.substring(0, destination.length() - 1);
                Connection additionalConnConnection = new Connection(additionalConnContext, additionalConnSource, additionalConnDestination);
                additionalConnConnection.setGenerate(Boolean.TRUE);
                additionalConnConnection.setSocketType("In");
                ConnectionNode connectionNode = getConnectionNode(destinationPath, null, null);
                addNewPage(context, COMPONENT_INSTANCES.get(destinationPath.get(0)).getId());

                CONNECTIONS.add(additionalConnConnection);
            }
            //dodanie połaczenia jesli to jest socket Out
            else if (sourcePath.get(0) != null && Category.PROCESS.getValue().equals(COMPONENT_INSTANCES.get(sourcePath.get(0)).getCategory()) && !Category.PROCESS.getValue().equals(COMPONENT_INSTANCES.get(destinationPath.get(0)).getCategory())) {

                //kolejna zaślepka az sie zrobie page i context!!!!!1
                String additionalConnContext = "";
                String additionalConnSource = source;
                String additionalConnDestination = destination;
                Connection additionalConnConnection = new Connection(additionalConnContext, additionalConnSource, additionalConnDestination);
                additionalConnConnection.setGenerate(Boolean.TRUE);
                additionalConnConnection.setSocketType("Out");
                ConnectionNode connectionNode = getConnectionNode(sourcePath, null, null);
                addNewPage(context, COMPONENT_INSTANCES.get(sourcePath.get(0)).getId());
                CONNECTIONS.add(additionalConnConnection);
            }

            CONNECTIONS.add(newConnection);

        }
        CONNECTIONS.sort(Comparator.comparing(Connection::getContext));

    }

    private void addNewPage(String context, String transId) {
        long count = pages.stream().filter(e -> e.getContext().equals(context)).count();
        if (count == 0) {
            Page newPage = new Page(context);
            if (!"".equals(context)) {
                newPage.setTransId(transId);
            }
            pages.add(newPage);
        }
        Collections.sort(pages);
    }


    private void searchElements(NodeList componenentInstances, ComponentInstance processingElement) {
        for (int i = 0; i < componenentInstances.getLength(); i++) {

            Node component = componenentInstances.item(i);
            System.out.println("\nCurrent Element :" + component.getNodeName());

            if (component.getNodeType() == Node.ELEMENT_NODE) {
                ComponentInstance componentInstance;
                Element actualComponent = (Element) component;
                if (processingElement != null) {
                    componentInstance = processingElement;

                } else {
                    componentInstance = new ComponentInstance(actualComponent.getAttribute("name"), actualComponent.getAttribute("category"));
                    // uniqueFeature.clear();
                }

                if (uniqueComponents.contains(actualComponent.getAttribute("name"))) {
                    continue;
                }

                ComponentInstance componentInstanceNested = processingElement != null ? new ComponentInstance(actualComponent.getAttribute("name"), actualComponent.getAttribute("category")) : null;

                NodeList featureInstances = actualComponent.getElementsByTagName("featureInstance");

                for (int j = 0; j < featureInstances.getLength(); j++) {
                    Node featureInstance = featureInstances.item(j);
                    Element featureElement = (Element) featureInstance;
                    System.out.println("Name of feature : " + featureElement.getAttribute("name"));
                    if (componentInstanceNested != null) {
                        componentInstance.getReverseFeatureInstances().remove(new FeatureInstance(featureElement.getAttribute("name"), featureElement.getAttribute("direction")));
                        componentInstance.getReverseFeatureInstances();//wroc do starego porzadku
                        componentInstanceNested.getFeatureInstance().add(new FeatureInstance(featureElement.getAttribute("name"), featureElement.getAttribute("direction")));
                        //uniqueFeature.remove(featureElement.getAttribute("name"));
                    } else {
                        // uniqueFeature.add(featureElement.getAttribute("name"));
                        componentInstance.getFeatureInstance().add(new FeatureInstance(featureElement.getAttribute("name"), featureElement.getAttribute("direction")));
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

class ConnectionNode {

    private String transId;
    private String placeId;
    private String category;
    private String headId;
    private String headCategory;

    public ConnectionNode(String transId, String placeId, String category, String headId, String headCategory) {
        this.transId = transId;
        this.placeId = placeId;
        this.category = category;
        this.headId = headId;
        this.headCategory = headCategory;
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

    public String getHeadId() {
        return headId;
    }

    public String getHeadCategory() {
        return headCategory;
    }
}

class Socket {
    private final String componentId;
    private final String portId;
    private final String socketId;
    private final String direction;

    public Socket(String componentId, String portId, String socketId, String direction) {
        this.componentId = componentId;
        this.portId = portId;
        this.socketId = socketId;
        this.direction = direction;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getPortId() {
        return portId;
    }

    public String getSocketId() {
        return socketId;
    }

    public String getDirection() {
        return direction;
    }
}

class Page implements Comparable<Page> {
    private String context;
    private String pageId;
    private String transId;

    public Page(String context) {
        this.context = context;
        this.pageId = ParserTools.generateUUID();
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }


    @Override
    public int compareTo(Page o) {
        return this.getContext().compareTo(o.getContext());
    }
}