package lukowicz.application.petrinet;

import lukowicz.application.memory.Cache;
import lukowicz.application.utils.ParserTools;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PetriGraphicsGenerator {

    public void addGeneratorInfo(Document pnmlDocument, Element workspaceElements) {
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

    public void generateGlobBox(Document pnmlDocument, Element root) {
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


    public Element generateBinders(Document pnmlDocument) {
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

        for (String cpnSheetInstance : Cache.getInstancesBinders()) {
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

    public void setArcGraphicsProperties(Document pnmlDocument, Element arc1, String pos_x, String pos_y) {
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


}
