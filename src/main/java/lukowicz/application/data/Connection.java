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
        System.out.println("Connection " + " id "+id);
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPos_X() {
        return pos_X;
    }

    public void setPos_X(String pos_X) {
        this.pos_X = pos_X;
    }

    public String getPos_Y() {
        return pos_Y;
    }

    public void setPos_Y(String pos_Y) {
        this.pos_Y = pos_Y;
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

    public Boolean getTimed() {
        return isTimed;
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
