package lukowicz.application.petrinet;

import lukowicz.application.data.Category;
import lukowicz.application.data.ComponentInstance;
import lukowicz.application.data.DataPort;
import lukowicz.application.memory.Cache;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class PetriNetTranslator {

    private PetriGraphicsGenerator petriGraphicsGenerator = new PetriGraphicsGenerator();

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

        return petriGraphicsGenerator.generatePlaceGraphics(pnmlDocument, dataPort, place);

    }

    private Element generateTransition(Document pnmlDocument, ComponentInstance componentInstance) {
       return petriGraphicsGenerator.generateGraphicsAttributeTransition(pnmlDocument, componentInstance);
    }


}
