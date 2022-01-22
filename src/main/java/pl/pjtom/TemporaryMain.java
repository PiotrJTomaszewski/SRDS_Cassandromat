package pl.pjtom;

import java.util.ArrayList;

import com.datastax.driver.core.Session;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.Client;
import pl.pjtom.model.Courier;
import pl.pjtom.model.Package;
import pl.pjtom.model.PackageSize;

public class TemporaryMain {
    private Session session;

    public void run() throws CassandraBackendException {
        CassandraConnector cassClient = new CassandraConnector();
        cassClient.connect("127.0.0.1", 9042, "Cassandromat");
        session = cassClient.getSession();

        Courier courier = new Courier();
        courier.generateCourierID();
        Client client = new Client();
        client.generateClientID();
        
        Package pack = new Package();
        pack.generatePackageID();
        pack.setClientID(client.getClientID());
        pack.setDistrictDest("Łazarz");
        pack.setSize(PackageSize.MEDIUM);

        cassClient.upsertPackageInWarehouse(pack);

        ArrayList<Package> packages = cassClient.getPackagesInWarehouse();
        for (Package p: packages) {
            if (p.getCourierID() == null) {
                System.out.println(p.toString());
                p.setCourierID(courier.getCourierID());
                cassClient.upsertPackageInWarehouse(p);
                Package check_package = cassClient.getPackageInWarehouseByID(p.getPackageID());
                if (check_package != null && check_package.getCourierID() == courier.getCourierID()) {

                } 

            }
        }
    }
}
