package lukowicz.application.model;

public enum Category {
    DEVICE("device"), PROCESS("process"), THREAD("thread"),
    PROCESSOR("processor"), MEMEORY("memory");
    String value;
    private Category(String value) {
     this.value = value;
    }
    public String getValue() {
        return value;
    }
}
