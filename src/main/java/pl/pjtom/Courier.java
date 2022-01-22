package pl.pjtom;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.PackageSize;
import pl.pjtom.model.PackageModel;

public class Courier {
    private CassandraConnector cassClient;
    private String courierID;
    private EnumMap<PackageSize, Integer> capacity = new EnumMap<>(PackageSize.class);
    private EnumMap<PackageSize, ArrayList<PackageModel>> claimedPackages = new EnumMap<>(PackageSize.class);
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
        capacity.put(PackageSize.SMALL, 10);
        capacity.put(PackageSize.MEDIUM, 10);
        capacity.put(PackageSize.LARGE, 10);
        init();
    }

    public Courier(CassandraConnector cassClient, String courierID, EnumMap<PackageSize, Integer> capacity) throws CassandraBackendException {
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

    public Integer getCapacity(PackageSize size) {
        return capacity.get(size);
    }

    public void setCapacity(PackageSize size, Integer count) {
        this.capacity.put(size, count);
    }

    public void loadTheTrunk() throws CassandraBackendException {
        boolean stayAtWarehouse = true;
        while (stayAtWarehouse) {
            claimAndLoadPackages();
            if (getFreeSpace(PackageSize.SMALL) == 0 && getFreeSpace(PackageSize.MEDIUM) == 0 && getFreeSpace(PackageSize.LARGE) == 0) {
                System.out.println("Leaving the warehouse with a full trunk");
                stayAtWarehouse = false;
            } else {
                // Move claimed packages to the trunk
                for (Entry<PackageSize, ArrayList<PackageModel>> entry: claimedPackages.entrySet()) {
                    for (PackageModel p: entry.getValue()) {
                        cassClient.upsertPackageInTrunk(p, entry.getKey());
                    }
                }
                System.out.print("I have " + claimedPackages.get(PackageSize.SMALL).size() + "/" + capacity.get(PackageSize.SMALL) + " small, " + claimedPackages.get(PackageSize.MEDIUM).size() + "/" + capacity.get(PackageSize.MEDIUM) + ", medium and " + claimedPackages.get(PackageSize.LARGE).size() + "/" + capacity.get(PackageSize.LARGE) + " large packages.");
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
            }
        }
        for (PackageSize containerSize: PackageSize.values()) {
            claimedPackages.get(containerSize).clear();
        }
    }

    private void claimAndLoadPackages() throws CassandraBackendException {
        // Get the list of free packages in warehouse sorted by size
        ArrayList<PackageModel> packages = cassClient.getPackagesInWarehouse();
        // TODO: Couriers prefer different district on each trip

        // Sort the free packages by size
        EnumMap<PackageSize, ArrayList<PackageModel>> freePackagesBySize = new EnumMap<>(PackageSize.class);
        for (PackageSize size: PackageSize.values()) {
            freePackagesBySize.put(size, new ArrayList<PackageModel>());
        }
        for (PackageModel p: packages) {
            if (p.getCourierID() == null) {
                freePackagesBySize.get(p.getSize()).add(p);
            }
        }

        // Claiming packages the courier wants to pick up
        for (PackageModel p: freePackagesBySize.get(PackageSize.LARGE)) {
            ArrayList<PackageModel> container = claimedPackages.get(PackageSize.LARGE);
            if (container.size() < capacity.get(PackageSize.LARGE)) {
                cassClient.updateCourierIDPackageInWarehouseByID(p.getPackageID(), getCourierID());
                container.add(p);
            } else {
                break;
            }
        }
        packageLoop:
        for (PackageModel p: freePackagesBySize.get(PackageSize.MEDIUM)) {
            PackageSize[] sizesToTry = new PackageSize[]{PackageSize.MEDIUM, PackageSize.LARGE};
            containerSizeLoop:
            for (PackageSize containerSize: sizesToTry) {
                ArrayList<PackageModel> container = claimedPackages.get(containerSize);
                if (container.size() < capacity.get(containerSize)) {
                    cassClient.updateCourierIDPackageInWarehouseByID(p.getPackageID(), getCourierID());
                    container.add(p);
                    break containerSizeLoop;
                } else {
                    break packageLoop;
                }
            }
        }
        packageLoop:
        for (PackageModel p: freePackagesBySize.get(PackageSize.SMALL)) {
            PackageSize[] sizesToTry = new PackageSize[]{PackageSize.SMALL, PackageSize.MEDIUM, PackageSize.LARGE};
            containerSizeLoop:
            for (PackageSize containerSize: sizesToTry) {
                ArrayList<PackageModel> container = claimedPackages.get(containerSize);
                if (container.size() < capacity.get(containerSize)) {
                    cassClient.updateCourierIDPackageInWarehouseByID(p.getPackageID(), getCourierID());
                    break containerSizeLoop;
                } else {
                    break packageLoop;
                }
            }
        }

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.getMessage();
        }

        for (PackageSize containerSize: PackageSize.values()) {
            for (PackageModel p: claimedPackages.get(containerSize)) {
                PackageModel checkPackage = cassClient.getPackageInWarehouseByID(p.getPackageID());
                if (checkPackage != null && checkPackage.getCourierID().equals(getCourierID())) {
                    p.setCourierID(getCourierID());
                    System.out.println("I'm taking package " + p.getPackageID() + ".");
                    cassClient.upsertPackageInTrunk(p, containerSize);
                    cassClient.deletePackageFromWarehouseByID(p.getPackageID());
                } else {
                    System.out.println("Someone else took the package " + p.getPackageID() + ".");
                }
            }
        }
    }

    private int getFreeSpace(PackageSize containerSize) {
        return capacity.get(containerSize) - claimedPackages.get(containerSize).size();
    }

}
