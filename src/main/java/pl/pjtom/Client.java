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
import pl.pjtom.model.PostBoxModel;
import pl.pjtom.model.PackageLogEntryModel;

public class Client implements Runnable {
    private CassandraConnector cassClient;
    private ClientModel clientModel;
    private Random rand = new Random();
    private ArrayList<PostBoxModel> postBoxesInMyDistrict;

    public Client(CassandraConnector cassClient, ClientModel clientModel) throws CassandraBackendException {
        this.cassClient = cassClient;
        this.clientModel = clientModel;
        postBoxesInMyDistrict = cassClient.getPostBoxesInDistrict(clientModel.getDistrict());
    }

    public void pickupPackages() throws CassandraBackendException {
        for (PostBoxModel postBox: postBoxesInMyDistrict) {
            ArrayList<PackageModel> packagesForMe = cassClient.getPackagesInPostBoxByClientID(postBox.getPostBoxID(), clientModel.getClientID());

            for (PackageModel p: packagesForMe) {
                if (p.getIsReadyToPickup()) {
                    Date timestamp = new Date(System.currentTimeMillis());
                    cassClient.deletePackageFromPostBox(postBox.getPostBoxID(), p.getPackageID());
                    cassClient.upsertPackageLog(new PackageLogEntryModel(p.getPackageID(), PackageLogEvent.PICKUP_PACKAGE_FROM_POSTBOX, timestamp, clientModel.getClientID(), p.getPostBoxID()));
                    System.out.println(clientModel.getClientID() + ": Taking package " + p.getPackageID() + " from postbox " + postBox.getPostBoxID());
                }
            }
            try {
                Thread.sleep(100 + rand.nextInt(10));
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
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
