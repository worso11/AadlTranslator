package lukowicz.application.memory;

import lukowicz.application.data.*;
import lukowicz.application.utils.ParserTools;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

public class Cache {
    private static Set<String> uniqueComponents = new HashSet<>();
    private static List<ComponentInstance> COMPONENT_INSTANCES = new ArrayList<>();
    private static List<ComponentInstance> PROCESSES = new ArrayList<>();
    private static List<Connection> CONNECTIONS = new ArrayList<>();
    private static List<Page> pages = new ArrayList<>();
    private static ArrayList<String> INSTANCES_BINDERS = new ArrayList<>();
    private static ArrayList<Socket> SOCKETS = new ArrayList<>();
    private static Set<String> usedFeature = new HashSet<>();

    public static Socket isConnectingPort(FeatureInstance featureInstance) {
        for (int i = 0; i < SOCKETS.size(); ++i) {
            Socket socket = SOCKETS.get(i);
            if (featureInstance.getId().equals(socket.getPortId()) || featureInstance.getId().equals(socket.getSocketId())) {
                return socket;
            }
        }
        return null;
    }

    public static void moveProcesses() {
        for (int i = 0; i < COMPONENT_INSTANCES.size(); ++i) {
            if (COMPONENT_INSTANCES.get(i).getCategory().equals(Category.PROCESS.getValue())) {
                PROCESSES.add(COMPONENT_INSTANCES.get(i));
            }
        }
    }

    public static Set<String> getUniqueComponents() {
        return uniqueComponents;
    }

    public static List<ComponentInstance> getComponentInstances() {
        return COMPONENT_INSTANCES;
    }

    public static List<ComponentInstance> getPROCESSES() {
        return PROCESSES;
    }

    public static List<Connection> getCONNECTIONS() {
        return CONNECTIONS;
    }

    public static List<Page> getPages() {
        return pages;
    }

    public static ArrayList<String> getInstancesBinders() {
        return INSTANCES_BINDERS;
    }

    public static ArrayList<Socket> getSOCKETS() {
        return SOCKETS;
    }

    public static Set<String> getUsedFeature() {
        return usedFeature;
    }

    public static Page getPageByIndex(int numberPage) {
        return pages.get(numberPage);
    }

    public static String getPageIdByIndex(int numberPage) {
        return getPageByIndex(numberPage).getPageId();
    }

    public static String getPageForTransId(String transId) {
        return pages.stream().filter(e -> transId.equals(e.getTransId())).findFirst().get().getPageId();
    }

    public static String getTransIdByIndex(int numberPage) {
        return getPageByIndex(numberPage).getTransId();
    }

    public static void addNewPage(String context, String transId) {
        long count = pages.stream().filter(e -> e.getContext().equals(context)).count();
        if (count == 0) {
            Page newPage = new Page(context);
            if (!"".equals(context)) {
                newPage.setTransId(transId);
            }
            pages.add(newPage);
        }
        Collections.sort(pages);
    }

    public static Element generateNewPage(String pageId, Document pnmlDocument, Element root) {
        Element page = pnmlDocument.createElement("page");
        Attr pageIdAttr = pnmlDocument.createAttribute("id");
        pageIdAttr.setValue(pageId);
        page.setAttributeNode(pageIdAttr);
        Element pageAttr = pnmlDocument.createElement("pageattr");
        Attr pageAttrName = pnmlDocument.createAttribute("name");
        pageAttrName.setValue("pageName");
        pageAttr.setAttributeNode(pageAttrName);
        page.appendChild(pageAttr);
        root.appendChild(page);
        return page;
    }

    public static Element generatePagesInstances(Document pnmlDocument) {
        Element instances = pnmlDocument.createElement("instances");
        Element firstInstance = pnmlDocument.createElement("instance");
        Integer numberPage = 0;
        Attr idAttr = pnmlDocument.createAttribute("id");
        String idAttrValue = ParserTools.generateUUID();
        idAttr.setValue(idAttrValue);
        Cache.getInstancesBinders().add(idAttrValue);
        Attr pageAttr = pnmlDocument.createAttribute("page");
        String firstPageId = Cache.getPageIdByIndex(numberPage);
        pageAttr.setValue(firstPageId);
        firstInstance.setAttributeNode(idAttr);
        firstInstance.setAttributeNode(pageAttr);
        instances.appendChild(firstInstance);
        for (Page page : Cache.getPages().subList(1, Cache.getPages().size())) {
            Element instance = pnmlDocument.createElement("instance");
            Attr newPageIdAttr = pnmlDocument.createAttribute("id");
            String newPageIdAttrValue = ParserTools.generateUUID();
            newPageIdAttr.setValue(newPageIdAttrValue);
            Cache.getInstancesBinders().add(newPageIdAttrValue);
            Attr newTransAttr = pnmlDocument.createAttribute("trans");
            newTransAttr.setValue(Cache.getTransIdByIndex(++numberPage));
            instance.setAttributeNode(newPageIdAttr);
            instance.setAttributeNode(newTransAttr);
            //wtedy na tym samym poziomie procesy
            firstInstance.appendChild(instance);
        }
        return instances;
    }
}
