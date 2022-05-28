package lukowicz.application.petrinet;

import lukowicz.application.data.Category;
import lukowicz.application.data.ComponentInstance;
import lukowicz.application.data.DataPort;
import lukowicz.application.data.Socket;
import lukowicz.application.memory.Cache;
import lukowicz.application.memory.ElementsPosition;
import lukowicz.application.utils.TranslatorTools;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PetriNetGraphicsGenerator {

    private Cache cache = Cache.getInstance();
    private PetriNetPager petriNetPager;

    public PetriNetGraphicsGenerator(PetriNetPager petriNetPager) {
        this.petriNetPager = petriNetPager;
    }

    public void addGeneratorInfo(Document petriNetXmlFile, Element workspaceElements) {
        Element generator = petriNetXmlFile.createElement("generator");
        Attr toolAttr = petriNetXmlFile.createAttribute("tool");
        toolAttr.setValue("CPN Tools");
        generator.setAttributeNode(toolAttr);
        Attr versionAttr = petriNetXmlFile.createAttribute("version");
        versionAttr.setValue("4.0.1");
        generator.setAttributeNode(versionAttr);

        Attr formatAttr = petriNetXmlFile.createAttribute("format");
        formatAttr.setValue("6");
        generator.setAttributeNode(formatAttr);

        workspaceElements.appendChild(generator);
    }

    public void generateGlobBox(Document pnmlDocument, Element root) {
        Element globbox = pnmlDocument.createElement("globbox");
        Element block = pnmlDocument.createElement("block");
        Attr attrId = pnmlDocument.createAttribute("id");
        attrId.setValue(TranslatorTools.generateUUID());
        block.setAttributeNode(attrId);

        Element idElement = pnmlDocument.createElement("id");
        idElement.setTextContent("Standard priorities");

        block.appendChild(idElement);
        createMlElement(pnmlDocument, block, "val P_HIGH = 100;");
        createMlElement(pnmlDocument, block, "val P_NORMAL = 1000;");
        createMlElement(pnmlDocument, block, "val P_LOW = 10000;");
        globbox.appendChild(block);

        generateStandardUnits(pnmlDocument, globbox);


        root.appendChild(globbox);
    }


    public Element generateBinders(Document pnmlDocument) {
        Element binders = pnmlDocument.createElement("binders");
        Element cpnBinder = pnmlDocument.createElement("cpnbinder");

        Attr binderIdAttr = pnmlDocument.createAttribute("id");
        binderIdAttr.setValue(TranslatorTools.generateUUID());

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

        for (String cpnSheetInstance : cache.getInstancesBinders()) {
            Element cpnSheet = pnmlDocument.createElement("cpnsheet");
            Attr cpnSheetIdAttr = pnmlDocument.createAttribute("id");
            cpnSheetIdAttr.setValue(TranslatorTools.generateUUID());
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

    private void generateStandardUnits(Document pnmlDocument, Element globbox) {
        Element block = pnmlDocument.createElement("block");
        Attr attrId = pnmlDocument.createAttribute("id");
        attrId.setValue(TranslatorTools.generateUUID());
        block.setAttributeNode(attrId);
        Element idElement = pnmlDocument.createElement("id");
        idElement.setTextContent("Standard priorities");
        block.appendChild(idElement);

        generateSimpleType(pnmlDocument, block, "UNIT", "colset UNIT = unit", Boolean.FALSE);
        generateSimpleType(pnmlDocument, block, "BOOL", null, Boolean.FALSE);
        generateSimpleType(pnmlDocument, block, "INTINF", "colset INTINF = intinf;", Boolean.FALSE);
        generateSimpleType(pnmlDocument, block, "TIME", "colset TIME = time;", Boolean.FALSE);
        generateSimpleType(pnmlDocument, block, "REAL", "colset REAL = real;", Boolean.FALSE);
        generateSimpleType(pnmlDocument, block, "TINT", "colset TINT = int timed;", Boolean.TRUE);
        generateSimpleType(pnmlDocument, block, "String", null, Boolean.FALSE);

        globbox.appendChild(block);
    }

    private void generateSimpleType(Document pnmlDocument, Element block, String colorId, String layoutText, Boolean isTimed) {
        Element colorElement = pnmlDocument.createElement("color");
        Attr attrIdColor = pnmlDocument.createAttribute("id");
        attrIdColor.setValue(TranslatorTools.generateUUID());
        colorElement.setAttributeNode(attrIdColor);
        Element idColorElement = pnmlDocument.createElement("id");
        idColorElement.setTextContent(colorId);
        colorElement.appendChild(idColorElement);
        Element colorUnitElement = pnmlDocument.createElement(colorId.toLowerCase());
        colorElement.appendChild(colorUnitElement);
        if (isTimed) {
            Element timedElement = pnmlDocument.createElement("timed");
            Element intElement = pnmlDocument.createElement("int");
            colorElement.appendChild(timedElement);
            colorElement.appendChild(intElement);
        }
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
        mlAttrId.setValue(TranslatorTools.generateUUID());
        mlElement.setAttributeNode(mlAttrId);
        mlElement.setTextContent(s);
        Element layoutElement = pnmlDocument.createElement("layout");
        layoutElement.setTextContent(s);
        mlElement.appendChild(layoutElement);
        block.appendChild(mlElement);
    }

    public void setArcGraphicsProperties(Document pnmlDocument, Element arc1, String periodArcText) {

        createPosattr(pnmlDocument,0.0,0.0);

        Element fillProperty = createFillProperty(pnmlDocument);
        arc1.appendChild(fillProperty);


        Element lineProperty = createLineProperty(pnmlDocument);
        arc1.appendChild(lineProperty);

        Element textProperty = createTextProperty(pnmlDocument);
        arc1.appendChild(textProperty);


        Element annot = pnmlDocument.createElement("annot");
        Attr annotId = pnmlDocument.createAttribute("id");
        annotId.setValue(TranslatorTools.generateUUID());
        annot.setAttributeNode(annotId);

        Element position = createPosattr(pnmlDocument, 0.00000, 0.00000);
        annot.appendChild(position);

        Element fillAnnotProperty = createFillProperty(pnmlDocument);
        createElementGraphicsProperty(pnmlDocument, fillAnnotProperty);

        annot.appendChild(fillAnnotProperty);

        Element lineAnnotProperty = createLineProperty(pnmlDocument);

        createElementGraphicsProperty(pnmlDocument, lineAnnotProperty);

        annot.appendChild(lineAnnotProperty);

        Element textAnnotProperty = createTextProperty(pnmlDocument);

        annot.appendChild(textAnnotProperty);

        Element textArc = pnmlDocument.createElement("text");
        Attr textTool = pnmlDocument.createAttribute("tool");
        textTool.setValue("CPN Tools");
        textArc.setAttributeNode(textTool);
        Attr versionTool = pnmlDocument.createAttribute("version");
        versionTool.setValue("4.0.1");
        if (periodArcText != null) {
            textArc.setTextContent(periodArcText);
        } else {
            textArc.setTextContent("1");
        }

        annot.appendChild(textArc);

        arc1.appendChild(annot);
    }

    private void createElementGraphicsProperty(Document pnmlDocument, Element fillAnnotProperty) {
        Attr colorAnnotFill = pnmlDocument.createAttribute("colour");
        colorAnnotFill.setValue("White");
        fillAnnotProperty.setAttributeNode(colorAnnotFill);
        Attr patternAnnot = pnmlDocument.createAttribute("pattern");
        patternAnnot.setValue("Solid");
        fillAnnotProperty.setAttributeNode(patternAnnot);
        Attr filledAnnot = pnmlDocument.createAttribute("filled");
        patternAnnot.setValue("false");
        fillAnnotProperty.setAttributeNode(filledAnnot);
    }

    public Element generatePlaceGraphics(Document pnmlDocument, DataPort dataPort, Element place, Boolean isTimed) {
        Double placeXPosition = ElementsPosition.getPLACE_X_POSITION();
        Double placeYPosition = ElementsPosition.getPLACE_Y_POSITION();

        Element placePosition = createPosattr(pnmlDocument, placeXPosition, placeYPosition);
        place.appendChild(placePosition);

        Element fillProperty = createFillProperty(pnmlDocument);
        place.appendChild(fillProperty);

        Element lineProperty = createLineProperty(pnmlDocument);
        place.appendChild(lineProperty);

        Element textProperty = createTextProperty(pnmlDocument);
        place.appendChild(textProperty);

        Element placeText = pnmlDocument.createElement("text");
        placeText.appendChild(pnmlDocument.createTextNode(dataPort.getName()));
        place.appendChild(placeText);

        Element ellipseProperty = createEllipseProperty(pnmlDocument);
        place.appendChild(ellipseProperty);

        Element typeProperty = pnmlDocument.createElement("type");
        Element typePosition = createPosattr(pnmlDocument, placeXPosition - 15.0000, placeYPosition - 20.0000);
        typeProperty.appendChild(typePosition);

        Element fillTypeProperty = createFillProperty(pnmlDocument);
        typeProperty.appendChild(fillTypeProperty);

        Element lineTypeProperty = createLineProperty(pnmlDocument);
        typeProperty.appendChild(lineTypeProperty);

        Element textTypeProperty = createTextProperty(pnmlDocument);
        typeProperty.appendChild(textTypeProperty);

        createTextTypeContent(pnmlDocument, isTimed, typeProperty);
        place.appendChild(typeProperty);

        Element initMarkProperty = pnmlDocument.createElement("initmark");

        Element initMarkPosition = createPosattr(pnmlDocument, placeXPosition + 15.0000, placeYPosition + 20.0000);
        initMarkProperty.appendChild(initMarkPosition);

        Element fillInitMarkProperty = createFillProperty(pnmlDocument);
        initMarkProperty.appendChild(fillInitMarkProperty);


        Element lineInitMarkProperty = createLineProperty(pnmlDocument);
        initMarkProperty.appendChild(lineInitMarkProperty);

        Element textInitMarkProperty = createTextProperty(pnmlDocument);
        initMarkProperty.appendChild(textInitMarkProperty);

        Element textInitMarkPlaceContent = createTextContentInitMark(pnmlDocument);
        initMarkProperty.appendChild(textInitMarkPlaceContent);

        place.appendChild(initMarkProperty);


        return place;

    }

    private Element createTextContentInitMark(Document pnmlDocument) {
        Element textInitMarkPlaceContent = pnmlDocument.createElement("text");
        Attr toolInitMarkTextAttr = pnmlDocument.createAttribute("tool");
        toolInitMarkTextAttr.setValue("CPN Tools");
        textInitMarkPlaceContent.setAttributeNode(toolInitMarkTextAttr);
        Attr textInitMarkVersion = pnmlDocument.createAttribute("version");
        textInitMarkVersion.setValue("4.0.1");
        textInitMarkPlaceContent.setAttributeNode(textInitMarkVersion);
        textInitMarkPlaceContent.setTextContent("1");
        return textInitMarkPlaceContent;
    }

    private void createTextTypeContent(Document pnmlDocument, Boolean isTimed, Element typeProperty) {
        Element textTypePlaceContent = pnmlDocument.createElement("text");
        Attr toolAttr = pnmlDocument.createAttribute("tool");
        toolAttr.setValue("CPN Tools");
        textTypePlaceContent.setAttributeNode(toolAttr);
        Attr textTypeVersion = pnmlDocument.createAttribute("version");
        textTypeVersion.setValue("4.0.1");
        textTypePlaceContent.setAttributeNode(textTypeVersion);
        if (isTimed) {
            textTypePlaceContent.setTextContent("TINT");
        } else {
            textTypePlaceContent.setTextContent("INTINF");
        }

        typeProperty.appendChild(textTypePlaceContent);
    }

    private Element createPosattr(Document pnmlDocument, Double placeXPosition, Double placeYPosition) {
        Element typePosition = pnmlDocument.createElement("posattr");
        Attr posattrXProperty = pnmlDocument.createAttribute("x");
        Double typeXPosition = placeXPosition;
        posattrXProperty.setValue(typeXPosition.toString());
        Attr posattrYProperty = pnmlDocument.createAttribute("y");
        Double typeYPosition = placeYPosition;
        posattrYProperty.setValue(typeYPosition.toString());
        typePosition.setAttributeNode(posattrXProperty);
        typePosition.setAttributeNode(posattrYProperty);
        return typePosition;
    }

    private Element createEllipseProperty(Document pnmlDocument) {
        Element ellipseProperty = pnmlDocument.createElement("ellipse");
        Attr weight = pnmlDocument.createAttribute("w");
        weight.setValue("60.000000");
        ellipseProperty.setAttributeNode(weight);
        Attr height = pnmlDocument.createAttribute("h");
        height.setValue("40.000000");
        ellipseProperty.setAttributeNode(height);
        return ellipseProperty;
    }

    private Element createTextProperty(Document pnmlDocument) {
        Element textProperty = pnmlDocument.createElement("textattr");
        Attr colorText = pnmlDocument.createAttribute("colour");
        colorText.setValue("Black");
        textProperty.setAttributeNode(colorText);
        Attr isBold = pnmlDocument.createAttribute("bold");
        isBold.setValue("false");
        textProperty.setAttributeNode(isBold);
        return textProperty;
    }

    private Element createLineProperty(Document pnmlDocument) {
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
        return lineProperty;
    }

    private Element createFillProperty(Document pnmlDocument) {
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
        return fillProperty;
    }

    public Element generateGeneralTransition(Document pnmlDocument, ComponentInstance componentInstance){
        Element transition = generateAttributesGraphicsForTransition(pnmlDocument, componentInstance);
        Element subtElement = pnmlDocument.createElement("subst");
        Attr subpageAttr = pnmlDocument.createAttribute("subpage");
        String subpageId = petriNetPager.getPageByContext("").getPageId();
        subpageAttr.setValue(subpageId);
        subtElement.setAttributeNode(subpageAttr);
        Attr portSock = pnmlDocument.createAttribute("portsock");
        portSock.setValue("");
        subtElement.setAttributeNode(portSock);
        transition.appendChild(subtElement);

        return transition;

    }


    public Element generateGraphicsAttributeTransition(Document pnmlDocument, ComponentInstance componentInstance) {
        Element transition = generateAttributesGraphicsForTransition(pnmlDocument, componentInstance);

        if (Category.PROCESS.getValue().equals(componentInstance.getCategory()) || (Category.THREAD.getValue().equals(componentInstance.getCategory()) && !"".equals(componentInstance.getPeriod()))) {
            Element substElement = pnmlDocument.createElement("subst");
            Attr subpageAttr = pnmlDocument.createAttribute("subpage");
            String pageId = petriNetPager.getPageIdForTransId(componentInstance.getId());
            subpageAttr.setValue(pageId);
            substElement.setAttributeNode(subpageAttr);
            Attr portSock = pnmlDocument.createAttribute("portsock");
            StringBuilder portSockValue = new StringBuilder();
            for (Socket socket : cache.getSOCKETS()) {
                if (componentInstance.getId().equals(socket.getComponentId())) {
                    portSockValue.append("(" + socket.getPortId() + "," + socket.getSocketId() + ")");
                }
            }
            portSock.setValue(portSockValue.toString());
            substElement.setAttributeNode(portSock);


            Element subpageElement = pnmlDocument.createElement("subpageinfo");
            Attr subpageAttrId = pnmlDocument.createAttribute("id");
            subpageAttrId.setValue(TranslatorTools.generateUUID());
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

            Element subPageTransitionTextProperty = createTextProperty(pnmlDocument);
            subpageElement.appendChild(subPageTransitionTextProperty);

            substElement.appendChild(subpageElement);

            transition.appendChild(substElement);
        }

        return transition;
    }

    private Element generateAttributesGraphicsForTransition(Document pnmlDocument, ComponentInstance componentInstance) {
        Element transition = pnmlDocument.createElement("trans");

        Attr transitionId = pnmlDocument.createAttribute("id");
        transitionId.setValue(componentInstance.getId());
        transition.setAttributeNode(transitionId);

        Element transitionPosition = createPosattr(pnmlDocument, componentInstance.getPos_X(),componentInstance.getPos_Y());
        transition.appendChild(transitionPosition);


        Element fillProperty = createFillProperty(pnmlDocument);
        transition.appendChild(fillProperty);


        Element lineProperty = createLineProperty(pnmlDocument);
        transition.appendChild(lineProperty);

        Element textProperty = createTextProperty(pnmlDocument);
        transition.appendChild(textProperty);

        Element transitionText = pnmlDocument.createElement("text");
        transitionText.appendChild(pnmlDocument.createTextNode(componentInstance.getName()));
        transition.appendChild(transitionText);


        Element boxProperty = pnmlDocument.createElement("box");
        createBoxProperty(pnmlDocument, boxProperty);
        transition.appendChild(boxProperty);
        return transition;
    }

    private void createBoxProperty(Document pnmlDocument, Element boxProperty) {
        Attr weight = pnmlDocument.createAttribute("w");
        weight.setValue("152.000000");
        boxProperty.setAttributeNode(weight);
        Attr height = pnmlDocument.createAttribute("h");
        height.setValue("40.000000");
        boxProperty.setAttributeNode(height);
    }


}
