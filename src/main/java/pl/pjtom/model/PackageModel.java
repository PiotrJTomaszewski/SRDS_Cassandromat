package pl.pjtom.model;

import java.util.Formatter;
import java.util.UUID;

public class PackageModel {
    private String packageID;
    private String districtDest;
    private String clientID;
    private String courierID;

    public PackageModel() {

    }

    public PackageModel(String packageID, String courierID, String districtDest, String clientID) {
        this.packageID = packageID;
        this.courierID = courierID;
        this.districtDest = districtDest;
        this.clientID = clientID;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format("{package_id: %s, courier_id: %s, district_dest: %s, client_id: %s}", packageID, courierID, districtDest, clientID);
        fmt.close();
        return sb.toString();
    }
}
