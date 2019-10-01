package lukowicz.application.data;

import lukowicz.application.memory.ElementsPosition;
import lukowicz.application.utils.TranslatorTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentInstance {
    private String name;
    private String category;
    private String id;
    private Double pos_X;
    private Double pos_Y;
    private List<DataPort> dataPort = new ArrayList<>();
    private List<ComponentInstance> componentInstancesNested = new ArrayList<>();
    private String period;

    public ComponentInstance(String name, String category) {
        this.name = name;
        this.category = category;
        this.id = TranslatorTools.generateUUID();
        this.pos_X = ElementsPosition.getTRANSITION_X_POSITION();
        this.pos_Y = ElementsPosition.getTRANSITION_Y_POSITION();
        System.out.println("name feature " + name + " id " + id + "category " + category);
    }

    public String getName() {
        return name;
    }


    public String getCategory() {
        return category;
    }


    public String getId() {
        return id;
    }

    public List<DataPort> getDataPort() {
        return dataPort;
    }

    public List<DataPort> getReverseFeatureInstances() {
        Collections.reverse(dataPort);
        return dataPort;
    }


    public List<ComponentInstance> getComponentInstancesNested() {
        return componentInstancesNested;
    }

    public void setComponentInstancesNested(List<ComponentInstance> componentInstancesNested) {
        this.componentInstancesNested = componentInstancesNested;
    }


    public Double getPos_X() {
        return pos_X;
    }


    public Double getPos_Y() {
        return pos_Y;
    }


    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPeriod() {
        return period;
    }
}
