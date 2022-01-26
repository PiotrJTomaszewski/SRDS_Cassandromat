package pl.pjtom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;

import org.beryx.textio.TerminalProperties;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.web.RunnerData;

import de.vandermeer.asciitable.AsciiTable;
import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.ClientModel;
import pl.pjtom.model.CourierModel;
import pl.pjtom.model.PostBoxModel;
import pl.pjtom.model.PackageModel;

public class Menu {
    private CassandraConnector cassClient;
    private TextIO textIO;
    private TextTerminal<?> terminal;
    private TerminalProperties<?> props;

    public Menu(CassandraConnector cassClient) {
        this.cassClient = cassClient;
        textIO = TextIoFactory.getTextIO();
        terminal = textIO.getTextTerminal();
        props = terminal.getProperties();
    }

    public void showMenu() {
        while (true) {
            try {
                terminal.println("\n======== Cassandromat ========");
                String option = textIO.newStringInputReader().withNumberedPossibleValues("Upsert courier",
                        "List couriers", "Upsert district", "List districts", "Upsert client", "List clients",
                        "Upsert postbox", "List postboxes", "List packages in postbox", "Upsert package in warehouse", "List packages in warehouse", "Generate random packages in warehouse", "Courier go on route", "Client go for packages")
                        .read("Choose option");
                switch (option) {
                    case "Upsert courier":
                        upsertCourier();
                        break;
                    case "List couriers":
                        listCouriers();
                        break;
                    case "Upsert district":
                        upsertDistrict();
                        break;
                    case "List districts":
                        listDistricts();
                        break;
                    case "Upsert client":
                        upsertClient();
                        break;
                    case "List clients":
                        listClients();
                        break;
                    case "Upsert postbox":
                        upsertPostBox();
                        break;
                    case "List postboxes":
                        listPostBoxes();
                        break;
                    case "List packages in postbox":
                        listPackagesInPostBox();
                        break;
                    case "Upsert package in warehouse":
                        upsertPackageInWarehouse();
                        break;
                    case "List packages in warehouse":
                        listPackagesInWarehouse();
                        break;
                    case "Generate random packages in warehouse":
                        generateRandomPackagesInWarehouse();
                        break;
                    case "Courier go on route":
                        courierGoOnRoute();
                        break;
                    case "Client go for packages":
                        clientGoForPakcages();
                        break;
                }
            } catch (CassandraBackendException e) {
                props.setPromptColor("red");
                terminal.println(e.getMessage());
                props.setPromptColor("default");
            }
        }
    }

    private void upsertCourier() throws CassandraBackendException {
        CourierModel courier = new CourierModel();
        String courierID = textIO.newStringInputReader().withMinLength(0)
                .read("Courier ID (leave empty to generate one)");
        if (courierID.length() > 0) {
            courier.setCourierID(courierID);
        } else {
            courier.generateCourierID();
        }
        courier.setCapacity(textIO.newIntInputReader().withMinVal(1).read("Capacity"));
        cassClient.upsertCourier(courier);
        displayCouriers(Arrays.asList(new CourierModel[] { courier }));
    }

    private void listCouriers() throws CassandraBackendException {
        ArrayList<CourierModel> couriers = cassClient.getCouriers();
        displayCouriers(couriers);
    }

    private void upsertDistrict() throws CassandraBackendException {
        String district = textIO.newStringInputReader().read("District");
        cassClient.upsertDistrict(district);
        displayDistricts(Arrays.asList(new String[] { district }));
    }

    private void listDistricts() throws CassandraBackendException {
        ArrayList<String> districts = cassClient.getDistricts();
        displayDistricts(districts);
    }

    private void upsertClient() throws CassandraBackendException {
        ClientModel client = new ClientModel();
        String clientID = textIO.newStringInputReader().withMinLength(0)
                .read("Client ID (leave empty to generate one)");
        if (clientID.length() > 0) {
            client.setClientID(clientID);
        } else {
            client.generateClientID();
        }
        ArrayList<String> districts = cassClient.getDistricts();
        client.setDistrict(textIO.newStringInputReader().withNumberedPossibleValues(districts).read("District"));
        cassClient.upsertClient(client);
        displayClients(Arrays.asList(new ClientModel[] { client }));
    }

    private void listClients() throws CassandraBackendException {
        ArrayList<ClientModel> clients = cassClient.getClients();
        displayClients(clients);
    }

    private void upsertPostBox() throws CassandraBackendException {
        PostBoxModel postBox = new PostBoxModel();
        String postBoxID = textIO.newStringInputReader().withMinLength(0)
                .read("Postbox ID (leave empty to generate one)");
        if (postBoxID.length() > 0) {
            postBox.setPostboxID(postBoxID);
        } else {
            postBox.generatePostboxID();
        }
        ArrayList<String> districts = cassClient.getDistricts();
        postBox.setDistrict(textIO.newStringInputReader().withNumberedPossibleValues(districts).read("District"));
        cassClient.upsertPostbox(postBox);
        displayPostBoxes(Arrays.asList(new PostBoxModel[] { postBox }));
    }

    private void listPostBoxes() throws CassandraBackendException {
        ArrayList<String> districts = cassClient.getDistricts();
        String district = textIO.newStringInputReader().withNumberedPossibleValues(districts).read("District");
        ArrayList<PostBoxModel> postBoxes = cassClient.getPostBoxesInDistrict(district);
        displayPostBoxes(postBoxes);
    }

    private void listPackagesInPostBox() throws CassandraBackendException {
        ArrayList<String> districts = cassClient.getDistricts();
        String district = textIO.newStringInputReader().withNumberedPossibleValues(districts).read("District");

        ArrayList<PostBoxModel> postBoxesInDistrict = cassClient.getPostBoxesInDistrict(district);

        ArrayList<String> postBoxesInDistrictIDs = new ArrayList<>();
        for (PostBoxModel postBox: postBoxesInDistrict) {
            postBoxesInDistrictIDs.add(postBox.getPostBoxID());
        }

        String postBoxID = textIO.newStringInputReader().withNumberedPossibleValues(postBoxesInDistrictIDs).read("Postbox ID");

        displayPackages(cassClient.getPackagesInPostBox(postBoxID));
    }

    private void upsertPackageInWarehouse() throws CassandraBackendException {
        PackageModel packageModel = new PackageModel();
        String packageID = textIO.newStringInputReader().withMinLength(0)
                .read("Package ID (leave empty to generate one)");
        if (packageID.length() > 0) {
            packageModel.setPackageID(packageID);
        } else {
            packageModel.generatePackageID();
        }

        ArrayList<ClientModel> clients = cassClient.getClients();
        ArrayList<String> clientsIDs = new ArrayList<>();
        for (ClientModel client : clients) {
            clientsIDs.add(client.getClientID());
        }
        String clientID = textIO.newStringInputReader().withNumberedPossibleValues(clientsIDs).read("Client ID (recipient)");
        packageModel.setClientID(clientID);
        for (ClientModel client : clients) {
            if (client.getClientID().equals(clientID)) {
                packageModel.setDistrictDest(client.getDistrict());
                break;
            }
        }
        cassClient.upsertPackageInWarehouse(packageModel);
        displayPackages(Arrays.asList(new PackageModel[] { packageModel }));
    }

    private void listPackagesInWarehouse() throws CassandraBackendException {
        ArrayList<String> districts = cassClient.getDistricts();
        String district = textIO.newStringInputReader().withNumberedPossibleValues(districts).read("Dest. district");
        ArrayList<PackageModel> packages = cassClient.getPackagesInWarehouseByDistrict(district);
        displayPackages(packages);
    }

    private void generateRandomPackagesInWarehouse() throws CassandraBackendException {
        DataCreator.createSomePackages(cassClient);
    }

    private void courierGoOnRoute() throws CassandraBackendException {
        ArrayList<CourierModel> couriers = cassClient.getCouriers();
        ArrayList<String> couriersIDs = new ArrayList<>();
        for (CourierModel courier: couriers) {
            couriersIDs.add(courier.getCourierID());
        }
        String courierID = textIO.newStringInputReader().withNumberedPossibleValues(couriersIDs).read("Courier ID");
        Courier courier;
        for (CourierModel courierModel: couriers) {
            if (courierModel.getCourierID().equals(courierID)) {
                courier = new Courier(cassClient, courierModel);
                courier.loadTheTrunk();
                courier.deliverPackages();
                break;
            }
        }
    }

    private void clientGoForPakcages() throws CassandraBackendException {
        ArrayList<ClientModel> clients = cassClient.getClients();
        ArrayList<String> clientsIDs = new ArrayList<>();
        for (ClientModel client: clients) {
            clientsIDs.add(client.getClientID());
        }
        String clientID = textIO.newStringInputReader().withNumberedPossibleValues(clientsIDs).read("Client ID");
        Client client;
        for (ClientModel clientModel: clients) {
            if (clientModel.getClientID().equals(clientID)) {
                client = new Client(cassClient, clientModel);
                client.pickupPackages();
                break;
            }
        }
    }

    private void displayCouriers(Iterable<CourierModel> couriers) {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("CourierID", "Capacity");
        for (CourierModel courier : couriers) {
            at.addRule();
            at.addRow(courier.getCourierID(), courier.getCapacity());
        }
        at.addRule();
        terminal.print(at.render());
    }

    private void displayDistricts(Iterable<String> districts) {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("District");
        for (String district : districts) {
            at.addRule();
            at.addRow(district);
        }
        at.addRule();
        terminal.print(at.render());
    }

    private void displayClients(Iterable<ClientModel> clients) {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Client ID", "District");
        for (ClientModel client : clients) {
            at.addRule();
            at.addRow(client.getClientID(), client.getDistrict());
        }
        at.addRule();
        terminal.print(at.render());
    }

    private void displayPostBoxes(Iterable<PostBoxModel> postBoxes) {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Postbox ID", "District", "Capacity");
        for (PostBoxModel postBox : postBoxes) {
            at.addRule();
            at.addRow(postBox.getPostBoxID(), postBox.getDistrict(), postBox.getDistrict());
        }
        at.addRule();
        terminal.print(at.render());
    }

    private void displayPackages(Iterable<PackageModel> packages) {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Package ID", "Dest. district", "Client ID", "Courier ID", "Is ready to pickup", "Postbox ID");
        for (PackageModel p : packages) {
            at.addRule();
            at.addRow(
                p.getPackageID(),
                p.getDistrictDest(),
                p.getClientID(),
                p.getCourierID() != null ? p.getCourierID() : "",
                p.getIsReadyToPickup(),
                p.getPostBoxID() != null ? p.getPostBoxID() : ""
            );
        }
        at.addRule();
        terminal.print(at.render());
    }
}
