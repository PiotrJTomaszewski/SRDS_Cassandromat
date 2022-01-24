package pl.pjtom.model;

public enum PackageLogEvent {
    A(0);

    private int value;
    PackageLogEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
