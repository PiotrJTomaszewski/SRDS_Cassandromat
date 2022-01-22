package pl.pjtom;

import java.util.ArrayList;
import java.util.EnumMap;

import com.datastax.driver.core.Session;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.ClientModel;
import pl.pjtom.model.PackageModel;
import pl.pjtom.model.PackageSize;

public class TemporaryMain {
    private Session session;

    public void run() throws CassandraBackendException {
        CassandraConnector cassClient = new CassandraConnector();
        cassClient.connect("127.0.0.1", 9042, "Cassandromat");
        session = cassClient.getSession();

        Courier courier = new Courier(cassClient);
        courier.generateCourierID();
        ClientModel client = new ClientModel();
        client.generateClientID();
        
        PackageModel pack = new PackageModel();
        pack.generatePackageID();
        pack.setClientID(client.getClientID());
        pack.setDistrictDest("Łazarz");
        pack.setSize(PackageSize.MEDIUM);

        cassClient.upsertPackageInWarehouse(pack);

        courier.loadTheTrunk();
    }
}
