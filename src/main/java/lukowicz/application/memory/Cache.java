package lukowicz.application.memory;

import lukowicz.application.data.*;

import java.util.*;

public class Cache {

    private static volatile Cache instance;
    private  Set<String> uniqueComponents = new HashSet<>();
    private  List<ComponentInstance> COMPONENT_INSTANCES = new ArrayList<>();
    private  List<ComponentInstance> PROCESSES = new ArrayList<>();
    private  List<Connection> CONNECTIONS = new ArrayList<>();
    private  List<Page> pages = new ArrayList<>();
    private  ArrayList<String> INSTANCES_BINDERS = new ArrayList<>();
    private  ArrayList<Socket> SOCKETS = new ArrayList<>();
    private  Set<String> usedFeature = new HashSet<>();
    private  Set<DataPort> generatedPlaces = new HashSet<>();

    private Cache() {}

    public static Cache getInstance() {
        if(instance == null) {
            synchronized (Cache.class) {
                if(instance == null) {
                    instance = new Cache();
                }
            }
        }
        return instance;
    }



    public  Socket isConnectingPort(DataPort dataPort) {
        for (int i = 0; i < SOCKETS.size(); ++i) {
            Socket socket = SOCKETS.get(i);
            if (dataPort.getId().equals(socket.getPortId()) || dataPort.getId().equals(socket.getSocketId())) {
                return socket;
            }
        }
        return null;
    }

    public  void moveProcesses() {
        for (int i = 0; i < COMPONENT_INSTANCES.size(); ++i) {
            if (COMPONENT_INSTANCES.get(i).getCategory().equals(Category.PROCESS.getValue())) {
                PROCESSES.add(COMPONENT_INSTANCES.get(i));
                movePeriodThread(COMPONENT_INSTANCES.get(i));
            }
        }
    }

    public void movePeriodThread(ComponentInstance componentInstance) {
        if(componentInstance.getComponentInstancesNested() != null){
            for (int i = 0; i < componentInstance.getComponentInstancesNested().size(); ++i) {
                if ((componentInstance.getComponentInstancesNested().get(i).getCategory().equals(Category.THREAD.getValue()) && !"".equals(componentInstance.getComponentInstancesNested().get(i).getPeriod()))) {
                    PROCESSES.add(componentInstance.getComponentInstancesNested().get(i));
                }
            }
        }
    }

    public  Boolean isUniqueComponentsContain(String nameComponent) {
        return uniqueComponents.contains(nameComponent);
    }

    public  void addElementToUniqueComponents(String nameComponent){
        uniqueComponents.add(nameComponent);
    }

    public  List<ComponentInstance> getComponentInstances() {
        return COMPONENT_INSTANCES;
    }

    public void addElementToComponentInstances(ComponentInstance componentInstance){
        COMPONENT_INSTANCES.add(componentInstance);
    }

    public ComponentInstance getComponentInstanceByIndex(Integer index){
        return COMPONENT_INSTANCES.get(index);
    }

    public  List<ComponentInstance> getPROCESSES() {
        return PROCESSES;
    }

    public  List<Connection> getCONNECTIONS() {
        return CONNECTIONS;
    }

    public void addConnection(Connection connection) {
        CONNECTIONS.add(connection);
    }
    public void sortConnections(){
        CONNECTIONS.sort(Comparator.comparing(Connection::getContext));

    }
    public  List<Page> getPages() {
        return pages;
    }

    public  ArrayList<String> getInstancesBinders() {
        return INSTANCES_BINDERS;
    }

    public  ArrayList<Socket> getSOCKETS() {
        return SOCKETS;
    }

    public  Set<String> getUsedFeature() {
        return usedFeature;
    }

    public  Set<DataPort> getGeneratedPlaces() {
        return generatedPlaces;
    }

    public void sortPages(){
       Collections.sort(pages);
    }

    // pager
    public  void clearUsedFeature(){
        usedFeature.clear();
    }

    public  void clearGeneratedPlaces(){
        generatedPlaces.clear();
    }



}
