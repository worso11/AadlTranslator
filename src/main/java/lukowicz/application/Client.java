package lukowicz.application;

import lukowicz.application.tools.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashMap;


public class Client {
    private String userName = "";
    private static HashMap<String, Element> uniquePlaces = new HashMap<>();
    private static  Parser parser = new Parser();

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, TransformerException {
          parser.parseFile();
//        File fXmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatAADL-XML2.xml");
//        File pnmlFile = new File("D:\\Studia\\magisterka\\Modelowanie i analiza oprogramowania z zastosowaniem języka AADL i sieci Petriego\\Pliki\\tempomatPnml-Output2.xml");
//
//        DocumentBuilderFactory factory =
//                DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//
//
//        Document loadedDocument = builder.parse(fXmlFile);
//        Document pnmlDocument = builder.newDocument();
//        Element root = pnmlDocument.createElement("pnml:pnml");
//
//        Element module = pnmlDocument.createElement("pnml:module");
//        root.appendChild(module);
//        pnmlDocument.appendChild(root);
//
//        Element pnmlName = pnmlDocument.createElement("pnml:name");
//        module.appendChild(pnmlName);
//
//
//        Element pnmlText = pnmlDocument.createElement("pnml:text");
//        pnmlText.appendChild(pnmlDocument.createTextNode("New_page"));
//        pnmlName.appendChild(pnmlText);
//
//        loadedDocument.getDocumentElement().normalize();
//
//        NodeList nList = loadedDocument.getElementsByTagName("componentInstance");
//
//        NodeList connInstances = loadedDocument.getElementsByTagName("connectionInstance");
//
//
//        for (int temp = 0; temp < nList.getLength(); temp++) {
//
//            Node nNode = nList.item(temp);
//
//            System.out.println("\nCurrent Element :" + nNode.getNodeName());
//
//            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//
//                Element eElement = (Element) nNode;
//
//                System.out.println("Name of componenet : " + eElement.getAttribute("name"));
//                System.out.println("Categroy of componenet : " + eElement.getAttribute("category"));
//                if (eElement.getAttribute("category").equals("device")) {
//
//                    Element transition = pnmlDocument.createElement("pnml:transition");
//                    Element transitionName = pnmlDocument.createElement("pnml:name");
//                    Element transitionText = pnmlDocument.createElement("pnml:text");
//                    transitionText.appendChild(pnmlDocument.createTextNode(eElement.getAttribute("name")));
//                    transitionName.appendChild(transitionText);
//                    transition.appendChild(transitionName);
//
//                    Attr transitionId = pnmlDocument.createAttribute("id");
//                    transitionId.setValue(String.valueOf(temp));
//                    transition.setAttributeNode(transitionId);
//
//                    NodeList features = eElement.getElementsByTagName("featureInstance");
//                    extractPlaces(pnmlDocument, features);
//                    System.out.println("features instances " + features.getLength());
//
//                    module.appendChild(transition);
//                }
//                if (eElement.getAttribute("category").equals("process")) {
//
//                    Element newModule = pnmlDocument.createElement("pnml:module");
//                    Element moduleName = pnmlDocument.createElement("pnml:name");
//                    Element moduleText = pnmlDocument.createElement("pnml:text");
//                    moduleText.appendChild(pnmlDocument.createTextNode(eElement.getAttribute("name")));
//                    moduleName.appendChild(moduleText);
//                    newModule.appendChild(moduleName);
//
//                    NodeList features = eElement.getElementsByTagName("featureInstance");
//
//
//                    NodeList threads = eElement.getElementsByTagName("componentInstance");
//
//                    for (int i = 0; i < threads.getLength(); i++) {
//                        Node threadNode = threads.item(i);
//
//                        System.out.println("\nCurrent Element :" + nNode.getNodeName());
//
//                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//
//                            Element threadElement = (Element) threadNode;
//
//                            System.out.println("Name of componenet : " + threadElement.getAttribute("name"));
//                            System.out.println("Categroy of componenet : " + threadElement.getAttribute("category"));
//                            if (threadElement.getAttribute("category").equals("thread")) {
//
//                                Node featureNode = threads.item(i);
//                                if (featureNode != null) {
//                                    if (featureNode.getNodeType() == Node.ELEMENT_NODE) {
//                                        Element transition = pnmlDocument.createElement("pnml:transition");
//                                        Element transitionName = pnmlDocument.createElement("pnml:name");
//                                        Element transitionText = pnmlDocument.createElement("pnml:text");
//                                        transitionText.appendChild(pnmlDocument.createTextNode(threadElement.getAttribute("name")));
//                                        transitionName.appendChild(transitionText);
//                                        transition.appendChild(transitionName);
//
//                                        Attr transitionId = pnmlDocument.createAttribute("id");
//                                        transitionId.setValue(String.valueOf(i));
//                                        transition.setAttributeNode(transitionId);
//
//
//                                        newModule.appendChild(transition);
//                                    }
//
//                                }
//                            }
//
//
//                            System.out.println("features instances " + features.getLength());
//                            extractPlaces(pnmlDocument,features); //zastanowic sie
//
//                            root.appendChild(newModule);
//                        }
//
//                    }
//
//                }
//
//
//
//            }
//        }
//        for (String key : uniquePlaces.keySet()) {
//            module.appendChild(uniquePlaces.get(key));
//        }
//        TransformerFactory transformerFactory = TransformerFactory.newInstance();
//        Transformer transformer = transformerFactory.newTransformer();
//        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//        DOMSource domSource = new DOMSource(pnmlDocument);
//        StreamResult streamResult = new StreamResult(new File(String.valueOf(pnmlFile)));
//
//
//        transformer.transform(domSource, streamResult);
//
//        System.out.println("Done creating XML File");

    }




            public static void extractPlaces (Document document, NodeList features){
                for (int i = 0; i < features.getLength(); i++) {
                    Node featureNode = features.item(i);
                    if (featureNode != null) {
                        if (featureNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element featureElement = (Element) featureNode;
                            Element place = document.createElement("pnml:place");
                            Element placeName = document.createElement("pnml:name");
                            Element placeText = document.createElement("pnml:text");
                            System.out.println("LOLL TEN NAME TO : " + featureElement.getAttribute("name"));
                            NodeList ownedPropertyAssociation = featureElement.getElementsByTagName("ownedPropertyAssociation");

                            for (int j = 0; j < ownedPropertyAssociation.getLength(); j++) {
                                Node property = ownedPropertyAssociation.item(j);
                                Element propertyElement = (Element) property;
                                NodeList testNode = propertyElement.getElementsByTagName("ownedValue");
                                //testNode.item(1).getAttributes(). tu sa value!!!!!!!!!!!!!!!!!!!!!!!!
                                System.out.println("o to chodzi "+propertyElement.getAttributes().getNamedItem("value"));
                            }

                            placeText.appendChild(document.createTextNode(featureElement.getAttribute("name")));
                            placeName.appendChild(placeText);
                            place.appendChild(placeName);
//                                transition.appendChild(place);
                            uniquePlaces.put(featureElement.getAttribute("name"), place);
                        }

                    }
                }
            }

        }
