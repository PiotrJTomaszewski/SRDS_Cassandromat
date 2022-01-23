package pl.pjtom.model;

import java.util.Formatter;
import java.util.UUID;

public class PackageModel {
    private String packageID;
    private String districtDest;
    private String clientID;
    private String courierID;
    private boolean isReadyToPickup;

    public PackageModel() {

    }

    public void generatePackageID() {
        this.packageID = UUID.randomUUID().toString();
    }

    public String getPackageID() {
        return packageID;
    }

    public void setPackageID(String packageID) {
        this.packageID = packageID;
    }

    public String getDistrictDest() {
        return districtDest;
    }

    public void setDistrictDest(String districtDest) {
        this.districtDest = districtDest;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getCourierID() {
        return courierID;
    }

    public void setCourierID(String courierID) {
        this.courierID = courierID;
    }

    public boolean getIsReadyToPickup() {
        return isReadyToPickup;
    }

    public void setIsReadyToPickup(boolean isReadyToPickup) {
        this.isReadyToPickup = isReadyToPickup;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format("{package_id: %s, courier_id: %s, district_dest: %s, client_id: %s, is_ready_to_pickup: %s}", packageID, courierID, districtDest, clientID, isReadyToPickup);
        fmt.close();
        return sb.toString();
    }
}
