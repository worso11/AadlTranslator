package lukowicz.application.data;

import lukowicz.application.utils.TranslatorTools;

import java.util.Objects;

public class DataPort {
    private String name;
    private String id;
    private Double pos_X;
    private Double pos_Y;
    private String direction;
    private Boolean isTimed = Boolean.FALSE;

    public DataPort(String name, String direction) {
        this.name = name;
        this.id = TranslatorTools.generateUUID();
        this.direction = direction.equals("") ? "in" : direction;
    }

    public String getName() {
        return name;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getDirection() {
        return direction;
    }


    public Boolean getTimed() {
        return isTimed;
    }

    public void setTimed(Boolean timed) {
        isTimed = timed;
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
