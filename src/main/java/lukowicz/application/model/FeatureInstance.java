package lukowicz.application.model;

import java.util.Objects;
import java.util.UUID;

public class FeatureInstance {
     private String name;
     private String id;

    public FeatureInstance(String name) {
        this.name = name;
        this.id = UUID.randomUUID().toString().replace("-", "");;
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
