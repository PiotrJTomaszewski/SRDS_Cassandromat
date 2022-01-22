package pl.pjtom.model;

import java.util.Formatter;
import java.util.UUID;

public class Package {
    private String packageID;
    private String districtDest;
    private PackageSize size;
    private String clientID;
    private String courierID;

    public Package() {

    }

    public Package(String packageID, String courierID, String districtDest, PackageSize size, String clientID) {
        this.packageID = packageID;
        this.courierID = courierID;
        this.districtDest = districtDest;
        this.size = size;
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

    public PackageSize getSize() {
        return size;
    }

    public void setSize(PackageSize size) {
        this.size = size;
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
        fmt.format("{package_id: %s, courier_id: %s, district_dest: %s, size: %s, client_id: %s}", packageID, courierID, districtDest, size, clientID);
        fmt.close();
        return sb.toString();
    }
}
