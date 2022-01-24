package pl.pjtom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.ClientModel;
import pl.pjtom.model.PackageModel;

public class Client {
    private CassandraConnector cassClient;
    private ClientModel clientModel;

    public Client(CassandraConnector cassClient) {
        this.cassClient = cassClient;
        this.clientModel = new ClientModel();
        this.clientModel.generateClientID(); // TODO: Temporary
    }

    public void pickupPackages() throws CassandraBackendException {
        ArrayList<PackageModel> packagesForMe = cassClient.getPackagesInPostBoxesByClientID(clientModel.getClientID());
        HashMap<String, ArrayList<PackageModel>> packagesToPickupByPostBox = new HashMap<>();

        for (PackageModel p: packagesForMe) {
            if (p.getIsReadyToPickup()) {
                String postBoxID = p.getPostBoxID();
                if (!packagesToPickupByPostBox.containsKey(postBoxID)) {
                    packagesToPickupByPostBox.put(postBoxID, new ArrayList<PackageModel>());
                }
                packagesToPickupByPostBox.get(postBoxID).add(p);
            }
        }

        for (Entry<String, ArrayList<PackageModel>> entry: packagesToPickupByPostBox.entrySet()) {
            System.out.println("Traveling to post box " + entry.getKey() + ".");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Picking up " + entry.getValue().size() + " packages.");
            for (PackageModel p: entry.getValue()) {
                cassClient.deletePackageFromPostBox(entry.getKey(), p.getPackageID());
            }
        }
    }

    public ClientModel getClientModel() {
        return clientModel;
    }
}
