package pl.pjtom.model;

public enum PackageLogEvent {
    CREATION(0),
    TAKE_PACKAGE_FROM_WAREHOUSE(1),
    PUT_PACKAGE_IN_POSTBOX(2),
    PICKUP_PACKAGE_FROM_POSTBOX(3);

    private int value;
    PackageLogEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
