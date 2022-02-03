package pl.pjtom;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.ClientModel;
import pl.pjtom.model.PackageLogEvent;
import pl.pjtom.model.PackageModel;
import pl.pjtom.model.PackageLogEntryModel;

public class Client implements Runnable {
    private CassandraConnector cassClient;
    private ClientModel clientModel;
    private Random rand = new Random();

    public Client(CassandraConnector cassClient, ClientModel clientModel) {
        this.cassClient = cassClient;
        this.clientModel = clientModel;
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
            // System.out.println("Traveling to post box " + entry.getKey() + ".");
            try {
                Thread.sleep(100 + rand.nextInt(10));
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
            // System.out.println("Picking up " + entry.getValue().size() + " packages.");
            for (PackageModel p: entry.getValue()) {
                Date timestamp = new Date(System.currentTimeMillis());
                cassClient.deletePackageFromPostBox(entry.getKey(), p.getPackageID());
                cassClient.upsertPackageLog(new PackageLogEntryModel(p.getPackageID(), PackageLogEvent.PICKUP_PACKAGE_FROM_POSTBOX, timestamp, clientModel.getClientID(), p.getPostBoxID()));
                System.out.println("Taking package " + p.getPackageID() + " from postbox " + entry.getKey());
            }
        }
    }

    public ClientModel getClientModel() {
        return clientModel;
    }

    public String getClientID() {
        return clientModel.getClientID();
    }

    @Override
    public void run() {
        while (true) {
            try {
                pickupPackages();
                Thread.sleep(100);
            } catch (CassandraBackendException e) {
                System.err.println(e.getMessage());
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
