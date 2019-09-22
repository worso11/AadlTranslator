package lukowicz.application;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class Client {

    private static Parser parser = new Parser();

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        parser.parseFile();
    }
}
