package lukowicz.application.model;

import java.util.UUID;

public class Connection {
    private String context;
    private String source;
    private String destination;
    private String id;

    public Connection(String context, String source, String destination) {
        this.context = context;
        this.source = source;
        this.destination = destination;
        this.id = UUID.randomUUID().toString().replace("-", "");;

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
}
