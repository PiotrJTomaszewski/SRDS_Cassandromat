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
    static private String[] districts = {"Grunwald", "Jeżyce", "Nowe Miasto", "Stare Miasto", "Wilda"};
    // static private String[] districts = {"Grunwald", "Jeżyce", "Wilda"};

    public static void createPostBoxesAndDistricts(CassandraConnector cassClient) {
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

            System.out.println("Districts & post boxes created");
        } catch (CassandraBackendException e) {
            System.err.println(e);
        }
    }

    public static ArrayList<CourierModel> createCouriers(CassandraConnector cassClient) throws CassandraBackendException {
        // Create couriers
        int courierCount = 20;
        ArrayList<CourierModel> couriers = new ArrayList<>();
        for (int i=0; i<courierCount; i++) {
            CourierModel courier = new CourierModel();
            courier.generateCourierID();
            courier.setCapacity(rand.nextInt(5) + 10);
            cassClient.upsertCourier(courier);
            couriers.add(courier);
        }
        return couriers;
    }

    public static ArrayList<ClientModel> createClients(CassandraConnector cassClient) throws CassandraBackendException {
        // Create clients
        int clientCount = 10;
        ArrayList<ClientModel> clients = new ArrayList<>();
        for (int i=0; i<clientCount; i++) {
            ClientModel client = new ClientModel();
            client.generateClientID();
            client.setDistrict(districts[i % districts.length]);
            cassClient.upsertClient(client);
            clients.add(client);
        }
        return clients;
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
