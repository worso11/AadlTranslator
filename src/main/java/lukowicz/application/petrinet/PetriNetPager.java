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


    public  String getPageIdForTransId(String transId) {
        return getPages().stream().filter(e -> transId.equals(e.getTransId())).findFirst().get().getPageId();
    }

    public  Page getPageByContext(String context) {
        return getPages().stream().filter(e -> context.equals(e.getContext())).findFirst().get();
    }

    public Page getPageForTransId(String transId) {
        return getPages().stream().filter(e -> transId.equals(e.getTransId())).findFirst().orElse(null);
    }


    public void addNewPage(String context, String transId, Boolean isGenerated, String headId, String pageName) {
        Page newPage;
        long count = getPages().stream().filter(e -> e.getContext().equals(context)).count();
        if (count == 0) {
            newPage = new Page(context, isGenerated, headId, pageName);
            if (!"".equals(context)) {
                newPage.setTransId(transId);
            }
            getPages().add(newPage);
        }
    }

    public void preparePagesList(){
        cache.sortPages();
        for (int i = 1; i < cache.getPages().size(); i++) {
            if(cache.getPages().get(i).getHeadId() == null){
                cache.getPages().get(0).getNestedPage().add(cache.getPages().get(i));
            }
            if(cache.getPages().get(i).getHeadId() != null){
                for(int j = 0 ; j < cache.getPages().get(0).getNestedPage().size(); ++j){
                    if(cache.getPages().get(0).getNestedPage().get(j).getTransId().equals(cache.getPages().get(i).getHeadId())){
                        cache.getPages().get(0).getNestedPage().get(j).getNestedPage().add(cache.getPages().get(i));
                    }
                }
            }

        }

    }

    public Element generateNewPage(String pageId, Document pnmlDocument, Element root, String transName) {
        Element page = pnmlDocument.createElement("page");
        Attr pageIdAttr = pnmlDocument.createAttribute("id");
        pageIdAttr.setValue(pageId);
        page.setAttributeNode(pageIdAttr);
        Element pageAttr = pnmlDocument.createElement("pageattr");
        Attr pageAttrName = pnmlDocument.createAttribute("name");
        pageAttrName.setValue(transName);
        pageAttr.setAttributeNode(pageAttrName);
        page.appendChild(pageAttr);
        root.appendChild(page);
        return page;
    }

    public  Element generatePagesInstances(Document pnmlDocument) {
        preparePagesList();
        Element instances = pnmlDocument.createElement("instances");

        Element generalInstance = pnmlDocument.createElement("instance");
        Attr generalInstanceIdAttr = pnmlDocument.createAttribute("id");
        String generalInstanceIdAttrValue = TranslatorTools.generateUUID();
        generalInstanceIdAttr.setValue(generalInstanceIdAttrValue);
        getInstancesBinders().add(generalInstanceIdAttrValue);
        Attr generalPageAttr = pnmlDocument.createAttribute("page");
        Page generalSystemPage = getPageByContext("General_System");
        generalPageAttr.setValue(generalSystemPage.getPageId());
        generalInstance.setAttributeNode(generalInstanceIdAttr);
        generalInstance.setAttributeNode(generalPageAttr);


        Element generalTransInstance = pnmlDocument.createElement("instance");
        Attr generalTransInstanceIdAttr = pnmlDocument.createAttribute("id");
        String generalTransInstanceIdAttrValue = TranslatorTools.generateUUID();
        generalTransInstanceIdAttr.setValue(generalTransInstanceIdAttrValue);
        getInstancesBinders().add(generalTransInstanceIdAttrValue);
        Attr generalTransAttr = pnmlDocument.createAttribute("trans");
        String generalTransSystemId = getPageByContext("General_System").getTransId();
        generalTransAttr.setValue(generalTransSystemId);
        generalTransInstance.setAttributeNode(generalTransInstanceIdAttr);
        generalTransInstance.setAttributeNode(generalTransAttr);
        generalInstance.appendChild(generalTransInstance);

        instances.appendChild(generalInstance);

        generateNestedInstance(pnmlDocument,getPages().get(0).getNestedPage(), generalTransInstance);
        return instances;
    }

    private void generateNestedInstance(Document pnmlDocument,List<Page> pageList, Element firstInstance) {
        for (int i = 0; i < pageList.size(); ++i) {
            Element instance = pnmlDocument.createElement("instance");
            Attr newPageIdAttr = pnmlDocument.createAttribute("id");
            String newPageIdAttrValue = TranslatorTools.generateUUID();
            newPageIdAttr.setValue(newPageIdAttrValue);
            getInstancesBinders().add(newPageIdAttrValue);
            Attr newTransAttr = pnmlDocument.createAttribute("trans");
            newTransAttr.setValue(pageList.get(i).getTransId());
            instance.setAttributeNode(newPageIdAttr);
            instance.setAttributeNode(newTransAttr);
            firstInstance.appendChild(instance);
            if(!pageList.get(i).getNestedPage().isEmpty()){
                generateNestedInstance(pnmlDocument,getPages().get(0).getNestedPage().get(i).getNestedPage(),instance);
            }
        }
    }

}
