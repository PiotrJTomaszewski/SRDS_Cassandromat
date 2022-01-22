package pl.pjtom.model;

import pl.pjtom.cassandra.CassandraBackendException;

public enum PackageSize {
    SMALL,
    MEDIUM,
    LARGE;

    public static PackageSize fromInt(int size) {
        switch (size) {
            case 0:
                return SMALL;
            case 1:
                return MEDIUM;
            case 2:
                return LARGE;
        }
        return null;
    }

    public static int toInt(PackageSize size) {
        switch (size) {
            case SMALL:
                return 0;
            case MEDIUM:
                return 1;
            case LARGE:
                return 2;
        }
        return 0;
    }
}