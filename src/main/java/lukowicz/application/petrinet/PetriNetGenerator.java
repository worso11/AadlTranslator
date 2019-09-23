package lukowicz.application.petrinet;

import lukowicz.application.aadl.ElementSearcher;
import lukowicz.application.data.*;
import lukowicz.application.memory.Cache;
import lukowicz.application.utils.ParserTools;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


//is first layer metoda jak to dziala
public class PetriNetGenerator {

    private PetriGraphicsGenerator petriGraphicsGenerator = new PetriGraphicsGenerator();
    private PetriNetTranslator petriNetTranslator = new PetriNetTranslator();
    private ElementSearcher elementSearcher = new ElementSearcher();

    private File petriNetXmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatPetriNet-OutputTeeest2.xml");


    public void generatePetriNet() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        int numberPage = 0;
        Page actualPage = Cache.getPageByIndex(numberPage);

        Document pnmlDocument = builder.newDocument();

        Element workspaceElements = pnmlDocument.createElement("workspaceElements");
        petriGraphicsGenerator.addGeneratorInfo(pnmlDocument, workspaceElements);

        Element root = pnmlDocument.createElement("cpnet");
        workspaceElements.appendChild(root);
        pnmlDocument.appendChild(workspaceElements);

        petriGraphicsGenerator.generateGlobBox(pnmlDocument, root);

        //page startowy
        Element page = Cache.generateNewPage(actualPage.getPageId(), pnmlDocument, root);
        List<Node> arcs = generateConnections(actualPage.getContext(), pnmlDocument, page);
        petriNetTranslator.translateElements(pnmlDocument, page, Cache.getComponentInstances());
        insertArcToPNet(page, arcs);
        GraphicPosition.resetPositions();


        //pageForProcess   zrobic odniesienia do stron!!
        for (ComponentInstance pageProcess : Cache.getPROCESSES()) {
            actualPage = Cache.getPageByIndex(++numberPage);
            Element pageForProcess = Cache.generateNewPage(actualPage.getPageId(), pnmlDocument, root);
            List<Node> arcs2 = generateConnections(actualPage.getContext(), pnmlDocument, pageForProcess);
            petriNetTranslator.translateElements(pnmlDocument, pageForProcess, pageProcess.getComponentInstancesNested());
            insertArcToPNet(pageForProcess, arcs2);
            GraphicPosition.resetPositions();
        }

        Element instances = Cache.generatePagesInstances(pnmlDocument);
        root.appendChild(instances);

        Element binders = petriGraphicsGenerator.generateBinders(pnmlDocument);

        root.appendChild(binders);
        workspaceElements.appendChild(root);

        saveFile(pnmlDocument);


    }

    private void saveFile(Document pnmlDocument) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource domSource = new DOMSource(pnmlDocument);
        StreamResult streamResult = new StreamResult(new File(String.valueOf(petriNetXmlFile)));


        transformer.transform(domSource, streamResult);

        System.out.println("Done creating XML File");
    }

    private List<Node> generateConnections(String actualContext, Document pnmlDocument, Element page) {
        List<Node> arcs = new ArrayList<>();
        Set<String> threadsId = new HashSet<>();
        for (Connection connection : Cache.getCONNECTIONS()) {
            if (actualContext.equals(connection.getContext())) {

                ArrayList<Integer> source = ParserTools.preparePorts(connection.getSource());
                ArrayList<Integer> dst = ParserTools.preparePorts(connection.getDestination());

                ConnectionNode sourceNode = elementSearcher.getConnectionNode(source, null, null);
                ConnectionNode dstNode = elementSearcher.getConnectionNode(dst, null, null);

                if(dstNode.getPeriod() != null && Category.THREAD.getValue().equals(sourceNode.getCategory())){
                    if(!threadsId.contains(sourceNode.getTransId())) {
                        DataPort waitingPlace = new DataPort("Wait", "");
                        Element waitingInPlaceArc = createWaitingPlaceArc(pnmlDocument, sourceNode.getTransId(), waitingPlace.getId(),"PtoT");
                        Element waitingOutPlaceArc = createWaitingPlaceArc(pnmlDocument,sourceNode.getTransId(), waitingPlace.getId(),"TtoP");
                        petriGraphicsGenerator.setArcGraphicsProperties(pnmlDocument, waitingInPlaceArc, "0","0");
                        petriGraphicsGenerator.setArcGraphicsProperties(pnmlDocument, waitingOutPlaceArc, "0.8","0.8");
                        arcs.add(waitingInPlaceArc);
                        arcs.add(waitingOutPlaceArc);

                        threadsId.add(sourceNode.getTransId());
                        Cache.getGeneratedPlaces().add(waitingPlace);
                    }

                }

                Element arc1 = pnmlDocument.createElement("arc");
                Attr arcId = pnmlDocument.createAttribute("id");
                arcId.setValue(connection.getId());
                arc1.setAttributeNode(arcId);

                Element transend = pnmlDocument.createElement("transend");
                Attr transendIdRef = pnmlDocument.createAttribute("idref");

                Element placeend = pnmlDocument.createElement("placeend");
                Attr placeendIdRef = pnmlDocument.createAttribute("idref");
                Attr arcOrientation = pnmlDocument.createAttribute("orientation");

                petriGraphicsGenerator.setArcGraphicsProperties(pnmlDocument, arc1, connection.getPos_X(), connection.getPos_Y());

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
                    Cache.getSOCKETS().add(new Socket(dstNode.getHeadId(), dstNode.getPlaceId(), sourceNode.getPlaceId(), "In"));
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(sourceNode.getPlaceId());
                    arcOrientation.setValue("TtoP");

                    transendIdRef2.setValue(dstNode.getHeadId());
                    placeendIdRef2.setValue(sourceNode.getPlaceId());
                    arcOrientation2.setValue("PtoT");

                    Cache.getUsedFeature().add(sourceNode.getPlaceId());
                }

                //dla wygenerowanego
                else if (Category.PROCESS.getValue().equals(sourceNode.getHeadCategory()) && !dstNode.getCategory().equals(sourceNode.getCategory()) && "Out".equals(connection.getSocketType())) {
                    Cache.getSOCKETS().add(new Socket(sourceNode.getHeadId(), sourceNode.getPlaceId(), dstNode.getPlaceId(), "Out"));
                    transendIdRef.setValue(sourceNode.getHeadId());
                    placeendIdRef.setValue(dstNode.getPlaceId());
                    arcOrientation.setValue("TtoP");

                    transendIdRef2.setValue(dstNode.getTransId());
                    placeendIdRef2.setValue(dstNode.getPlaceId());
                    arcOrientation2.setValue("PtoT");

                    Cache.getUsedFeature().add(dstNode.getPlaceId());
                } else if (Boolean.TRUE.equals(connection.getGenerate()) && "In".equals(connection.getSocketType())) {
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(sourceNode.getPlaceId());
                    arcOrientation.setValue("PtoT");
                    Cache.getUsedFeature().add(sourceNode.getPlaceId());

                } else if (Boolean.FALSE.equals(connection.getGenerate()) && connection.getSocketType() == null && !sourceNode.getCategory().equals(Category.BUS.getValue())) {
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(sourceNode.getPlaceId());
                    arcOrientation.setValue("TtoP");
                    //czy tak może byc?? Device jako odpowiednik komponentu z wyzszej warstwy
                    if (!isFirstLayer(dstNode.getCategory())) {
                        transendIdRef2.setValue(dstNode.getTransId());
                        placeendIdRef2.setValue(sourceNode.getPlaceId());
                        arcOrientation2.setValue("PtoT");
                    }

                    Cache.getUsedFeature().add(sourceNode.getPlaceId());
                } else if (sourceNode.getCategory().equals(Category.BUS.getValue())) {
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(dstNode.getPlaceId());
                    Cache.getUsedFeature().add(dstNode.getPlaceId()); // dodane
                    arcOrientation.setValue("TtoP");

                    Cache.getUsedFeature().add(dstNode.getPlaceId());

                } else {
                    transendIdRef.setValue(sourceNode.getTransId());
                    placeendIdRef.setValue(sourceNode.getPlaceId()); //było sourceNode.getPlaceId jak chcemy miejsce z wyjsciowego
                    arcOrientation.setValue("TtoP");

                    transendIdRef2.setValue(dstNode.getTransId());
                    placeendIdRef2.setValue(sourceNode.getPlaceId());
                    arcOrientation2.setValue("PtoT");

                    Cache.getUsedFeature().add(sourceNode.getPlaceId());  // było sourceNode.getPlaceId jak chcemy miejsce z wyjsciowego
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
                    petriGraphicsGenerator.setArcGraphicsProperties(pnmlDocument, arc2, connection.getPos_X(), connection.getPos_Y());
                    arc2.appendChild(transend2);
                    arc2.appendChild(placeend2);
                    arcs.add(arc2);
                }

            }

        }
        return arcs;

    }

    private Element createWaitingPlaceArc(Document pnmlDocument, String transId, String placeId, String orientation) {
        Element arc1 = pnmlDocument.createElement("arc");
        Attr arcId = pnmlDocument.createAttribute("id");
        arcId.setValue(ParserTools.generateUUID());
        arc1.setAttributeNode(arcId);

        Element transend = pnmlDocument.createElement("transend");
        Attr transendIdRef = pnmlDocument.createAttribute("idref");

        Element placeend = pnmlDocument.createElement("placeend");
        Attr placeendIdRef = pnmlDocument.createAttribute("idref");
        Attr arcOrientation = pnmlDocument.createAttribute("orientation");

        transendIdRef.setValue(transId);
        placeendIdRef.setValue(placeId);
        arcOrientation.setValue(orientation);

        transend.setAttributeNode(transendIdRef);
        placeend.setAttributeNode(placeendIdRef);
        arc1.setAttributeNode(arcOrientation);
        arc1.appendChild(transend);
        arc1.appendChild(placeend);
        return arc1;
    }

    private void insertArcToPNet(Element page, List<Node> arcs) {
        for (Node arc : arcs) {
            page.appendChild(arc);
        }
    }

    private boolean isFirstLayer(String category) {
        return Category.PROCESS.getValue().equals(category) || Category.DEVICE.getValue().equals(category);
    }


}
