package pl.pjtom.cassandra;

import java.util.ArrayList;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster.Builder;

import pl.pjtom.model.ClientModel;
import pl.pjtom.model.CourierModel;
import pl.pjtom.model.PackageLogEntryModel;
import pl.pjtom.model.PackageLogEvent;
import pl.pjtom.model.PackageModel;
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

    private static PreparedStatement SELECT_PACKAGES_IN_WAREHOUSE_BY_DISTRICT;
    private static PreparedStatement SELECT_PACKAGE_IN_WAREHOUSE_BY_ID;
    private static PreparedStatement UPSERT_PACKAGE_IN_WAREHOUSE;
    private static PreparedStatement UPDATE_COURIER_ID_PACKAGE_IN_WAREHOUSE_BY_ID;
    private static PreparedStatement DELETE_PACKAGE_FROM_WAREHOUSE_BY_ID;

    private static PreparedStatement SELECT_POSTBOXES;
    private static PreparedStatement SELECT_POSTBOXES_IN_DISTRICT;
    private static PreparedStatement UPSERT_POSTBOX;

    private static PreparedStatement SELECT_PACKAGES_IN_POSTBOX;
    private static PreparedStatement SELECT_PACKAGES_IN_POSTBOXES_BY_CLIENT_ID;
    private static PreparedStatement UPSERT_PACKAGE_IN_POSTBOX;
    private static PreparedStatement DELETE_PACKAGE_FROM_POSTBOX;

    private static PreparedStatement SELECT_DISTRICTS;
    private static PreparedStatement UPSERT_DISTRICT;

    private static PreparedStatement SELECT_CLIENTS;
    private static PreparedStatement UPSERT_CLIENT;

    private static PreparedStatement SELECT_COURIERS;
    private static PreparedStatement UPSERT_COURIER;

    private static PreparedStatement SELECT_PACKAGE_LOG;
    private static PreparedStatement UPSERT_PACKAGE_INTO_LOG;

    private static PreparedStatement TRUNCATE_POSTBOX;
    private static PreparedStatement TRUNCATE_POSTBOX_CONTENT;
    private static PreparedStatement TRUNCATE_COURIER;
    private static PreparedStatement TRUNCATE_PACKAGE_LOG;
    private static PreparedStatement TRUNCATE_WAREHOUSE_CONTENT;
    private static PreparedStatement TRUNCATE_CLIENT;
    private static PreparedStatement TRUNCATE_DISTRICT;

    private void prepareStatements() throws CassandraBackendException {
        try {
            SELECT_PACKAGES_IN_WAREHOUSE_BY_DISTRICT = session.prepare("SELECT * FROM WarehouseContent WHERE district_dest = ?;");
            SELECT_PACKAGE_IN_WAREHOUSE_BY_ID = session.prepare("SELECT * FROM WarehouseContent WHERE district_dest = ? AND package_id = ?;");
            UPSERT_PACKAGE_IN_WAREHOUSE = session.prepare("INSERT INTO WarehouseContent (package_id, courier_id, district_dest, client_id) VALUES (?, ?, ?, ?);");
            UPDATE_COURIER_ID_PACKAGE_IN_WAREHOUSE_BY_ID = session.prepare("UPDATE WarehouseContent USING TTL 5 SET courier_id = ? WHERE district_dest = ? AND package_id = ?;");
            DELETE_PACKAGE_FROM_WAREHOUSE_BY_ID = session.prepare("DELETE FROM WarehouseContent WHERE district_dest = ? AND package_id = ?;");

            SELECT_POSTBOXES = session.prepare("SELECT * FROM PostBox;");
            SELECT_POSTBOXES_IN_DISTRICT = session.prepare("SELECT * FROM PostBox WHERE district = ?;");
            UPSERT_POSTBOX = session.prepare("INSERT INTO PostBox (postbox_id, district, capacity) VALUES (?, ?, ?);");

            SELECT_PACKAGES_IN_POSTBOX = session.prepare("SELECT * FROM PostBoxContent WHERE postbox_id = ?;");
            SELECT_PACKAGES_IN_POSTBOXES_BY_CLIENT_ID = session.prepare("SELECT * FROM PostBoxContent WHERE client_id = ?;");
            UPSERT_PACKAGE_IN_POSTBOX = session.prepare("INSERT INTO PostBoxContent (postbox_id, package_id, client_id, is_ready_to_pickup) VALUES (?, ?, ?, ?);");
            DELETE_PACKAGE_FROM_POSTBOX = session.prepare("DELETE FROM PostBoxContent WHERE postbox_id = ? AND package_id = ?;");

            SELECT_DISTRICTS = session.prepare("SELECT * FROM District;");
            UPSERT_DISTRICT = session.prepare("INSERT INTO District (district) VALUES (?);");

            SELECT_CLIENTS = session.prepare("SELECT * FROM Client;");
            UPSERT_CLIENT = session.prepare("INSERT INTO Client (client_id, district) VALUES (?, ?);");

            SELECT_COURIERS = session.prepare("SELECT * FROM Courier;");
            UPSERT_COURIER = session.prepare("INSERT INTO Courier (courier_id, capacity) VALUES (?, ?);");

            SELECT_PACKAGE_LOG = session.prepare("SELECT * FROM PackageLog;");
            UPSERT_PACKAGE_INTO_LOG = session.prepare("INSERT INTO PackageLog (package_id, action_type, action_time, actor_id, postbox_id) VALUES (?, ?, ?, ?, ?);");

            TRUNCATE_POSTBOX = session.prepare("TRUNCATE PostBox;");
            TRUNCATE_POSTBOX_CONTENT = session.prepare("TRUNCATE PostBoxContent;");
            TRUNCATE_COURIER = session.prepare("TRUNCATE Courier;");
            TRUNCATE_PACKAGE_LOG = session.prepare("TRUNCATE PackageLog;");
            TRUNCATE_WAREHOUSE_CONTENT = session.prepare("TRUNCATE WarehouseContent;");
            TRUNCATE_CLIENT = session.prepare("TRUNCATE Client;");
            TRUNCATE_DISTRICT = session.prepare("TRUNCATE District;");

        } catch (Exception e) {
            throw new CassandraBackendException("Could not prepare statements. " + e.getMessage() + ".", e);
        }
    }

    public ArrayList<PackageModel> getPackagesInWarehouseByDistrict(String district) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGES_IN_WAREHOUSE_BY_DISTRICT);
        bs.bind(district);

        ArrayList<PackageModel> packages = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query getPackagesInWarehouse. " + e.getMessage() + ".", e);
        }
        for (Row row: rs) {
            PackageModel p = new PackageModel();
            p.setPackageID(row.getString("package_id"));
            p.setCourierID(row.getString("courier_id"));
            p.setDistrictDest(row.getString("district_dest"));
            p.setClientID(row.getString("client_id"));
            packages.add(p);
        }
        return packages;
    }

    public PackageModel getPackageInWarehouseByID(String district, String packageID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGE_IN_WAREHOUSE_BY_ID);
        bs.bind(district, packageID);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query getPackageInWarehouseByID. " + e.getMessage() + ".", e);
        }
        Row row = rs.one();
        if (row != null) {
            PackageModel p = new PackageModel();
            p.setPackageID(row.getString("package_id"));
            p.setCourierID(row.getString("courier_id"));
            p.setDistrictDest(row.getString("district_dest"));
            p.setClientID(row.getString("client_id"));
            return p;
        }
        return null;
    }

    public void upsertPackageInWarehouse(PackageModel p) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_PACKAGE_IN_WAREHOUSE);
        bs.bind(
            p.getPackageID(),
            p.getCourierID(),
            p.getDistrictDest(),
            p.getClientID()
        );

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query upsertPackageInWarehouse. " + e.getMessage() + ".", e);
        }
    }

    public void updateCourierIDPackageInWarehouseByID(String destDistrict, String packageID, String courierID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPDATE_COURIER_ID_PACKAGE_IN_WAREHOUSE_BY_ID);
        bs.bind(courierID, destDistrict, packageID);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query updateCourierIDPackageInWarehouseByID. " + e.getMessage() + ".", e);
        }
    }

    public void deletePackageFromWarehouseByID(String district, String packageID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(DELETE_PACKAGE_FROM_WAREHOUSE_BY_ID);
        bs.bind(district, packageID);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query deletePackageFromWarehouseByID. " + e.getMessage() + ".", e);
        }
    }

    public PostBoxModel getPostBox(String postBoxID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_POSTBOXES);
        bs.bind(postBoxID);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query getPostBox. " + e.getMessage() + ".", e);
        }

        Row row = rs.one();
        if (row != null) {
            PostBoxModel postbox = new PostBoxModel();
            postbox.setPostboxID(row.getString("postbox_id "));
            postbox.setDistrict(row.getString("district"));
            postbox.setCapacity(row.getInt("capacity"));
            return postbox;
        }
        return null;
    }

    public ArrayList<PostBoxModel> getPostBoxesInDistrict(String district) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_POSTBOXES_IN_DISTRICT);
        bs.bind(district);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query getPostBoxesInDistrict. " + e.getMessage() + ".", e);
        }

        ArrayList<PostBoxModel> postboxes = new ArrayList<PostBoxModel>();
        for (Row row: rs) {
            PostBoxModel postbox = new PostBoxModel();
            postbox.setPostboxID(row.getString("postbox_id"));
            postbox.setDistrict(row.getString("district"));
            postbox.setCapacity(row.getInt("capacity"));
            postboxes.add(postbox);
        }
        return postboxes;
    }

    public void upsertPostbox(PostBoxModel postbox) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_POSTBOX);
        bs.bind(
            postbox.getPostBoxID(),
            postbox.getDistrict(),
            postbox.getCapacity()
        );

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query upsertPostbox. " + e.getMessage() + ".", e);
        }
    }

    public ArrayList<PackageModel> getPackagesInPostBox(String postBoxID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGES_IN_POSTBOX);
        bs.bind(postBoxID);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query getPackagesInPostBox. " + e.getMessage() + ".", e);
        }
        ArrayList<PackageModel> packages = new ArrayList<>();
        for (Row row: rs) {
            PackageModel p = new PackageModel();
            p.setPackageID(row.getString("package_id"));
            p.setClientID(row.getString("client_id"));
            p.setIsReadyToPickup(row.getBool("is_ready_to_pickup"));
            packages.add(p);
        }
        return packages;
    }

    public ArrayList<PackageModel> getPackagesInPostBoxesByClientID(String clientID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGES_IN_POSTBOXES_BY_CLIENT_ID);
        bs.bind(clientID);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query getPackagesInPostBoxesByClientID. " + e.getMessage() + ".", e);
        }
        ArrayList<PackageModel> packages = new ArrayList<>();
        for (Row row: rs) {
            PackageModel p = new PackageModel();
            p.setPackageID(row.getString("package_id"));
            p.setClientID(row.getString("client_id"));
            p.setIsReadyToPickup(row.getBool("is_ready_to_pickup"));
            p.setPostBoxID(row.getString("postbox_id"));
            packages.add(p);
        }
        return packages;
    }

    public int countPackagesInPostBox(String postBoxID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGES_IN_POSTBOX);
        bs.bind(postBoxID);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query countPackagesInPostBox. " + e.getMessage() + ".", e);
        }
        return rs.all().size();
    }

    public void upsertPackageInPostBox(String postBoxID, PackageModel p) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_PACKAGE_IN_POSTBOX);
        bs.bind(
            postBoxID,
            p.getPackageID(),
            p.getClientID(),
            p.getIsReadyToPickup()
        );

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query upsertPackageInPostBox. " + e.getMessage() + ".", e);
        }
    }

    public void deletePackageFromPostBox(String postBoxID, String packageID) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(DELETE_PACKAGE_FROM_POSTBOX);
        bs.bind(postBoxID, packageID);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query deletePackageFromPostBox. " + e.getMessage() + ".", e);
        }
    }

    public ArrayList<String> getDistricts() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_DISTRICTS);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query getDistricts. " + e.getMessage() + ".", e);
        }
        ArrayList<String> districts = new ArrayList<String>();
        for (Row row: rs) {
            districts.add(row.getString("district"));
        }
        return districts;
    }

    public void upsertDistrict(String district) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_DISTRICT);
        bs.bind(district);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query upsertDistrict. " + e.getMessage() + ".", e);
        }
    }

    public ArrayList<CourierModel> getCouriers() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_COURIERS);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query getCouriers. " + e.getMessage() + ".", e);
        }
        ArrayList<CourierModel> couriers = new ArrayList<>();
        for (Row row: rs) {
            CourierModel courier = new CourierModel();
            courier.setCourierID(row.getString("courier_id"));
            courier.setCapacity(row.getInt("capacity"));
            couriers.add(courier);
        }
        return couriers;
    }

    public void upsertCourier(CourierModel courier) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_COURIER);
        bs.bind(courier.getCourierID(), courier.getCapacity());

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query upsertCourier. " + e.getMessage() + ".", e);
        }
    }

    public ArrayList<ClientModel> getClients() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_CLIENTS);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query getClients. " + e.getMessage() + ".", e);
        }
        ArrayList<ClientModel> clients = new ArrayList<>();
        for (Row row: rs) {
            ClientModel client = new ClientModel();
            client.setClientID(row.getString("client_id"));
            client.setDistrict(row.getString("district"));
            clients.add(client);
        }
        return clients;
    }

    public void upsertClient(ClientModel client) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_CLIENT);
        bs.bind(client.getClientID(), client.getDistrict());

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query upsertClient. " + e.getMessage() + ".", e);
        }
    }

    public ArrayList<PackageLogEntryModel> getPackageLog() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(SELECT_PACKAGE_LOG);
        ResultSet rs = null;
        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform a query getPackageLog. " + e.getMessage() + ".", e);
        }
        ArrayList<PackageLogEntryModel> packageLog = new ArrayList<>();
        for (Row row: rs) {
            PackageLogEntryModel packageLogEntry = new PackageLogEntryModel();
            packageLogEntry.setPackageID(row.getString("package_id"));
            packageLogEntry.setActionType(PackageLogEvent.fromInt(row.getInt("action_type")));
            packageLogEntry.setActionTime(row.getTimestamp("action_time"));
            packageLogEntry.setActorID(row.getString("actor_id"));
            packageLogEntry.setPostBoxID(row.getString("postbox_id"));
            packageLog.add(packageLogEntry);
        }
        return packageLog;
    }

    public void upsertPackageLog(PackageLogEntryModel packageLogEntry) throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(UPSERT_PACKAGE_INTO_LOG);
        bs.bind(
            packageLogEntry.getPackageID(),
            packageLogEntry.getActionType().getValue(),
            packageLogEntry.getActionTime(),
            packageLogEntry.getActorID(),
            packageLogEntry.getPostBoxID()
        );

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query upsertPackageLog. " + e.getMessage() + ".", e);
        }
    }

    public void truncatePostBox() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(TRUNCATE_POSTBOX);
        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query truncatePostBox. " + e.getMessage() + ".", e);
        }
    }

    public void truncatePostBoxContent() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(TRUNCATE_POSTBOX_CONTENT);
        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query truncatePostBoxContent. " + e.getMessage() + ".", e);
        }
    }

    public void truncateCourier() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(TRUNCATE_COURIER);
        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query truncateCourier. " + e.getMessage() + ".", e);
        }
    }

    public void truncatePackageLog() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(TRUNCATE_PACKAGE_LOG);
        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query truncatePackageLog. " + e.getMessage() + ".", e);
        }
    }

    public void truncateWarehouseContent() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(TRUNCATE_WAREHOUSE_CONTENT);
        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query truncateWarehouseContent. " + e.getMessage() + ".", e);
        }
    }

    public void truncateClient() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(TRUNCATE_CLIENT);
        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query truncateClient. " + e.getMessage() + ".", e);
        }
    }

    public void truncateDistrict() throws CassandraBackendException {
        BoundStatement bs = new BoundStatement(TRUNCATE_DISTRICT);
        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new CassandraBackendException("Could not perform user query truncateDistrict. " + e.getMessage() + ".", e);
        }
    }

}
