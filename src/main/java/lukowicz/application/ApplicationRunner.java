package lukowicz.application;

import lukowicz.application.aadl.ElementSearcher;
import lukowicz.application.petrinet.PetriNetGenerator;
import lukowicz.application.petrinet.PetriNetGraphicsGenerator;
import lukowicz.application.petrinet.PetriNetPager;
import lukowicz.application.petrinet.PetriNetTranslator;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class ApplicationRunner {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        PetriNetPager petriNetPager = new PetriNetPager();
        ElementSearcher elementSearcher = new ElementSearcher(petriNetPager);
        PetriNetGraphicsGenerator petriNetGraphicsGenerator = new PetriNetGraphicsGenerator(petriNetPager);
        PetriNetTranslator petriNetTranslator = new PetriNetTranslator(petriNetGraphicsGenerator);
        PetriNetGenerator petriNetGenerator = new PetriNetGenerator(petriNetGraphicsGenerator, petriNetTranslator, elementSearcher, petriNetPager);
        Parser parser = new Parser(elementSearcher, petriNetGenerator);
        parser.parseFile(args[0]);
    }
}
