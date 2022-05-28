package lukowicz.application.data;

import lukowicz.application.utils.TranslatorTools;

import java.util.ArrayList;
import java.util.List;

public class Page implements Comparable<Page> {
    private String context;
    private String pageId;
    private String transId;
    private Boolean generated;
    private List<Page> nestedPage = new ArrayList<>();
    private String headId;
    private String pageName;

    public Page(String context, Boolean generated, String headId, String pageName) {
        this.context = context;
        this.pageId = TranslatorTools.generateUUID();
        this.generated = generated;
        this.headId = headId;
        this.pageName = pageName;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getContext() {
        return context;
    }

    public String getPageId() {
        return pageId;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public Boolean getGenerated() {
        return generated;
    }


    public List<Page> getNestedPage() {
        return nestedPage;
    }


    public String getHeadId() {
        return headId;
    }


    public String getPageName() {
        return pageName;
    }


    @Override
    public int compareTo(Page o) {
        if (this.getContext().length() > o.getContext().length()) {
            return 1;
        } else if (this.getContext().length() < o.getContext().length()) {
            return -1;
        } else {
            return 0;
        }
    }
}