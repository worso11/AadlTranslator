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

    private PetriNetGraphicsGenerator petriNetGraphicsGenerator;
    private Cache cache = Cache.getInstance();

    public PetriNetTranslator(PetriNetGraphicsGenerator petriNetGraphicsGenerator) {
        this.petriNetGraphicsGenerator = petriNetGraphicsGenerator;
    }

    public void translateElements(Document pnmlDocument, Element page, List<ComponentInstance> componentInstances) {
        for (ComponentInstance componentInstance : componentInstances) {
            String componentInstanceCategory = componentInstance.getCategory();
            if (componentInstanceCategory.equals(Category.DEVICE.getValue()) || componentInstanceCategory.equals(Category.PROCESS.getValue())
                    || componentInstanceCategory.equals(Category.THREAD.getValue()) || componentInstanceCategory.equals(Category.GENERATED_TRANS.getValue())) {
                Element transition = generateTransition(pnmlDocument, componentInstance);
                page.appendChild(transition);
            }
            List<DataPort> dataPorts = componentInstance.getDataPort();
            for (DataPort feature : dataPorts) {
                Element place = generatePlace(pnmlDocument, feature);
                if (cache.getUsedFeature().contains(feature.getId())) { // unikalnośc miejsc
                    page.appendChild(place);
                }

            }

        }
        cache.clearUsedFeature();
        cache.clearGeneratedPlaces();
    }

    public Element generatePlace(Document pnmlDocument, DataPort dataPort) {
        Element place = pnmlDocument.createElement("place");
        Attr placeId = pnmlDocument.createAttribute("id");
        placeId.setValue(dataPort.getId());
        place.setAttributeNode(placeId);

        return petriNetGraphicsGenerator.generatePlaceGraphics(pnmlDocument, dataPort, place, dataPort.getTimed());
    }

    private Element generateTransition(Document pnmlDocument, ComponentInstance componentInstance) {
       return petriNetGraphicsGenerator.generateGraphicsAttributeTransition(pnmlDocument, componentInstance);
    }



    public Element insertGeneralTransition(Document petriNetXmlFile) {
        ComponentInstance systemInstance = new ComponentInstance(cache.getSystemName(),Category.SYSTEM.getValue());
        systemInstance.setPos_X(0.00);
        systemInstance.setPos_Y(0.00);
        return petriNetGraphicsGenerator.generateGeneralTransition(petriNetXmlFile, systemInstance);

    }
}
