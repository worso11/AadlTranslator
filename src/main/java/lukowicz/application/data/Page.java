package lukowicz.application.data;

import lukowicz.application.utils.TranslatorTools;

public class Page implements Comparable<Page> {
    private String context;
    private String pageId;
    private String transId;

    public Page(String context) {
        this.context = context;
        this.pageId = TranslatorTools.generateUUID();
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

    @Override
    public int compareTo(Page o) {
        return this.getContext().compareTo(o.getContext());
    }
}