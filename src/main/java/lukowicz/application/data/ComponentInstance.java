package lukowicz.application.data;

import lukowicz.application.utils.ParserTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentInstance {
    private String name;
    private String category;
    private String id;
    private Double pos_X;
    private Double pos_Y;
    private List<FeatureInstance> featureInstance = new ArrayList<>();
    private List<ComponentInstance> componentInstancesNested = new ArrayList<>();
    private String period;

    public ComponentInstance(String name, String category) {
        this.name = name;
        this.category = category;
        this.id = ParserTools.generateUUID();
        this.pos_X = GraphicPosition.getTRANSITION_X_POSITION();
        this.pos_Y = GraphicPosition.getTRANSITION_Y_POSITION();
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

    public List<FeatureInstance> getFeatureInstance() {
        return featureInstance;
    }

    public List<FeatureInstance> getReverseFeatureInstances() {
        Collections.reverse(featureInstance);
        return featureInstance;
    }


    public List<ComponentInstance> getComponentInstancesNested() {
        return componentInstancesNested;
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


}
