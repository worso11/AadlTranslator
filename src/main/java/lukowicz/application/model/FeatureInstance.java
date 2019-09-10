package lukowicz.application.model;

import java.util.Objects;
import java.util.UUID;

public class FeatureInstance {
     private String name;
     private String id;
     private Double pos_X;
     private Double pos_Y;

    public FeatureInstance(String name) {
        this.name = name;
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.pos_X = Constants.getPLACE_X_POSITION();
        this.pos_Y = Constants.getPLACE_Y_POSITION();

        System.out.println("name feature "+ name + " id "+id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getPos_X() {
        return pos_X;
    }

    public Double getPos_Y() {
        return pos_Y;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureInstance that = (FeatureInstance) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
