package pl.pjtom.model;

public enum PackageSize {
    SMALL,
    MEDIUM,
    LARGE;

    public static PackageSize fromInt(int size) throws PackageSizeException {
        switch (size) {
            case 0:
                return SMALL;
            case 1:
                return MEDIUM;
            case 2:
                return LARGE;
        }
        throw new PackageSizeException("Package size doesn't exist");
    }

    public static int toInt(PackageSize size) throws PackageSizeException {
        switch (size) {
            case SMALL:
                return 0;
            case MEDIUM:
                return 1;
            case LARGE:
                return 2;
        }
        throw new PackageSizeException("Package size doesn't exist");
    }

    public static PackageSize getBiggerSize(PackageSize size) throws PackageSizeException {
        switch (size) {
            case SMALL:
                return MEDIUM;
            case MEDIUM:
                return LARGE;
            default:
                break;
        }
        throw new PackageSizeException("Package size doesn't exist");
    }

    
}