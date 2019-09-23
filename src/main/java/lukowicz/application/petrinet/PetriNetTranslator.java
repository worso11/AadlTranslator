package lukowicz.application.petrinet;

import lukowicz.application.data.*;
import lukowicz.application.memory.Cache;
import lukowicz.application.utils.ParserTools;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class PetriNetTranslator {

    public void translateElements(Document pnmlDocument, Element page, List<ComponentInstance> componentInstances) {
        for (ComponentInstance componentInstance : componentInstances) {
            String componentInstanceCategory = componentInstance.getCategory();
            if (componentInstanceCategory.equals(Category.DEVICE.getValue()) || componentInstanceCategory.equals(Category.PROCESS.getValue()) || componentInstanceCategory.equals(Category.THREAD.getValue())) {
                Element transition = generateTransition(pnmlDocument, componentInstance);
                page.appendChild(transition);
            }
            if (componentInstanceCategory.equals(Category.BUS.getValue())) {
                Element transition = generateTransition(pnmlDocument, componentInstance);
                page.appendChild(transition);
            }
            List<DataPort> dataPorts = componentInstance.getDataPort();
            for (DataPort feature : dataPorts) {
                Element place = generatePlace(pnmlDocument, feature);
                if (Cache.getUsedFeature().contains(feature.getId())) { // unikalnośc miejsc
                    page.appendChild(place);
                }

            }

        }
        // Generated places
        for(DataPort feature : Cache.getGeneratedPlaces()){
            Element place = generatePlace(pnmlDocument,feature);
            page.appendChild(place);
        }

        Cache.clearUsedFeature();
        Cache.clearGeneratedPlaces();
    }

    public Element generatePlace(Document pnmlDocument, DataPort dataPort) {
        Element place = pnmlDocument.createElement("place");
        Attr placeId = pnmlDocument.createAttribute("id");
        placeId.setValue(dataPort.getId());
        place.setAttributeNode(placeId);

        return generatePlaceGraphics(pnmlDocument, dataPort, place);

    }

    private Element generatePlaceGraphics(Document pnmlDocument, DataPort dataPort, Element place) {
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

        Socket socket = Cache.isConnectingPort(dataPort);

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

    private Element generateTransition(Document pnmlDocument, ComponentInstance componentInstance) {
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

        if (Category.PROCESS.getValue().equals(componentInstance.getCategory())) {
            Element substElement = pnmlDocument.createElement("subst");
            Attr subpageAttr = pnmlDocument.createAttribute("subpage");
            String pageId = Cache.getPageForTransId(componentInstance.getId());
            subpageAttr.setValue(pageId);
            substElement.setAttributeNode(subpageAttr);
            Attr portSock = pnmlDocument.createAttribute("portsock");
            StringBuilder portSockValue = new StringBuilder();
            for (Socket socket : Cache.getSOCKETS()) {
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


}
