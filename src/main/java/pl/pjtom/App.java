package pl.pjtom;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;

public class App {
    public static void main(String[] args) {
        try {
            CassandraConnector cassClient = new CassandraConnector();
            cassClient.connect("127.0.0.1", 9042, "Cassandromat");
            System.out.println("Removing old data. Please wait");
            if (args.length >= 1 && args[0].equals("create_data")) {
                cassClient.truncatePostBox();
                cassClient.truncateCourier();
                cassClient.truncateClient();
                cassClient.truncateDistrict();
                DataCreator.loadDataIntoCassandra(cassClient, false);
            }
            cassClient.truncatePostBoxContent();
            cassClient.truncatePackageLog();
            cassClient.truncateWarehouseContent();
            cassClient.truncateCourierTrunkContent();
            StressTester stressTester = new StressTester(cassClient);
            stressTester.run();
        } catch (CassandraBackendException e) {
            System.err.println(e.getMessage());
        }
    }
}
