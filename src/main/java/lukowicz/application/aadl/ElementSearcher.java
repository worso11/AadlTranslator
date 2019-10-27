package lukowicz.application.aadl;

import lukowicz.application.data.*;
import lukowicz.application.memory.Cache;
import lukowicz.application.petrinet.PetriNetPager;
import lukowicz.application.utils.TranslatorTools;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class ElementSearcher {

    private Cache cache = Cache.getInstance();
    private PetriNetPager petriNetPager;

    public ElementSearcher(PetriNetPager petriNetPager) {
        this.petriNetPager = petriNetPager;
    }

    public void searchElements(NodeList componenentInstances, ComponentInstance processingElement) {
        for (int i = 0; i < componenentInstances.getLength(); i++) {
            Node component = componenentInstances.item(i);
            System.out.println("\nCurrent Element :" + component.getNodeName());

            if (component.getNodeType() == Node.ELEMENT_NODE) {
                ComponentInstance componentInstance;
                Element actualComponent = (Element) component;
                if (processingElement != null) {
                    componentInstance = processingElement;
                } else {
                    componentInstance = new ComponentInstance(actualComponent.getAttribute("name"),
                            actualComponent.getAttribute("category"));
                }

                if (cache.isUniqueComponentsContain(actualComponent.getAttribute("name"))) {
                    continue;
                }

                ComponentInstance componentInstanceNested = processingElement != null ?
                        new ComponentInstance(actualComponent.getAttribute("name"), actualComponent.getAttribute("category")) : null;

                NodeList featureInstances = actualComponent.getElementsByTagName("featureInstance");

                NodeList ownedPropertyAssociations = actualComponent.getElementsByTagName("ownedPropertyAssociation");
                String periodValue = "";
                for (int k = 0; k < ownedPropertyAssociations.getLength(); k++) {
                    Node ownerProperty = ownedPropertyAssociations.item(k);
                    Element ownedPropertyElement = (Element) ownerProperty;
                    System.out.println("owned Property" + ownedPropertyElement);
                    NodeList ownerProperties = ownedPropertyElement.getElementsByTagName("property");
                    for (int l = 0; l < ownerProperties.getLength(); ++l) {
                        Node property = ownerProperties.item(l);
                        Element propertyElement = (Element) property;
                        Attr hrefProperty = propertyElement.getAttributeNode("href");
                        if (hrefProperty.getValue().contains("Timing_Properties.Period")) {
                            periodValue = ownedPropertyElement.getElementsByTagName("ownedValue").item(1).
                                    getAttributes().getNamedItem("value").getNodeValue();
                            System.out.println("period Value " + periodValue);
                        }

                    }
                }
                for (int j = 0; j < featureInstances.getLength(); j++) {
                    Node featureInstance = featureInstances.item(j);

                    Element featureElement = (Element) featureInstance;
                    if(!"busAccess".equals(featureElement.getAttribute("category"))) {
                        System.out.println("Name of feature : " + featureElement.getAttribute("name"));
                        if (componentInstanceNested != null) {
                            componentInstance.getReverseFeatureInstances().remove(new DataPort(featureElement.getAttribute("name"),
                                    featureElement.getAttribute("direction")));
                            componentInstance.getReverseFeatureInstances();//wroc do starego porzadku
                            componentInstanceNested.getDataPort().add(new DataPort(featureElement.getAttribute("name"),
                                    featureElement.getAttribute("direction")));
                        } else {
                            componentInstance.getDataPort().add(new DataPort(featureElement.getAttribute("name"),
                                    featureElement.getAttribute("direction")));
                        }
                    }
                }
                if (componentInstanceNested != null) {
                    //czy nie mozna lepiej??
                    processingElement.getComponentInstancesNested().add(componentInstanceNested);
                    componentInstanceNested.setPeriod(periodValue);
                    cache.addElementToUniqueComponents(componentInstanceNested.getName());
                    if (!"".equals(periodValue)) {
                        DataPort waitingPlace = new DataPort("Wait", "in");
                        waitingPlace.setTimed(Boolean.TRUE);
                        componentInstanceNested.getDataPort().add(waitingPlace);

                        String contextPage = "NI:" + componentInstance.getId();
                        String connectionPageSource = waitingPlace.getId();
                        String connectionPageDestination = componentInstanceNested.getId();

                        Connection connectionIn = new Connection(contextPage, connectionPageSource, connectionPageDestination);
                        connectionIn.setSocketType("in");
                        connectionIn.setGenerate(Boolean.TRUE);
                        connectionIn.setTimed(Boolean.TRUE);
                        Connection connectionOut = new Connection(contextPage, connectionPageSource, connectionPageDestination);
                        connectionOut.setGenerate(Boolean.TRUE);
                        connectionOut.setTimed(Boolean.TRUE);
                        connectionOut.setSocketType("out");
                        connectionOut.setPeriodArc("1@+" + periodValue);

                        cache.addConnection(connectionIn);
                        cache.addConnection(connectionOut);

                        componentInstanceNested.setComponentInstancesNested(new ArrayList<>());
                        ComponentInstance generatedTrans = new ComponentInstance("Code Implementation", Category.GENERATED_TRANS.getValue());
                        componentInstanceNested.getComponentInstancesNested().
                                add(generatedTrans);

                        String additionalConnContext = TranslatorTools.generateUUID();

                        petriNetPager.addNewPage(additionalConnContext, componentInstanceNested.getId(), Boolean.TRUE, componentInstance.getId(), componentInstanceNested.getName());


                        for (DataPort dataPort :
                                componentInstanceNested.getDataPort()) {
                            DataPort copyDataPort = new DataPort(dataPort.getName(), dataPort.getDirection());
                            copyDataPort.setTimed(dataPort.getTimed());
                            componentInstanceNested.getComponentInstancesNested().get(0).getDataPort().
                                    add(copyDataPort);

                            String connectionSubpageContext = additionalConnContext;
                            String connectionSubpageSource = copyDataPort.getId();
                            String connectionSubpageDestination = generatedTrans.getId();

                            Connection newConnection = new Connection(connectionSubpageContext, connectionSubpageSource, connectionSubpageDestination);
                            newConnection.setSocketType(copyDataPort.getDirection());
                            if("Wait".equals(copyDataPort.getName())){
                                newConnection.setTimed(Boolean.TRUE);
                            }
                            cache.addConnection(newConnection);

                            if("Wait".equals(copyDataPort.getName())){
                                Connection returnConnection = new Connection(connectionSubpageContext, connectionSubpageSource, connectionSubpageDestination);
                                String oppositeDirection = "in".equals(copyDataPort.getDirection()) ? "out" : "in";
                                returnConnection.setSocketType(oppositeDirection);
                                returnConnection.setTimed(Boolean.TRUE);
                                returnConnection.setPeriodArc("1@+"+ periodValue);
                                cache.addConnection(returnConnection);
                            }

                            cache.getSOCKETS().add(new Socket(componentInstanceNested.getId(), copyDataPort.getId(), dataPort.getId(), dataPort.getDirection()));

                        }

                    }
                }
                // zagniezdzone komponenenty
                NodeList nestedComponents = actualComponent.getElementsByTagName("componentInstance");
                if (nestedComponents.getLength() != 0) {
                    searchElements(nestedComponents, componentInstance);

                } else {
                    if (!cache.isUniqueComponentsContain(componentInstance.getName())) {
                        cache.addElementToComponentInstances(componentInstance);
                        cache.addElementToUniqueComponents(componentInstance.getName());
                    }
                }

            }
        }

    }

    public void searchConnections(NodeList connections) {
        for (int i = 0; i < connections.getLength(); i++) {
            Node connection = connections.item(i);
            System.out.println("\nCurrent Element :" + connection.getNodeName());
            Element actualConnection = (Element) connection;
            if(!"accessConnection".equals(actualConnection.getAttribute("kind"))){
            System.out.println("Name of  connection : " + actualConnection.getAttribute("name"));
            NodeList connectionReferences = actualConnection.getElementsByTagName("connectionReference");
            String contextRaw = connectionReferences.item(0).getAttributes().getNamedItem("context").getNodeValue();
            System.out.println("context of  connection : " + contextRaw);

            System.out.println("source of  connection : " + actualConnection.getAttribute("source"));
            System.out.println("destination of  connection : " + actualConnection.getAttribute("destination"));
            System.out.println("destination of  connection : " + actualConnection.getAttribute("destination"));

            String context = contextRaw.replaceAll("\\D+", " ").trim();
            String source = actualConnection.getAttribute("source").replaceAll("\\D+", " ").trim();
            String destination = actualConnection.getAttribute("destination").replaceAll("\\D+", " ").trim();

            Connection newConnection = new Connection(context, source, destination);


            ArrayList<Integer> destinationPath = TranslatorTools.preparePorts(destination);
            ArrayList<Integer> sourcePath = TranslatorTools.preparePorts(source);


            if (destinationPath.get(0) != null && Category.PROCESS.getValue().equals(cache.getComponentInstanceByIndex(destinationPath.get(0)).getCategory()) &&
                    !Category.PROCESS.getValue().equals(cache.getComponentInstanceByIndex(sourcePath.get(0)).getCategory())) {
                String additionalConnContext = destinationPath.get(0).toString();
                String additionalConnSource = destination;
                String additionalConnDestination = destination.substring(0, destination.length() - 1);
                Connection additionalConnConnection = new Connection(additionalConnContext, additionalConnSource, additionalConnDestination);
                additionalConnConnection.setGenerate(Boolean.TRUE);
                additionalConnConnection.setSocketType("in");
                ConnectionNode connectionNode = getConnectionNode(destinationPath, null, null);
                petriNetPager.addNewPage(context, cache.getComponentInstanceByIndex(destinationPath.get(0)).getId(), Boolean.FALSE, null, connectionNode.getTransName());
                cache.addConnection(additionalConnConnection);
            }
            //dodanie połaczenia jesli to jest socket Out
            else if (sourcePath.get(0) != null && Category.PROCESS.getValue().equals(cache.getComponentInstanceByIndex(sourcePath.get(0)).getCategory()) &&
                    !Category.PROCESS.getValue().equals(cache.getComponentInstanceByIndex(destinationPath.get(0)).getCategory())) {

                //kolejna zaślepka az sie zrobie page i context!!!!!1
                String additionalConnContext = "";
                String additionalConnSource = source;
                String additionalConnDestination = destination;
                Connection additionalConnConnection = new Connection(additionalConnContext, additionalConnSource, additionalConnDestination);
                additionalConnConnection.setGenerate(Boolean.TRUE);
                additionalConnConnection.setSocketType("out");
                ConnectionNode connectionNode = getConnectionNode(sourcePath, null, null);
                petriNetPager.addNewPage(context, cache.getComponentInstanceByIndex(sourcePath.get(0)).getId(), Boolean.FALSE, null, cache.getComponentInstanceByIndex(sourcePath.get(0)).getName());
                cache.addConnection(additionalConnConnection);
            }

            cache.addConnection(newConnection);

        }}
        cache.sortConnections();

    }

    public ConnectionNode getConnectionNode(List<Integer> path, ComponentInstance actualComponentInstance, ComponentInstance headComponent) {

        for (int j = 0; j < path.size(); ++j) {
            ComponentInstance processingComponent = actualComponentInstance != null ?
                    actualComponentInstance : cache.getComponentInstanceByIndex(path.get(j));
            if (j == path.size() - 1) {
                return new ConnectionNode(processingComponent.getId(), null, processingComponent.getCategory(), null, null,
                        processingComponent.getPeriod(), processingComponent.getPos_X(), processingComponent.getPos_Y(), processingComponent.getName());
            } else if (j == path.size() - 2) {
                String headComponentId = headComponent != null ? headComponent.getId() : null;
                String headCategory = headComponent != null ? headComponent.getCategory() : null;

                return new ConnectionNode(processingComponent.getId(), processingComponent.getDataPort().get(path.get(j + 1)).getId(),
                        processingComponent.getCategory(), headComponentId, headCategory, processingComponent.getPeriod(),
                        processingComponent.getPos_X(), processingComponent.getPos_Y(), processingComponent.getName());
            } else {
                return getConnectionNode(path.subList(j + 1, path.size()), processingComponent.getComponentInstancesNested().get(path.get(j + 1)),
                        processingComponent);
            }
        }
        return null;
    }




    public void insertPort(List<Integer> path, ComponentInstance actualComponentInstance, DataPort portToInsert) {
        for (int j = 0; j < path.size(); ++j) {
            ComponentInstance processingComponent = actualComponentInstance != null ?
                    actualComponentInstance : cache.getComponentInstanceByIndex(path.get(j));
            if (j == path.size() - 1) {
                processingComponent.getDataPort().add(portToInsert);
            }
            else {
                 insertPort(path.subList(j + 1, path.size()), processingComponent.getComponentInstancesNested().get(path.get(j+1)),
                        portToInsert);
            }
        }

    }


    public void searchSystemName(Element systemInstance) {
        cache.setSystemName(systemInstance.getAttribute("name"));
    }
}
