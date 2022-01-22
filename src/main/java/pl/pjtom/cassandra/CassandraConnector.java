package pl.pjtom.cassandra;

import java.util.ArrayList;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster.Builder;

import pl.pjtom.model.PackageModel;
import pl.pjtom.model.PackageSize;
import pl.pjtom.model.PackageSizeException;

import com.datastax.driver.core.Session;

public class CassandraConnector {
    private Cluster cluster;
    private Session session;

    public void connect(String node, int port, String keyspace) throws CassandraBackendException {
        Builder b = Cluster.builder().addContactPoint(node);
        b.withPort(port);
        cluster = b.build();
        try {
            session =  cluster.connect(keyspace);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not connect to the cluster. " + e.getMessage() + ".", e);
        }
        prepareStatements();
    }

    public Session getSession() {
        return session;
    }

    public void close() {
        session.close();
        cluster.close();
    }

    private static PreparedStatement SELECT_PACKAGES_IN_WAREHOUSE;
    private static PreparedStatement SELECT_PACKAGE_IN_WAREHOUSE_BY_ID;
    private static PreparedStatement UPSERT_PACKAGE_IN_WAREHOUSE;
    private static PreparedStatement UPDATE_COURIER_ID_PACKAGE_IN_WAREHOUSE_BY_ID;
    private static PreparedStatement DELETE_PACKAGE_FROM_WAREHOUSE_BY_ID;

    private static PreparedStatement SELECT_PACKAGES_IN_POSTBOX;
    private static PreparedStatement INSERT_PACKAGE_INTO_POSTBOX;
    private static PreparedStatement DELETE_PACKAGE_FROM_POSTBOX;

    private static PreparedStatement SELECT_PACKAGE_LOG;
    private static PreparedStatement UPSERT_PACKAGE_INTO_LOG;

    private void prepareStatements() throws CassandraBackendException {
        try {
            SELECT_PACKAGES_IN_WAREHOUSE = session.prepare("SELECT * FROM WarehouseContent;");
            SELECT_PACKAGE_IN_WAREHOUSE_BY_ID = session.prepare("SELECT * FROM WarehouseContent WHERE package_id = ?;");
            UPSERT_PACKAGE_IN_WAREHOUSE = session.prepare("INSERT INTO WarehouseContent (package_id, courier_id, district_dest, size, client_id) VALUES (?, ?, ?, ?, ?);");
            UPDATE_COURIER_ID_PACKAGE_IN_WAREHOUSE_BY_ID = session.prepare("UPDATE WarehouseContent USING TTL 15 SET courier_id = ? WHERE package_id = ?;");
            DELETE_PACKAGE_FROM_WAREHOUSE_BY_ID = session.prepare("DELETE FROM WarehouseContent WHERE package_id = ?;");
        } catch (Exception e) {
            throw new CassandraBackendException("Could not prepare statements. " + e.getMessage() + ".", e);
        }
    }

    private ArrayList<PackageModel> getPackagesUsingQuery(BoundStatement bs) throws CassandraBackendException {
        ArrayList<PackageModel> packages = new ArrayList<>();

        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        for (Row row: rs) {
            PackageModel p = new PackageModel();
            p.setPackageID(row.getString("package_id"));
            p.setCourierID(row.getString("courier_id"));
            p.setDistrictDest(row.getString("district_dest"));
            try {
                p.setSize(PackageSize.fromInt(row.getInt("size")));
            } catch (PackageSizeException e) {
                throw new CassandraBackendException(e.getMessage());
            }
            p.setClientID(row.getString("client_id"));
            packages.add(p);
        }
        return packages;
    }

    public ArrayList<PackageModel> getPackagesInWarehouse() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGES_IN_WAREHOUSE);
        return getPackagesUsingQuery(bs);
        
    }

    public PackageModel getPackageInWarehouseByID(String packageID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGE_IN_WAREHOUSE_BY_ID);
        bs.bind(packageID);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        Row row = rs.one();
        if (row != null) {
            PackageModel p = new PackageModel();
            p.setPackageID(row.getString("package_id"));
            p.setCourierID(row.getString("courier_id"));
            p.setDistrictDest(row.getString("district_dest"));
            try {
                p.setSize(PackageSize.fromInt(row.getInt("size")));
            } catch (PackageSizeException e) {
                throw new CassandraBackendException(e.getMessage());
            }
            p.setClientID(row.getString("client_id"));
            return p;
        }
        return null;
    }

    public void upsertPackageInWarehouse(PackageModel p) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_PACKAGE_IN_WAREHOUSE);
        try {
            bs.bind(
                p.getPackageID(),
                p.getCourierID(),
                p.getDistrictDest(),
                PackageSize.toInt(p.getSize()),
                p.getClientID()
            );
        } catch (PackageSizeException e) {
            throw new CassandraBackendException(e.getMessage());
        }

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query. " + e.getMessage() + ".", e);
        }
    }

    public void updateCourierIDPackageInWarehouseByID(String packageID, String courierID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPDATE_COURIER_ID_PACKAGE_IN_WAREHOUSE_BY_ID);
        bs.bind(courierID, packageID);

        try {
            ResultSet a = session.execute(bs);
            System.out.println(a);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query. " + e.getMessage() + ".", e);
        }
    }

    public void deletePackageFromWarehouseByID(String packageID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(DELETE_PACKAGE_FROM_WAREHOUSE_BY_ID);
        bs.bind(packageID);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query. " + e.getMessage() + ".", e);
        }
    }

}
