package lukowicz.application.model;

import lukowicz.application.tools.ParserTools;

public class Connection {
    private String context;
    private String source;
    private String destination;
    private String id;
    private String pos_X;
    private String pos_Y;

    public Connection(String context, String source, String destination) {
        this.context = context;
        this.source = source;
        this.destination = destination;
        this.id = ParserTools.generateUUID();
        this.pos_X = GraphicPosition.getArcXPosition();
        this.pos_Y = GraphicPosition.getArcYPosition();
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
}
