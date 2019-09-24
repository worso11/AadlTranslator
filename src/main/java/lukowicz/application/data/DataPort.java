package lukowicz.application.data;

import lukowicz.application.utils.TranslatorTools;

import java.util.Objects;

public class DataPort {
     private String name;
     private String id;
     private Double pos_X;
     private Double pos_Y;
     private String direction;

    public DataPort(String name, String direction) {
        this.name = name;
        this.id = TranslatorTools.generateUUID();
        this.direction = direction.equals("") ? "in" : direction;

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

    public void setPos_X(Double pos_X) {
        this.pos_X = pos_X;
    }

    public void setPos_Y(Double pos_Y) {
        this.pos_Y = pos_Y;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataPort that = (DataPort) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(id, that.id) &&
                Objects.equals(pos_X, that.pos_X) &&
                Objects.equals(pos_Y, that.pos_Y) &&
                Objects.equals(direction, that.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
