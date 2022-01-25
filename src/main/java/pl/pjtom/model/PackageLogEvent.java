package pl.pjtom.model;

public enum PackageLogEvent {
    // CREATION(0),
    TAKE_PACKAGE_FROM_WAREHOUSE(0),
    PUT_PACKAGE_IN_POSTBOX(1),
    PICKUP_PACKAGE_FROM_POSTBOX(2);

    private int value;
    PackageLogEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PackageLogEvent fromInt(int value) {
        switch (value) {
            case 0:
                return TAKE_PACKAGE_FROM_WAREHOUSE;
            case 1:
                return PUT_PACKAGE_IN_POSTBOX;
            case 2:
                return PICKUP_PACKAGE_FROM_POSTBOX;
        }
        return null;
    }
}
