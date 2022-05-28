package lukowicz.application.petrinet;

import lukowicz.application.aadl.ElementSearcher;
import lukowicz.application.data.*;
import lukowicz.application.memory.Cache;
import lukowicz.application.memory.ElementsPosition;
import lukowicz.application.utils.TranslatorTools;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//is first layer metoda jak to dziala
public class PetriNetGenerator {

    private PetriNetGraphicsGenerator petriNetGraphicsGenerator;
    private PetriNetTranslator petriNetTranslator;
    private ElementSearcher elementSearcher;
    private PetriNetPager petriNetPager;
    private Cache cache = Cache.getInstance();


    public PetriNetGenerator(PetriNetGraphicsGenerator petriNetGraphicsGenerator, PetriNetTranslator petriNetTranslator, ElementSearcher elementSearcher, PetriNetPager petriNetPager) {
        this.petriNetGraphicsGenerator = petriNetGraphicsGenerator;
        this.petriNetTranslator = petriNetTranslator;
        this.elementSearcher = elementSearcher;
        this.petriNetPager = petriNetPager;

    }

    public void generatePetriNet(String outputFilePath) throws ParserConfigurationException, TransformerException, IOException {
        Document petriNetDocument = TranslatorTools.createDocumentFile();

        Element workspaceElements = petriNetDocument.createElement("workspaceElements");
        petriNetGraphicsGenerator.addGeneratorInfo(petriNetDocument, workspaceElements);

        Element root = petriNetDocument.createElement("cpnet");
        workspaceElements.appendChild(root);
        petriNetDocument.appendChild(workspaceElements);

        petriNetGraphicsGenerator.generateGlobBox(petriNetDocument, root);

        String generalPageId = TranslatorTools.generateUUID();

        Element generalTransistion = petriNetTranslator.insertGeneralTransition(petriNetDocument);
        String generalTransId = generalTransistion.getAttribute("id");
        Element generalPage = petriNetPager.generateNewPage(generalPageId, petriNetDocument, root, "General");
        generalPage.appendChild(generalTransistion);
        Page generalSystemPage = new Page("General_System", Boolean.TRUE, "", "General_System");
        generalSystemPage.setPageId(generalPageId);
        generalSystemPage.setTransId(generalTransId);
        petriNetPager.getPages().add(generalSystemPage);
        ElementsPosition.resetPositions();


        Page actualPage = petriNetPager.getPageByContext("");
        Element page = petriNetPager.generateNewPage(actualPage.getPageId(), petriNetDocument, root, "System");
        List<Node> arcs = generateConnections(actualPage.getContext(), petriNetDocument, page);
        petriNetTranslator.translateElements(petriNetDocument, page, cache.getComponentInstances());
        insertArcToPNet(page, arcs);
        ElementsPosition.resetPositions();

        cache.moveProcesses();

        for (ComponentInstance pageProcess : cache.getHIERARCHY_TRANSITIONS()) {
            actualPage = petriNetPager.getPageForTransId(pageProcess.getId());
            Element pageForProcess = petriNetPager.generateNewPage(actualPage.getPageId(), petriNetDocument, root, actualPage.getPageName());
            List<Node> arcs2;
            if (!actualPage.getGenerated()) {
                arcs2 = generateConnections(actualPage.getContext(), petriNetDocument, pageForProcess);
            } else {
                arcs2 = generateConnectionsForGeneratedPage(actualPage.getContext(), petriNetDocument, pageForProcess);
            }
            petriNetTranslator.translateElements(petriNetDocument, pageForProcess, pageProcess.getComponentInstancesNested());
            insertArcToPNet(pageForProcess, arcs2);
            ElementsPosition.resetPositions();
        }

        Element instances = petriNetPager.generatePagesInstances(petriNetDocument);
        Element binders = petriNetGraphicsGenerator.generateBinders(petriNetDocument);

        root.appendChild(instances);
        root.appendChild(binders);

        workspaceElements.appendChild(root);

        outputFilePath = outputFilePath + "\\generatedPetriNetFile.xml";

        File petriNetFile = new File(outputFilePath);
        TranslatorTools.saveFile(petriNetDocument, petriNetFile);
    }

    private List<Node> generateConnectionsForGeneratedPage(String context, Document pnmlDocument, Element pageForProcess) {
        List<Node> arcs = new ArrayList<>();

        for (Connection connection : cache.getCONNECTIONS()) {
            if (context.equals(connection.getContext())) {
                Element transend = pnmlDocument.createElement("transend");
                Attr transendIdRef = pnmlDocument.createAttribute("idref");

                Element placeend = pnmlDocument.createElement("placeend");
                Attr placeendIdRef = pnmlDocument.createAttribute("idref");
                Attr arcOrientation = pnmlDocument.createAttribute("orientation");
                Element arc1 = pnmlDocument.createElement("arc");
                Attr arcId = pnmlDocument.createAttribute("id");
                arcId.setValue(connection.getId());
                arc1.setAttributeNode(arcId);
                String directionArc = "in".equalsIgnoreCase(connection.getSocketType()) ? "PtoT" : "TtoP";
                setArcNodes(transendIdRef, placeendIdRef, arcOrientation, connection.getDestination(), connection.getSource(), directionArc);
                transend.setAttributeNode(transendIdRef);
                placeend.setAttributeNode(placeendIdRef);
                arc1.setAttributeNode(arcOrientation);

                arc1.appendChild(transend);
                arc1.appendChild(placeend);

                cache.getUsedFeature().add(connection.getSource());
                cache.getUsedFeature().add(connection.getDestination());

                petriNetGraphicsGenerator.setArcGraphicsProperties(pnmlDocument, arc1, connection.getPeriodArc());
                arcs.add(arc1);
            }
        }
        return arcs;
    }

    private List<Node> generateConnections(String actualContext, Document pnmlDocument, Element page) {
        List<Node> arcs = new ArrayList<>();
        for (Connection connection : cache.getCONNECTIONS()) {
            if (actualContext.equals(connection.getContext())) {

                ArrayList<Integer> source = TranslatorTools.preparePorts(connection.getSource());
                ArrayList<Integer> dst = TranslatorTools.preparePorts(connection.getDestination());

                ConnectionNode sourceNode = elementSearcher.getConnectionNode(source, null, null);
                ConnectionNode dstNode = elementSearcher.getConnectionNode(dst, null, null);

                Element arc1 = pnmlDocument.createElement("arc");
                Attr arcId = pnmlDocument.createAttribute("id");
                arcId.setValue(connection.getId());
                arc1.setAttributeNode(arcId);

                Element transend = pnmlDocument.createElement("transend");
                Attr transendIdRef = pnmlDocument.createAttribute("idref");

                Element placeend = pnmlDocument.createElement("placeend");
                Attr placeendIdRef = pnmlDocument.createAttribute("idref");
                Attr arcOrientation = pnmlDocument.createAttribute("orientation");

                petriNetGraphicsGenerator.setArcGraphicsProperties(pnmlDocument, arc1, connection.getPeriodArc());

                Element arc2 = pnmlDocument.createElement("arc");
                Attr arcId2 = pnmlDocument.createAttribute("id");
                arcId2.setValue(connection.getId() + "#");
                arc2.setAttributeNode(arcId2);

                Element transend2 = pnmlDocument.createElement("transend");
                Attr transendIdRef2 = pnmlDocument.createAttribute("idref");


                Element placeend2 = pnmlDocument.createElement("placeend");
                Attr placeendIdRef2 = pnmlDocument.createAttribute("idref");
                Attr arcOrientation2 = pnmlDocument.createAttribute("orientation");

                if (Category.PROCESS.getValue().equals(dstNode.getHeadCategory()) &&
                        !dstNode.getCategory().equals(sourceNode.getCategory())) {
                    cache.getSOCKETS().add(new Socket(dstNode.getHeadId(), dstNode.getPlaceId(), sourceNode.getPlaceId(), "In"));
                    setArcNodes(transendIdRef, placeendIdRef, arcOrientation, sourceNode.getTransId(), sourceNode.getPlaceId(), "TtoP");
                    setArcNodes(transendIdRef2, placeendIdRef2, arcOrientation2, dstNode.getHeadId(), sourceNode.getPlaceId(), "PtoT");
                    cache.getUsedFeature().add(sourceNode.getPlaceId());
                }

                else if (Category.PROCESS.getValue().equals(sourceNode.getHeadCategory()) && !dstNode.getCategory().equals(sourceNode.getCategory()) &&
                        "out".equals(connection.getSocketType())) {
                    cache.getSOCKETS().add(new Socket(sourceNode.getHeadId(), sourceNode.getPlaceId(), dstNode.getPlaceId(), "out"));
                    setArcNodes(transendIdRef, placeendIdRef, arcOrientation, sourceNode.getHeadId(), dstNode.getPlaceId(), "TtoP");
                    setArcNodes(transendIdRef2, placeendIdRef2, arcOrientation2, dstNode.getTransId(), dstNode.getPlaceId(), "PtoT");
                    cache.getUsedFeature().add(dstNode.getPlaceId());
                } else if (Boolean.TRUE.equals(connection.getGenerate()) && "in".equals(connection.getSocketType())) {
                    setArcNodes(transendIdRef, placeendIdRef, arcOrientation, sourceNode.getTransId(), sourceNode.getPlaceId(), "PtoT");
                    cache.getUsedFeature().add(sourceNode.getPlaceId());

                } else if (Boolean.FALSE.equals(connection.getGenerate()) && connection.getSocketType() == null &&
                        !sourceNode.getCategory().equals(Category.BUS.getValue())) {
                    setArcNodes(transendIdRef, placeendIdRef, arcOrientation, sourceNode.getTransId(), sourceNode.getPlaceId(), "TtoP");
                    if (!isFirstLayer(dstNode.getCategory())) {
                        setArcNodes(transendIdRef2, placeendIdRef2, arcOrientation2, dstNode.getTransId(), sourceNode.getPlaceId(), "PtoT");
                    }
                    cache.getUsedFeature().add(sourceNode.getPlaceId());
                } else {
                    setArcNodes(transendIdRef, placeendIdRef, arcOrientation, sourceNode.getTransId(), sourceNode.getPlaceId(), "TtoP");
                    setArcNodes(transendIdRef2, placeendIdRef2, arcOrientation2, dstNode.getTransId(), sourceNode.getPlaceId(), "PtoT");
                    cache.getUsedFeature().add(sourceNode.getPlaceId());  // było sourceNode.getPlaceId jak chcemy miejsce z wyjsciowego
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
                    petriNetGraphicsGenerator.setArcGraphicsProperties(pnmlDocument, arc2, connection.getPeriodArc());
                    arc2.appendChild(transend2);
                    arc2.appendChild(placeend2);
                    arcs.add(arc2);
                }
            } else if (connection.getContext().length() >= 4 && "NI:".equals(connection.getContext().substring(0, 3))) {
                if (cache.getContextByTransId(connection.getContext().substring(3)).equals(actualContext)) {
                    Element arc1 = pnmlDocument.createElement("arc");
                    Attr arcId = pnmlDocument.createAttribute("id");
                    arcId.setValue(connection.getId());
                    arc1.setAttributeNode(arcId);

                    Element transend = pnmlDocument.createElement("transend");
                    Attr transendIdRef = pnmlDocument.createAttribute("idref");

                    Element placeend = pnmlDocument.createElement("placeend");
                    Attr placeendIdRef = pnmlDocument.createAttribute("idref");
                    Attr arcOrientation = pnmlDocument.createAttribute("orientation");
                    arcId.setValue(connection.getId());
                    arc1.setAttributeNode(arcId);


                    String directionArc = "in".equals(connection.getSocketType()) ? "PtoT" : "TtoP";

                    setArcNodes(transendIdRef, placeendIdRef, arcOrientation, connection.getDestination(), connection.getSource(), directionArc);

                    transend.setAttributeNode(transendIdRef);
                    placeend.setAttributeNode(placeendIdRef);
                    arc1.setAttributeNode(arcOrientation);


                    arc1.appendChild(transend);
                    arc1.appendChild(placeend);


                    cache.getUsedFeature().add(connection.getSource());
                    cache.getUsedFeature().add(connection.getDestination());

                    petriNetGraphicsGenerator.setArcGraphicsProperties(pnmlDocument, arc1, connection.getPeriodArc());
                    arcs.add(arc1);
                }

            }

        }
        return arcs;

    }

    private void setArcNodes(Attr transendIdRef, Attr placeendIdRef, Attr arcOrientation, String transId, String placeId, String directionArc) {
        transendIdRef.setValue(transId);
        placeendIdRef.setValue(placeId);
        arcOrientation.setValue(directionArc);
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
