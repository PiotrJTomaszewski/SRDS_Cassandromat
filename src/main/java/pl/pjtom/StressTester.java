package pl.pjtom;

import java.util.ArrayList;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.ClientModel;
import pl.pjtom.model.CourierModel;

public class StressTester {
    private CassandraConnector cassClient;
    private ArrayList<Courier> couriers = new ArrayList<>();
    private ArrayList<Client> clients = new ArrayList<>();

    public StressTester(CassandraConnector cassClient, int nodeCount, int nodeID) throws CassandraBackendException {
        this.cassClient = cassClient;
        ArrayList<CourierModel> courierModels = cassClient.getCouriers();
        ArrayList<ClientModel> clientModels = cassClient.getClients();
        // If there are other nodes present, use only a part of couriers and clients
        if (nodeCount > 1) {
            courierModels.removeIf(c -> (c.getCourierID().hashCode() % nodeCount) != nodeID);
            clientModels.removeIf(c -> (c.getClientID().hashCode() % nodeCount) != nodeID);
        }
        for (CourierModel courierModel: courierModels) {
            couriers.add(new Courier(cassClient, courierModel));
        }
        for (ClientModel clientModel: clientModels) {
            clients.add(new Client(cassClient, clientModel));
        }
    }

    public void run() throws CassandraBackendException {
        DataCreator.createSomePackages(cassClient);
        ArrayList<Thread> courierThreads = new ArrayList<>();
        ArrayList<Thread> clientThreads = new ArrayList<>();

        for (Courier courier: couriers) {
            courierThreads.add(new Thread(courier));
        }
        for (Client client: clients) {
            clientThreads.add(new Thread(client));
        }
        for (Thread courierThread: courierThreads) {
            courierThread.start();
        }
        for (Thread clientThread: clientThreads) {
            clientThread.start();
        }

        while (true) {
            DataCreator.createSomePackages(cassClient);
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
