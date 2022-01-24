package pl.pjtom;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;

public class App {
    public static void main(String[] args) {
        try {
            CassandraConnector cassClient = new CassandraConnector();
            cassClient.connect("127.0.0.1", 9042, "Cassandromat");
            if (args.length >= 1 && args[0].equals("create_data")) {
                DataCreator.loadDataIntoCassandra(cassClient);
            }
            StressTester stressTester = new StressTester(cassClient);
            stressTester.run();
        } catch (CassandraBackendException e) {
            e.printStackTrace();
        }
    }
}
