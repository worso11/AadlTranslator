package lukowicz.application.petrinet;

import lukowicz.application.data.Page;
import lukowicz.application.memory.Cache;
import lukowicz.application.utils.TranslatorTools;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class PetriNetPager {

    private Cache cache = Cache.getInstance();

    public PetriNetPager(){}

    public List<Page> getPages() {
        return cache.getPages();
    }

    public ArrayList<String> getInstancesBinders() {
        return cache.getInstancesBinders();
    }

    public Page getPageByIndex(int numberPage) {
        return getPages().get(numberPage);
    }

    public  String getPageIdByIndex(int numberPage) {
        return getPageByIndex(numberPage).getPageId();
    }

    public  String getPageIdForTransId(String transId) {
        return getPages().stream().filter(e -> transId.equals(e.getTransId())).findFirst().get().getPageId();
    }

    public Page getPageForTransId(String transId) {
        return getPages().stream().filter(e -> transId.equals(e.getTransId())).findFirst().orElse(null);
    }

    public  String getTransIdByIndex(int numberPage) {
        return getPageByIndex(numberPage).getTransId();
    }

    public void addNewPage(String context, String transId, Boolean isGenerated) {
        long count = getPages().stream().filter(e -> e.getContext().equals(context)).count();
        if (count == 0) {
            Page newPage = new Page(context, isGenerated);
            if (!"".equals(context)) {
                newPage.setTransId(transId);
            }
            getPages().add(newPage);
        }
        cache.sortPages();
    }

    public Element generateNewPage(String pageId, Document pnmlDocument, Element root) {
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

    public  Element generatePagesInstances(Document pnmlDocument) {
        Element instances = pnmlDocument.createElement("instances");
        Element firstInstance = pnmlDocument.createElement("instance");
        Integer numberPage = 0;
        Attr idAttr = pnmlDocument.createAttribute("id");
        String idAttrValue = TranslatorTools.generateUUID();
        idAttr.setValue(idAttrValue);
        getInstancesBinders().add(idAttrValue);
        Attr pageAttr = pnmlDocument.createAttribute("page");
        String firstPageId = getPageIdByIndex(numberPage);
        pageAttr.setValue(firstPageId);
        firstInstance.setAttributeNode(idAttr);
        firstInstance.setAttributeNode(pageAttr);
        instances.appendChild(firstInstance);
        for (Page page : getPages().subList(1, getPages().size())) {
            Element instance = pnmlDocument.createElement("instance");
            Attr newPageIdAttr = pnmlDocument.createAttribute("id");
            String newPageIdAttrValue = TranslatorTools.generateUUID();
            newPageIdAttr.setValue(newPageIdAttrValue);
            getInstancesBinders().add(newPageIdAttrValue);
            Attr newTransAttr = pnmlDocument.createAttribute("trans");
            newTransAttr.setValue(getTransIdByIndex(++numberPage));
            instance.setAttributeNode(newPageIdAttr);
            instance.setAttributeNode(newTransAttr);
            //wtedy na tym samym poziomie procesy
            firstInstance.appendChild(instance);
        }
        return instances;
    }

}
