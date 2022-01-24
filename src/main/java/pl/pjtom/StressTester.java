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

    public StressTester(CassandraConnector cassClient) throws CassandraBackendException {
        this.cassClient = cassClient;
        ArrayList<CourierModel> courierModels = cassClient.getCouriers();
        ArrayList<ClientModel> clientModels = cassClient.getClients();
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
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
