package pl.pjtom;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.PostBoxModel;
import pl.pjtom.model.CourierModel;
import pl.pjtom.model.PackageLogEvent;
import pl.pjtom.model.PackageLogEntryModel;
import pl.pjtom.model.PackageModel;

public class Courier implements Runnable {
    private CassandraConnector cassClient;
    private Random rand = new Random();
    private CourierModel courierModel;
    private ArrayList<PackageModel> trunkContent = new ArrayList<>();

    public Courier(CassandraConnector cassClient, CourierModel courierModel) throws CassandraBackendException {
        this.cassClient = cassClient;
        this.courierModel = courierModel;
    }

    public void loadTheTrunk() throws CassandraBackendException {
        boolean stayAtWarehouse = true;
        while (stayAtWarehouse) {
            ArrayList<String> districts = cassClient.getDistricts();
            // Choose next trip destination district
            String destinationDistrict = districts.get(rand.nextInt(districts.size()));
            ArrayList<PackageModel> claimedPackages = new ArrayList<>();
            // System.out.println(courierModel.getCourierID() + ": Going to the " + destinationDistrict + " district.");

            // Get the list of free packages in warehouse
            ArrayList<PackageModel> packages = cassClient.getPackagesInWarehouseByDistrict(destinationDistrict);

            // Claiming packages the courier wants to pick up
            for (PackageModel p: packages) {
                if (p.getCourierID() == null) {
                    if (trunkContent.size() + claimedPackages.size() < courierModel.getCapacity()) {
                        cassClient.updateCourierIDPackageInWarehouseByID(destinationDistrict, p.getPackageID(), courierModel.getCourierID());
                        claimedPackages.add(p);
                    } else {
                        break;
                    }
                }
            }

            // Wait to see if the packages were successfully claimed
            try {
                Thread.sleep(150 + rand.nextInt(50));
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

            // Move successfully claimed packages to the trunk
            for (PackageModel p: claimedPackages) {
                PackageModel checkPackage = cassClient.getPackageInWarehouseByID(destinationDistrict, p.getPackageID());
                if (checkPackage != null && checkPackage.getCourierID() != null && checkPackage.getCourierID().equals(courierModel.getCourierID())) {
                    p.setCourierID(courierModel.getCourierID());
                    System.out.println(courierModel.getCourierID() + ": I'm taking package " + p.getPackageID() + ".");
                    trunkContent.add(p);
                    Date timestamp = new Date(System.currentTimeMillis());
                    cassClient.deletePackageFromWarehouseByID(destinationDistrict, p.getPackageID());
                    cassClient.upsertPackageLog(new PackageLogEntryModel(p.getPackageID(), PackageLogEvent.TAKE_PACKAGE_FROM_WAREHOUSE, timestamp, courierModel.getCourierID(), null));
                } else {
                    // System.out.println("Someone else took the package " + p.getPackageID() + ".");
                }
            }

            System.out.print("I have " + trunkContent.size() + "/" + courierModel.getCapacity() + " packages.");
            if (trunkContent.size() == courierModel.getCapacity()) {
                // System.out.println("Leaving the warehouse with a full trunk");
                stayAtWarehouse = false;
            } else {
                if (trunkContent.size() > 0 && rand.nextInt(100) < 30) {
                    // System.out.println(" I'm leaving anyway");
                    stayAtWarehouse = false;
                } else {
                    // System.out.println(" I'll stay for a bit longer");
                    try {
                        Thread.sleep(250 + rand.nextInt(50));
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
    }

    public void deliverPackages() throws CassandraBackendException {        
        // ArrayList<PackageModel> packagesInTrunk = cassClient.getPackagesInTrunk(courierModel.getCourierID());
        ArrayList<PackageModel> packagesToClaim = new ArrayList<>(trunkContent);
        String district = trunkContent.get(0).getDistrictDest();
        while (trunkContent.size() > 0) {
            // Find a post box with free space
            ArrayList<PostBoxModel> postBoxes = cassClient.getPostBoxesInDistrict(district);
            Optional<PostBoxModel> freePostBox = Optional.empty();
            int freePostBoxCapacityLeft = 0;
            for (PostBoxModel postBox: postBoxes) {
                int postBoxPackagesCount = cassClient.countPackagesInPostBox(postBox.getPostBoxID());
                if (postBoxPackagesCount < postBox.getCapacity()) {
                    freePostBox = Optional.of(postBox);
                    freePostBoxCapacityLeft = postBox.getCapacity() - postBoxPackagesCount;
                    break;
                }
            }

            if (freePostBox.isPresent()) {
                // Packages for which a space in a post box was claimed
                ArrayList<PackageModel> claimedPackages = new ArrayList<PackageModel>();
                PostBoxModel postBox = freePostBox.get();
                for (PackageModel p: packagesToClaim) {
                    p.setIsReadyToPickup(false);
                    cassClient.upsertPackageInPostBox(postBox.getPostBoxID(), p);
                    claimedPackages.add(p);
                    System.out.println(courierModel.getCourierID() + ": Claimed space in " + postBox.getPostBoxID() + " for " + p.getPackageID() + ".");
                    freePostBoxCapacityLeft -= 1;
                    if (freePostBoxCapacityLeft == 0) {
                        break;
                    }
                }
                packagesToClaim.removeAll(claimedPackages);

                // System.out.println("Traveling to the post box " + postBox.getPostBoxID() + ".");
                try {
                    Thread.sleep(150 + rand.nextInt(50));
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }

                int packagesToUnclaimCount = cassClient.countPackagesInPostBox(postBox.getPostBoxID()) - postBox.getCapacity();
                if (packagesToUnclaimCount > 0) { // Post box is full
                    // System.out.println(courierModel.getCourierID() + ": Postbox " + postBox.getPostBoxID() + " is full. I have to unclaim " + packagesToUnclaimCount + " packages.");
                    // Unclaim packages that wouldn't fit
                    for (int i=0; i<packagesToUnclaimCount && i<claimedPackages.size(); i++) {
                        int packageToUnclaimIndex = claimedPackages.size() - 1 - i;
                        PackageModel p = claimedPackages.get(packageToUnclaimIndex);
                        cassClient.deletePackageFromPostBox(postBox.getPostBoxID(), p.getPackageID());
                        claimedPackages.remove(packageToUnclaimIndex);
                    }
                }
                // Put the rest of the claimed packages in the post box
                for (PackageModel p: claimedPackages) {
                    p.setIsReadyToPickup(true);
                    Date timestamp = new Date(System.currentTimeMillis());
                    cassClient.upsertPackageInPostBox(postBox.getPostBoxID(), p);
                    trunkContent.remove(p);
                    // cassClient.deletePackageFromTrunkByID(courierModel.getCourierID(), p.getPackageID());
                    cassClient.upsertPackageLog(new PackageLogEntryModel(p.getPackageID(), PackageLogEvent.PUT_PACKAGE_IN_POSTBOX, timestamp, courierModel.getCourierID(), postBox.getPostBoxID()));
                    System.out.println(courierModel.getCourierID() + ": Putting package " + p.getPackageID() + " in post box " + postBox.getPostBoxID() + ".");
                }

            } else {
                // System.out.println("There is no free post box in " + district + ". Waiting for a bit.");
                try {
                    Thread.sleep(100 + rand.nextInt(20));
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public String getCourierID() {
        return courierModel.getCourierID();
    }

    @Override
    public void run() {
        while (true) {
            try {
                loadTheTrunk();
                deliverPackages();
                // Go back to the warehouse
                // Thread.sleep(500 + rand.nextInt(100));
            } catch (CassandraBackendException e) {
                System.err.println(e.getMessage());
            }
            // catch (InterruptedException e) {
            //     System.err.println(e.getMessage());
            // }
        }
    }

}
