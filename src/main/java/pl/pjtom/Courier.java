package pl.pjtom;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import pl.pjtom.cassandra.CassandraBackendException;
import pl.pjtom.cassandra.CassandraConnector;
import pl.pjtom.model.PostBoxModel;
import pl.pjtom.model.CourierModel;
import pl.pjtom.model.PackageLogEvent;
import pl.pjtom.model.PackageModel;

public class Courier implements Runnable {
    private CassandraConnector cassClient;
    private Random rand = new Random();
    private CourierModel courierModel;

    public Courier(CassandraConnector cassClient, CourierModel courierModel) throws CassandraBackendException {
        this.cassClient = cassClient;
        this.courierModel = courierModel;
    }

    public void loadTheTrunk() throws CassandraBackendException {
        boolean stayAtWarehouse = true;
        int packagesInTrunkCount = 0;
        while (stayAtWarehouse) {
            // Get the list of free packages in warehouse
            ArrayList<PackageModel> packages = cassClient.getPackagesInWarehouse();
            ArrayList<String> districts = cassClient.getDistricts();
            String destinationDistrict = districts.get(rand.nextInt(districts.size()));
            ArrayList<PackageModel> claimedPackages = new ArrayList<>();
            // System.out.println("Going to the " + destinationDistrict + " district.");

            // Claiming packages the courier wants to pick up
            for (PackageModel p: packages) {
                if (p.getCourierID() == null && p.getDistrictDest().equals(destinationDistrict)) {
                    if (packagesInTrunkCount + claimedPackages.size() < courierModel.getCapacity()) {
                        cassClient.updateCourierIDPackageInWarehouseByID(p.getPackageID(), courierModel.getCourierID());
                        claimedPackages.add(p);
                    } else {
                        break;
                    }
                }
            }

            // Wait to see if the packages were successfully claimed
            try {
                Thread.sleep(300 + rand.nextInt(100));
            } catch (InterruptedException e) {
                e.getMessage();
            }

            // Move successfully claimed packages to the trunk
            for (PackageModel p: claimedPackages) {
                PackageModel checkPackage = cassClient.getPackageInWarehouseByID(p.getPackageID());
                if (checkPackage != null && checkPackage.getCourierID().equals(courierModel.getCourierID())) {
                    p.setCourierID(courierModel.getCourierID());
                    // System.out.println("I'm taking package " + p.getPackageID() + ".");
                    cassClient.upsertPackageInTrunk(p);
                    cassClient.deletePackageFromWarehouseByID(p.getPackageID());
                    cassClient.upsertPackageLog(p.getPackageID(), PackageLogEvent.TAKE_PACKAGE_FROM_WAREHOUSE, courierModel.getCourierID());
                    packagesInTrunkCount += 1;
                } else {
                    // System.out.println("Someone else took the package " + p.getPackageID() + ".");
                }
            }

            System.out.print("I have " + packagesInTrunkCount + "/" + courierModel.getCapacity() + " packages.");
            if (packagesInTrunkCount == courierModel.getCapacity()) {
                // System.out.println("Leaving the warehouse with a full trunk");
                stayAtWarehouse = false;
            } else {
                if (packagesInTrunkCount > 0 && rand.nextInt(100) < 30) {
                    // System.out.println(" I'm leaving anyway");
                    stayAtWarehouse = false;
                } else {
                    // System.out.println(" I'll stay for a bit longer");
                    try {
                        Thread.sleep(500 + rand.nextInt(100));
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    public void deliverPackages() throws CassandraBackendException {        
        ArrayList<PackageModel> packagesInTrunk = cassClient.getPackagesInTrunk(courierModel.getCourierID());
        ArrayList<PackageModel> packagesToClaim = new ArrayList<>(packagesInTrunk);
        String district = packagesInTrunk.get(0).getDistrictDest();
        while (packagesInTrunk.size() > 0) {
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
                    System.out.println("Claimed space in " + postBox.getPostBoxID() + " for " + p.getPackageID() + ".");
                    freePostBoxCapacityLeft -= 1;
                    if (freePostBoxCapacityLeft == 0) {
                        break;
                    }
                }
                packagesToClaim.removeAll(claimedPackages);

                // System.out.println("Traveling to the post box " + postBox.getPostBoxID() + ".");
                try {
                    Thread.sleep(300 + rand.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int packagesToUnclaimCount = cassClient.countPackagesInPostBox(postBox.getPostBoxID()) - postBox.getCapacity();
                if (packagesToUnclaimCount > 0) { // Post box is full
                    System.out.println("Postbox " + postBox.getPostBoxID() + " is full. I have to unclaim " + packagesToUnclaimCount + " packages.");
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
                    cassClient.upsertPackageInPostBox(postBox.getPostBoxID(), p);
                    packagesInTrunk.remove(p);
                    cassClient.upsertPackageLog(p.getPackageID(), PackageLogEvent.PUT_PACKAGE_IN_POSTBOX, courierModel.getCourierID());
                    System.out.println("Putting package " + p.getPackageID() + " in post box " + postBox.getPostBoxID() + ".");
                }

            } else {
                // System.out.println("There is no free post box in " + district + ". Waiting for a bit.");
                try {
                    Thread.sleep(100 + rand.nextInt(20));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
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
