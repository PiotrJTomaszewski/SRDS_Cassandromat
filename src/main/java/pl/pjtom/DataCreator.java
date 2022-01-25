package pl.pjtom;

import java.util.ArrayList;
import java.util.Random;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.ClientModel;
import pl.pjtom.model.CourierModel;
import pl.pjtom.model.PackageModel;
import pl.pjtom.model.PostBoxModel;

public class DataCreator {
    static private Random rand = new Random();
    static private ArrayList<ClientModel> clients;

    public static void loadDataIntoCassandra(CassandraConnector cassClient, boolean small_scale) {
        // String[] districts = {"Grunwald", "Jeżyce", "Nowe Miasto", "Stare Miasto", "Wilda"};
        String[] districts = {"Grunwald", "Jeżyce", "Wilda"};

        try {
            // Create districts
            for (String district: districts) {
                cassClient.upsertDistrict(district);
            }

            // Create post boxes
            for (String district: districts) {
                int postBoxCount = 2;
                for (int i=0; i<postBoxCount; i++) {
                    PostBoxModel postBox = new PostBoxModel();
                    postBox.generatePostboxID();
                    postBox.setDistrict(district);
                    postBox.setCapacity(rand.nextInt(3) + 2);
                    cassClient.upsertPostbox(postBox);
                }
            }

            // Create couriers
            int courierCount;
            if (small_scale) {
                courierCount = 1;
            } else {
                courierCount = 50;
            }
            for (int i=0; i<courierCount; i++) {
                CourierModel courier = new CourierModel();
                courier.generateCourierID();
                courier.setCapacity(rand.nextInt(2) + 2);
                cassClient.upsertCourier(courier);
            }

            // Create clients
            int clientCount;
            if (small_scale) {
                clientCount = 1;
            } else {
                clientCount = 10;
            }
            for (int i=0; i<clientCount; i++) {
                ClientModel client = new ClientModel();
                client.generateClientID();
                client.setDistrict(districts[rand.nextInt(districts.length)]);
                cassClient.upsertClient(client);
            }
            System.out.println("Data created");
        } catch (CassandraBackendException e) {
            System.err.println(e);
        }
    }

    public static void createSomePackages(CassandraConnector cassClient) throws CassandraBackendException {
        if (clients == null) {
            clients = cassClient.getClients();
        }
        int packagesToGenerate = rand.nextInt(20) + 20;
        for (int i=0; i<packagesToGenerate; i++) {
            ClientModel client = clients.get(rand.nextInt(clients.size()));
            PackageModel packageModel = new PackageModel();
            packageModel.generatePackageID();
            packageModel.setDistrictDest(client.getDistrict());
            packageModel.setClientID(client.getClientID());
            cassClient.upsertPackageInWarehouse(packageModel);
            // cassClient.upsertPackageLog(packageModel.getPackageID(), PackageLogEvent.CREATION, "Creator");
        }
    }
}
