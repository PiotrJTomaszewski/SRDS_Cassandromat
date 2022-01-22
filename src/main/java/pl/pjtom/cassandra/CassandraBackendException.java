package pl.pjtom.cassandra;

public class CassandraBackendException extends Exception {
    public CassandraBackendException(String message) {
        super(message);
    }

    public CassandraBackendException(Exception e) {
        super(e);
    }

    public CassandraBackendException(String message, Exception e) {
        super(message, e);
    }
}
