package lukowicz.application.data;

public enum Category {
    DEVICE("device"), PROCESS("process"), THREAD("thread"), BUS("bus"),
    PROCESSOR("processor"), MEMORY("memory"), FEATURE("feature");
    String value;

    private Category(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
