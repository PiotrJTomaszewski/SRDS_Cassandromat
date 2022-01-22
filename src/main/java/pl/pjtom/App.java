package pl.pjtom;

import pl.pjtom.cassandra.CassandraBackendException;

public class App {
    public static void main( String[] args ) {
        TemporaryMain tmp = new TemporaryMain();
        try {
            tmp.run();
        } catch (CassandraBackendException e) {
            e.printStackTrace();
        }
    }
}
