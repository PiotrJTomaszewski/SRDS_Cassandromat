package pl.pjtom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.PackageLogEntryModel;
import pl.pjtom.model.PackageLogEvent;
import pl.pjtom.model.CourierModel;
import pl.pjtom.model.PostBoxModel;

public class LogChecker {
    private CassandraConnector cassClient;
    private ArrayList<PackageLogEntryModel> packageLog;
    private HashMap<String, ArrayList<PackageLogEntryModel>> entriesByActor = new HashMap<>();
    private HashMap<String, ArrayList<PackageLogEntryModel>> entriesByPackage = new HashMap<>();
    private HashMap<String, ArrayList<PackageLogEntryModel>> entriesByPostBox = new HashMap<>();
    private ArrayList<CourierModel> couriers;
    private ArrayList<PostBoxModel> postBoxes = new ArrayList<PostBoxModel>();

    public LogChecker(CassandraConnector cassClient) {
        this.cassClient = cassClient;
    }

    public void checkLogs() throws CassandraBackendException {
        couriers = cassClient.getCouriers();
        ArrayList<String> districts = cassClient.getDistricts();
        for (String district: districts) {
            postBoxes.addAll(cassClient.getPostBoxesInDistrict(district));
        }
        packageLog = cassClient.getPackageLog();
        // Sort logs
        Collections.sort(packageLog);

        for (PackageLogEntryModel logEntry: packageLog) {
            // Group by actor (courier or client)
            if (!entriesByActor.containsKey(logEntry.getActorID())) {
                entriesByActor.put(logEntry.getActorID(), new ArrayList<PackageLogEntryModel>());
            }
            entriesByActor.get(logEntry.getActorID()).add(logEntry);

            // Group by package
            if (!entriesByPackage.containsKey(logEntry.getPackageID())) {
                entriesByPackage.put(logEntry.getPackageID(), new ArrayList<PackageLogEntryModel>());
            }
            entriesByPackage.get(logEntry.getPackageID()).add(logEntry);

            // Group by post box
            if (!entriesByPostBox.containsKey(logEntry.getPostBoxID())) {
                entriesByPostBox.put(logEntry.getPostBoxID(), new ArrayList<PackageLogEntryModel>());
            }
            entriesByPostBox.get(logEntry.getPostBoxID()).add(logEntry);
        }

        checkCouriersCapacity();
        checkPackageDeliveryHistory();
        checkPostBoxCapacityHistory();
    }

    private boolean checkCouriersCapacity() {
        boolean everythingOK = true;
        int entriesChecked = 0;
        for (CourierModel courier: couriers) {
            int capacityLeft = courier.getCapacity();
            ArrayList<PackageLogEntryModel> packages = entriesByActor.get(courier.getCourierID());
            if (packages != null) {
                for (PackageLogEntryModel entry: packages) {
                    switch (entry.getActionType()) {
                        case TAKE_PACKAGE_FROM_WAREHOUSE:
                            capacityLeft -= 1;
                            if (capacityLeft < 0) {
                                everythingOK = false;
                                System.out.println("Courier " + courier.getCourierID() + " picked up too many packages.");
                            }
                            break;
                        case PUT_PACKAGE_IN_POSTBOX:
                            capacityLeft += 1;
                            if (capacityLeft > courier.getCapacity()) {
                                everythingOK = false;
                                System.out.println("Courier " + courier.getCourierID() + " taken out more pacakes than he put in.");
                            }
                            break;
                        default: break;
                    }
                    entriesChecked += 1;
                }
            }
        }
        System.out.println("Couriers capacity history " + (everythingOK ? "OK" : "WRONG") + ", " + entriesChecked + " entries checked.");
        return everythingOK;
    }

    private boolean checkPackageDeliveryHistory() {
        boolean everythingOK = true;
        int entriesChecked = 0;
        for (Map.Entry<String, ArrayList<PackageLogEntryModel>> mapEntry: entriesByPackage.entrySet()) {
            int pickedFromWarehouseCount = 0;
            int putInPostBoxCount = 0;
            int pickedUpFromPostBoxCount = 0;
            for (PackageLogEntryModel logEntry: mapEntry.getValue()) {
                switch (logEntry.getActionType()) {
                    case TAKE_PACKAGE_FROM_WAREHOUSE:
                        pickedFromWarehouseCount += 1;
                        break;
                    case PUT_PACKAGE_IN_POSTBOX:
                        putInPostBoxCount += 1;
                        break;
                    case PICKUP_PACKAGE_FROM_POSTBOX:
                        pickedUpFromPostBoxCount += 1;
                        break;
                }
            }
            if (pickedFromWarehouseCount > 1) {
                everythingOK = false;
                System.out.println("Package " + mapEntry.getKey() + " was taken from warehouse more than once (" + pickedFromWarehouseCount + " times, to be precise).");
            }
            if (putInPostBoxCount > 1) {
                everythingOK = false;
                System.out.println("Package " + mapEntry.getKey() + " was put in post box more than once (" + putInPostBoxCount + " times, to be precise).");
            }
            if (pickedUpFromPostBoxCount > 1) {
                everythingOK = false;
                System.out.println("Package " + mapEntry.getKey() + " was taken out of the post box more than once (" + pickedUpFromPostBoxCount + " times, to be precise).");
            }
            entriesChecked += 1;
        }
        System.out.println("Package delivery history " + (everythingOK ? "OK" : "WRONG") + ", " + entriesChecked + " entries checked.");
        return everythingOK;
    }

    private boolean checkPostBoxCapacityHistory() {
        boolean everythingOK = true;
        int entriesChecked = 0;
        for (PostBoxModel postBox: postBoxes) {
            int capacityLeft = postBox.getCapacity();
            for (PackageLogEntryModel entry: entriesByPostBox.get(postBox.getPostBoxID())) {
                switch (entry.getActionType()) {
                    case PUT_PACKAGE_IN_POSTBOX:
                            capacityLeft -= 1;
                            if (capacityLeft < 0) {
                                everythingOK = false;
                                System.out.println("Post box " + postBox.getPostBoxID() + " has too many packages.");
                            }
                        break;
                    case PICKUP_PACKAGE_FROM_POSTBOX:
                        capacityLeft += 1;
                        if (capacityLeft > postBox.getCapacity()) {
                            everythingOK = false;
                            System.out.println("Post box " + postBox.getPostBoxID() + " has less than 0 pacakges.");
                        }
                        break;
                    default: break;
                }
                entriesChecked += 1;
            }
        }

        System.out.println("Post box capacity history " + (everythingOK ? "OK" : "WRONG") + ", " + entriesChecked + " entries checked.");
        return everythingOK;
    }
}
