package pl.pjtom.model;

public class PackageSizeException extends Exception {
    public PackageSizeException(String message) {
        super(message);
    }

    public PackageSizeException(Exception e) {
        super(e);
    }

    public PackageSizeException(String message, Exception e) {
        super(message, e);
    }
}