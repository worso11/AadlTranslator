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
        attrIdColor.setValue(TranslatorTools.generateUUID());
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
        mlAttrId.setValue(TranslatorTools.generateUUID());
        mlElement.setAttributeNode(mlAttrId);
        mlElement.setTextContent(s);
        Element layoutElement = pnmlDocument.createElement("layout");
        layoutElement.setTextContent(s);
        mlElement.appendChild(layoutElement);
        block.appendChild(mlElement);
    }

    public void setArcGraphicsProperties(Document pnmlDocument, Element arc1) {
        Element arcPosition = pnmlDocument.createElement("posattr");
        Attr positionX = pnmlDocument.createAttribute("x");
        positionX.setValue("0.0");
        Attr positionY = pnmlDocument.createAttribute("y");
        positionY.setValue("0.0");
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

    public Element generatePlaceGraphics(Document pnmlDocument, DataPort dataPort, Element place) {
        Element placePosition = pnmlDocument.createElement("posattr");
        Attr positionX = pnmlDocument.createAttribute("x");
        Double placeXPosition = ElementsPosition.getPLACE_X_POSITION();
        positionX.setValue(placeXPosition.toString());
        Attr positionY = pnmlDocument.createAttribute("y");
        Double placeYPosition = ElementsPosition.getPLACE_Y_POSITION();
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
        placeText.appendChild(pnmlDocument.createTextNode(dataPort.getName()));
        place.appendChild(placeText);

        Element ellipseProperty = pnmlDocument.createElement("ellipse");
        Attr weight = pnmlDocument.createAttribute("w");
        weight.setValue("60.000000");
        ellipseProperty.setAttributeNode(weight);
        Attr height = pnmlDocument.createAttribute("h");
        height.setValue("40.000000");
        ellipseProperty.setAttributeNode(height);
        place.appendChild(ellipseProperty);

        Socket socket = cache.isConnectingPort(dataPort);

//        if (socket != null) {
//
//            Element port = pnmlDocument.createElement("port");
//            Attr attrId = pnmlDocument.createAttribute("id");
//            attrId.setValue(TranslatorTools.generateUUID());
//            port.setAttributeNode(attrId);
//            Attr attrType = pnmlDocument.createAttribute("type");
//            attrType.setValue(socket.getDirection());
//            port.setAttributeNode(attrType);
//
//            Element portPosition = pnmlDocument.createElement("posattr");
//            Attr portPositionX = pnmlDocument.createAttribute("x");
//            Double portPositionXValue = placeXPosition - 24.0000;
//            portPositionX.setValue(portPositionXValue.toString());
//            Attr portPositionY = pnmlDocument.createAttribute("y");
//            Double portPositionYValue = placeYPosition - 16.0000;
//            portPositionY.setValue(portPositionYValue.toString());
//            portPosition.setAttributeNode(portPositionX);
//            portPosition.setAttributeNode(portPositionY);
//
//            port.appendChild(portPosition);
//
//
//            Element portFillProperty = pnmlDocument.createElement("fillattr");
//            Attr portColorFill = pnmlDocument.createAttribute("colour");
//            portColorFill.setValue("White");
//            portFillProperty.setAttributeNode(portColorFill);
//            Attr portPattern = pnmlDocument.createAttribute("pattern");
//            portPattern.setValue("Solid");
//            portFillProperty.setAttributeNode(portPattern);
//            Attr portFilled = pnmlDocument.createAttribute("filled");
//            portPattern.setValue("false");
//            portFillProperty.setAttributeNode(portFilled);
//            port.appendChild(portFillProperty);
//
//
//            Element portLineProperty = pnmlDocument.createElement("lineattr");
//            Attr portColorLine = pnmlDocument.createAttribute("colour");
//            portColorLine.setValue("Black");
//            portLineProperty.setAttributeNode(portColorLine);
//            Attr portThick = pnmlDocument.createAttribute("thick");
//            portThick.setValue("0");
//            portLineProperty.setAttributeNode(portThick);
//            Attr portType = pnmlDocument.createAttribute("type");
//            portType.setValue("Solid");
//            portLineProperty.setAttributeNode(portType);
//            port.appendChild(portLineProperty);
//
//
//            Element portTextProperty = pnmlDocument.createElement("textattr");
//            Attr portColorText = pnmlDocument.createAttribute("colour");
//            portColorText.setValue("Black");
//            portTextProperty.setAttributeNode(portColorText);
//            Attr portIsBold = pnmlDocument.createAttribute("bold");
//            portIsBold.setValue("false");
//            portTextProperty.setAttributeNode(portIsBold);
//            port.appendChild(portTextProperty);
//
//            place.appendChild(port);
//        }

        return place;

    }


    public Element generateGraphicsAttributeTransition(Document pnmlDocument, ComponentInstance componentInstance) {
        Element transition = pnmlDocument.createElement("trans");

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

        if (Category.PROCESS.getValue().equals(componentInstance.getCategory()) || (Category.THREAD.getValue().equals(componentInstance.getCategory()) && !"".equals(componentInstance.getPeriod()) )) {
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





}
