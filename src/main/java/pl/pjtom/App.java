package pl.pjtom;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;

public class App {
    public static void main(String[] args) {
        try {
            CassandraConnector cassClient = new CassandraConnector();
            String nodeAddress = args.length > 1 ? args[1] : "127.0.0.1";
            int nodePort = args.length > 2 ? Integer.parseInt(args[2]) : 9042;

            cassClient.connect(nodeAddress, nodePort, "Cassandromat");
            switch (args[0]) {
                case "check_logs":
                    LogChecker logChecker = new LogChecker(cassClient);
                    logChecker.checkLogs();
                    break;
                case "stress_test":
                    int nodeCount = args.length > 3 ? Integer.parseInt(args[3]) : 1;
                    int nodeID = args.length > 4 ? Integer.parseInt(args[4]) : 0;
                    if (nodeID == 0) {
                        cassClient.truncatePostBoxContent();
                        cassClient.truncatePackageLog();
                        cassClient.truncateWarehouseContent();
                    }
                    StressTester stressTester = new StressTester(cassClient, nodeCount, nodeID);
                    stressTester.run();
                    break;
                case "create_data":
                    System.out.println("Removing old data. Please wait");
                    cassClient.truncatePostBox();
                    cassClient.truncateCourier();
                    cassClient.truncateClient();
                    cassClient.truncateDistrict();
                    DataCreator.loadDataIntoCassandra(cassClient, false);
                    break;
                default:
                    Menu menu = new Menu(cassClient);
                    menu.showMenu();
                    break;
            }
        } catch (CassandraBackendException e) {
            System.err.println(e.getMessage());
        }
    }
}
