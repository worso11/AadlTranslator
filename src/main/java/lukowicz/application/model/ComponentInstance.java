package lukowicz.application.model;

import java.util.ArrayList;
import java.util.List;

public class ComponentInstance {
    private String name;
    private String category;
    private List<String> featureInstance = new ArrayList<>();
    private List<ComponentInstance> componentInstancesNested = new ArrayList<>();

    public ComponentInstance(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getFeatureInstance() {
        return featureInstance;
    }

    public void setFeatureInstance(List<String> featureInstance) {
        this.featureInstance = featureInstance;
    }

    public List<ComponentInstance> getComponentInstancesNested() {
        return componentInstancesNested;
    }

    public void setComponentInstancesNested(List<ComponentInstance> componentInstancesNested) {
        this.componentInstancesNested = componentInstancesNested;
    }


}
