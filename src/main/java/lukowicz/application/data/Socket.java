package lukowicz.application.data;

public class Socket {

    private final String componentId;
    private final String portId;
    private String socketId;
    private final String direction;

    public Socket(String componentId, String portId, String socketId, String direction) {
        this.componentId = componentId;
        this.portId = portId;
        this.socketId = socketId;
        this.direction = direction;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getPortId() {
        return portId;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {this.socketId = socketId; }
}
