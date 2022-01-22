package pl.pjtom.cassandra;

import java.util.ArrayList;
import java.util.EnumMap;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster.Builder;

import pl.pjtom.model.PackageModel;
import pl.pjtom.model.PackageSize;
import pl.pjtom.model.PackageSizeException;
import pl.pjtom.model.PostBoxModel;

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

    private static PreparedStatement SELECT_PACKAGES_IN_TRUNK_BY_ID;
    private static PreparedStatement UPSERT_PACKAGE_IN_TRUNK;
    private static PreparedStatement DELETE_PACKAGES_IN_TRUNK_BY_ID;

    private static PreparedStatement SELECT_POSTBOXES_IN_DISTRICT;
    private static PreparedStatement UPSERT_POSTBOX;

    private static PreparedStatement SELECT_PACKAGES_IN_POSTBOX;
    private static PreparedStatement UPSERT_PACKAGE_IN_POSTBOX;
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

            SELECT_PACKAGES_IN_TRUNK_BY_ID = session.prepare("SELECT * FROM CourierTrunkPackages WHERE courier_id = ?;");
            UPSERT_PACKAGE_IN_TRUNK = session.prepare("INSERT INTO CourierTrunkPackages (courier_id, package_id, district_dest, size, client_id, container_size) VALUES (?, ?, ?, ?, ?, ?);");
            DELETE_PACKAGES_IN_TRUNK_BY_ID = session.prepare("DELETE FROM CourierTrunkPackages WHERE courier_id = ? AND package_id = ?;");

            SELECT_POSTBOXES_IN_DISTRICT = session.prepare("SELECT * FROM PostBox WHERE district = ?;");
            UPSERT_POSTBOX = session.prepare("INSERT INTO PostBox (postbox_id, district, capacity_S, capacity_M, capacity_L) VALUES (?, ?, ?, ?, ?);");

            SELECT_PACKAGES_IN_POSTBOX = session.prepare("SELECT * FROM PostBoxContent WHERE postbox_id = ?;");
            UPSERT_PACKAGE_IN_POSTBOX = session.prepare("INSERT INTO PostBoxContent (postbox_id, package_id, container_size, client_id) VALUES (?, ?, ?, ?);");
            DELETE_PACKAGE_FROM_POSTBOX = session.prepare("DELETE FROM PostBoxContent WHERE postbox_id = ? AND package_id = ?;");

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

    public EnumMap<PackageSize, ArrayList<PackageModel>> getPackagesInTrunkGroupByContainer(String courierID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGES_IN_TRUNK_BY_ID);
        bs.bind(courierID);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        Row row = rs.one();
        EnumMap<PackageSize, ArrayList<PackageModel>> packages = new EnumMap<>(PackageSize.class);
        packages.put(PackageSize.SMALL, new ArrayList<PackageModel>());
        packages.put(PackageSize.MEDIUM, new ArrayList<PackageModel>());
        packages.put(PackageSize.LARGE, new ArrayList<PackageModel>());
        if (row != null) {
            PackageModel p = new PackageModel();
            p.setCourierID(row.getString("courier_id"));
            p.setPackageID(row.getString("package_id"));
            p.setDistrictDest(row.getString("district_dest"));
            try {
                p.setSize(PackageSize.fromInt(row.getInt("size")));
            } catch (PackageSizeException e) {
                throw new CassandraBackendException(e.getMessage());
            }
            p.setClientID(row.getString("client_id"));
            try {
                packages.get(PackageSize.fromInt(row.getInt("container_size"))).add(p);
            } catch (PackageSizeException e) {
                throw new CassandraBackendException(e.getMessage());
            }
        }
        return packages;
    }

    public EnumMap<PackageSize, ArrayList<PackageModel>> getPackagesInTrunkGroupByPackageSize(String courierID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGES_IN_TRUNK_BY_ID);
        bs.bind(courierID);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        Row row = rs.one();
        EnumMap<PackageSize, ArrayList<PackageModel>> packages = new EnumMap<>(PackageSize.class);
        packages.put(PackageSize.SMALL, new ArrayList<PackageModel>());
        packages.put(PackageSize.MEDIUM, new ArrayList<PackageModel>());
        packages.put(PackageSize.LARGE, new ArrayList<PackageModel>());
        if (row != null) {
            PackageModel p = new PackageModel();
            p.setCourierID(row.getString("courier_id"));
            p.setPackageID(row.getString("package_id"));
            p.setDistrictDest(row.getString("district_dest"));
            p.setClientID(row.getString("client_id"));
            try {
                packages.get(PackageSize.fromInt(row.getInt("size"))).add(p);
            } catch (PackageSizeException e) {
                throw new CassandraBackendException(e.getMessage());
            }
        }
        return packages;
    }

    public void upsertPackageInTrunk(PackageModel p, PackageSize containerSize) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_PACKAGE_IN_TRUNK);
        try {
            bs.bind(
                p.getCourierID(),
                p.getPackageID(),
                p.getDistrictDest(),
                PackageSize.toInt(p.getSize()),
                p.getClientID(),
                PackageSize.toInt(containerSize)
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

    public void deletePackageFromTrunkByID(String courierID, String packageID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(DELETE_PACKAGES_IN_TRUNK_BY_ID);
        bs.bind(courierID, packageID);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query. " + e.getMessage() + ".", e);
        }
    }

    public ArrayList<PostBoxModel> getPostBoxesInDistrict(String district) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_POSTBOXES_IN_DISTRICT);
        bs.bind(district);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        ArrayList<PostBoxModel> postboxes = new ArrayList<PostBoxModel>();
        Row row = rs.one();
        if (row != null) {
            PostBoxModel postbox = new PostBoxModel();
            postbox.setPostboxID(row.getString("postbox_id "));
            postbox.setDistrict(row.getString("district"));
            postbox.setCapacity(PackageSize.SMALL, row.getInt("capacity_S"));
            postbox.setCapacity(PackageSize.MEDIUM, row.getInt("capacity_M"));
            postbox.setCapacity(PackageSize.LARGE, row.getInt("capacity_L"));
            postboxes.add(postbox);
        }
        return postboxes;
    }

    public void upsertPostbox(PostBoxModel postbox) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_POSTBOX);
        bs.bind(
            postbox.getPostboxID(),
            postbox.getDistrict(),
            postbox.getCapacity(PackageSize.SMALL),
            postbox.getCapacity(PackageSize.MEDIUM),
            postbox.getCapacity(PackageSize.LARGE)
        );

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query. " + e.getMessage() + ".", e);
        }
    }

    public EnumMap<PackageSize, ArrayList<PackageModel>> getPackagesInPostBox(String postBoxID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGES_IN_POSTBOX);
        bs.bind(postBoxID);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        Row row = rs.one();
        EnumMap<PackageSize, ArrayList<PackageModel>> packages = new EnumMap<>(PackageSize.class);
        packages.put(PackageSize.SMALL, new ArrayList<PackageModel>());
        packages.put(PackageSize.MEDIUM, new ArrayList<PackageModel>());
        packages.put(PackageSize.LARGE, new ArrayList<PackageModel>());
        if (row != null) {
            PackageModel p = new PackageModel();
            p.setPackageID(row.getString("package_id"));
            p.setPackageID(row.getString("client_id"));
            try {
                packages.get(PackageSize.fromInt(row.getInt("container_size"))).add(p);
            } catch (PackageSizeException e) {
                throw new CassandraBackendException(e);
            }
        }
        return packages;
    }

    public void upsertPackageInPostBox(PackageModel p, PackageSize containerSize) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_PACKAGE_IN_TRUNK);
        try {
            bs.bind(
                p.getCourierID(),
                p.getPackageID(),
                PackageSize.toInt(containerSize),
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

    public void deletePackageFromPostBox(String postBoxID, String packageID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(DELETE_PACKAGES_IN_TRUNK_BY_ID);
        bs.bind(postBoxID, packageID);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query. " + e.getMessage() + ".", e);
        }
    }

}
