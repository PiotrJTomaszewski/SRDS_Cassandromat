package pl.pjtom;

import java.util.ArrayList;

import com.datastax.driver.core.Session;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.ClientModel;
import pl.pjtom.model.CourierModel;
import pl.pjtom.model.PackageModel;
import pl.pjtom.model.PostBoxModel;

public class TemporaryMain {
    private Session session;

    public void run(CassandraConnector cassClient) throws CassandraBackendException {
        ArrayList<CourierModel> courierModels = cassClient.getCouriers();
        ArrayList<ClientModel> clientModels = cassClient.getClients();
        ArrayList<Courier> couriers = new ArrayList<>();
        for (CourierModel courierModel: courierModels) {
            couriers.add(new Courier(cassClient, courierModel));
        }
        ArrayList<Client> clients = new ArrayList<>();
        for (ClientModel clientModel: clientModels) {
            clients.add(new Client(cassClient, clientModel));
        }

        // courier.loadTheTrunk();
        // courier.deliverPackages();

        // client.pickupPackages();
    }
}
