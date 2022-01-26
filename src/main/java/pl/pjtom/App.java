package pl.pjtom;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;

public class App {
    public static void main(String[] args) {
        try {
            CassandraConnector cassClient = new CassandraConnector();
            cassClient.connect("127.0.0.1", 9042, "Cassandromat");
            if (args.length >= 1 && args[0].equals("check_logs")) {
                LogChecker logChecker = new LogChecker(cassClient);
                logChecker.checkLogs();
            } else if (args.length >= 1 && args[0].equals("stress_test")) {
                System.out.println("Removing old data. Please wait");
                if (args.length >= 2 && args[1].equals("create_data")) {
                    cassClient.truncatePostBox();
                    cassClient.truncateCourier();
                    cassClient.truncateClient();
                    cassClient.truncateDistrict();
                    DataCreator.loadDataIntoCassandra(cassClient, false);
                }
                cassClient.truncatePostBoxContent();
                cassClient.truncatePackageLog();
                cassClient.truncateWarehouseContent();
                StressTester stressTester = new StressTester(cassClient);
                stressTester.run();
            } else {
                Menu menu = new Menu(cassClient);
                menu.showMenu();
            }
        } catch (CassandraBackendException e) {
            System.err.println(e.getMessage());
        }
    }
}
