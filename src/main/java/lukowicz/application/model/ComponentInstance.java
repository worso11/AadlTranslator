package lukowicz.application.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ComponentInstance {
    private String name;
    private String category;
    private String id;
    private List<FeatureInstance> featureInstance = new ArrayList<>();
    private List<ComponentInstance> componentInstancesNested = new ArrayList<>();

    public ComponentInstance(String name, String category) {
        this.name = name;
        this.category = category;
        this.id = UUID.randomUUID().toString().replace("-", "");;
        System.out.println("name feature "+ name + " id "+id + "category "+category) ;
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

    public String getId() {
        return id;
    }

    public List<FeatureInstance> getFeatureInstance() {
        return featureInstance;
    }

    public List<FeatureInstance> getReverseFeatureInstances() {
        Collections.reverse(featureInstance);
        return  featureInstance;
    }

    public void setFeatureInstance(List<FeatureInstance> featureInstance) {
        this.featureInstance = featureInstance;
    }

    public List<ComponentInstance> getComponentInstancesNested() {
        return componentInstancesNested;
    }

    public void setComponentInstancesNested(List<ComponentInstance> componentInstancesNested) {
        this.componentInstancesNested = componentInstancesNested;
    }

    public void removeFeatureByName(String featureName){
        for(int i=0; i<featureInstance.size(); ++i){
            if(featureInstance.get(i).getName().equals(featureName)){
                featureInstance.remove(i);
            }
        }
    }


}
