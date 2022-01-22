package pl.pjtom;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.PostBoxModel;
import pl.pjtom.model.PackageModel;

public class Courier {
    private CassandraConnector cassClient;
    private String courierID;
    private int capacity;
    private ArrayList<PackageModel> claimedPackages = new ArrayList<>();
    private Random rand = new Random();

    private void init() throws CassandraBackendException {
        // Trunk content survives system restarts
        claimedPackages = cassClient.getPackagesInTrunk(getCourierID());
    }

    public Courier(CassandraConnector cassClient, boolean generateID) throws CassandraBackendException {
        this.cassClient = cassClient;
        if (generateID) {
            generateCourierID();
        }
        // TODO: Read from database
        capacity = 10;
        init();
    }

    public Courier(CassandraConnector cassClient, String courierID, int capacity) throws CassandraBackendException {
        this.cassClient = cassClient;
        this.courierID = courierID;
        this.capacity = capacity;
        init();
    }

    public void generateCourierID() {
        courierID = UUID.randomUUID().toString();
    }

    public String getCourierID() {
        return courierID;
    }

    public void setCourierID(String courierID) {
        this.courierID = courierID;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void loadTheTrunk() throws CassandraBackendException {
        boolean stayAtWarehouse = true;
        int packagesInTrunkCount = 0;
        while (stayAtWarehouse) {
            claimAndLoadPackages(packagesInTrunkCount);
            if (claimedPackages.size() + packagesInTrunkCount == capacity) {
                System.out.println("Leaving the warehouse with a full trunk");
                stayAtWarehouse = false;
            } else {
                // Move successfully claimed packages to the trunk
                for (PackageModel p: claimedPackages) {
                    cassClient.upsertPackageInTrunk(p);
                    packagesInTrunkCount += 1;
                }
                System.out.print("I have " + packagesInTrunkCount + "/" + capacity + " packages.");
                if (rand.nextInt(100) < 30) {
                    System.out.println(" I'm leaving anyway");
                    stayAtWarehouse = false;
                } else {
                    System.out.println(" I'll stay for a bit longer");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
                claimedPackages.clear();
            }
        }
    }

    private void claimAndLoadPackages(int packagesInTrunkCount) throws CassandraBackendException {
        // Get the list of free packages in warehouse
        ArrayList<PackageModel> packages = cassClient.getPackagesInWarehouse();
        // TODO: Couriers prefer different district on each trip

        // Claiming packages the courier wants to pick up
        for (PackageModel p: packages) {
            if (p.getCourierID() == null) {
                if (packagesInTrunkCount + claimedPackages.size() < capacity) {
                    cassClient.updateCourierIDPackageInWarehouseByID(p.getPackageID(), getCourierID());
                    claimedPackages.add(p);
                } else {
                    break;
                }
            }
        }

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.getMessage();
        }

        for (PackageModel p: claimedPackages) {
            PackageModel checkPackage = cassClient.getPackageInWarehouseByID(p.getPackageID());
            if (checkPackage != null && checkPackage.getCourierID().equals(getCourierID())) {
                p.setCourierID(getCourierID());
                System.out.println("I'm taking package " + p.getPackageID() + ".");
                cassClient.upsertPackageInTrunk(p);
                cassClient.deletePackageFromWarehouseByID(p.getPackageID());
            } else {
                System.out.println("Someone else took the package " + p.getPackageID() + ".");
            }
        }
    }

    public void placePackagesInPostBox(String postboxID) throws CassandraBackendException {
        ArrayList<PackageModel> packagesInTrunk = cassClient.getPackagesInTrunk(getCourierID());
    }

}
