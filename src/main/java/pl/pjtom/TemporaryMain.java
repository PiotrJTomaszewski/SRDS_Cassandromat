package pl.pjtom;

import com.datastax.driver.core.Session;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.ClientModel;
import pl.pjtom.model.PackageModel;
import pl.pjtom.model.PostBoxModel;

public class TemporaryMain {
    private Session session;

    public void run() throws CassandraBackendException {
        // CassandraConnector cassClient = new CassandraConnector();
        // cassClient.connect("127.0.0.1", 9042, "Cassandromat");
        // session = cassClient.getSession();

        // courier.loadTheTrunk();
        // courier.deliverPackages();

        // client.pickupPackages();
    }
}
