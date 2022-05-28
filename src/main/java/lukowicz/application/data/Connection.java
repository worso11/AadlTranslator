package lukowicz.application.data;

import lukowicz.application.memory.ElementsPosition;
import lukowicz.application.utils.TranslatorTools;

public class Connection {
    private String context;
    private String source;
    private String destination;
    private String id;
    private String pos_X;
    private String pos_Y;
    private Boolean isGenerate = false;
    private String socketType;
    private Boolean isTimed = Boolean.FALSE;
    private String periodArc;

    public Connection(String context, String source, String destination) {
        this.context = context;
        this.source = source;
        this.destination = destination;
        this.id = TranslatorTools.generateUUID();
        this.pos_X = ElementsPosition.getArcXPosition();
        this.pos_Y = ElementsPosition.getArcYPosition();
    }

    public String getContext() {
        return context;
    }

    public String getSource() {
        return source;
    }


    public String getDestination() {
        return destination;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setGenerate(Boolean generate) {
        isGenerate = generate;
    }

    public Boolean getGenerate() {
        return isGenerate;
    }

    public String getSocketType() {
        return socketType;
    }

    public void setSocketType(String socketType) {
        this.socketType = socketType;
    }


    public void setTimed(Boolean timed) {
        isTimed = timed;
    }

    public String getPeriodArc() {
        return periodArc;
    }

    public void setPeriodArc(String periodArc) {
        this.periodArc = periodArc;
    }
}
