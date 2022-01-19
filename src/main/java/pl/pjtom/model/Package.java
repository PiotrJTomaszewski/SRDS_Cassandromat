package pl.pjtom.model;

import java.time.Instant;
import java.util.UUID;

public class Package {
    private String packageID;
    private String destDistrict;
    private PackageSize size;
    private String clientID;
    private Instant plannedDeliveryDatetime;
    private Instant pickupDatetime;
    private boolean isInWarehouse;
    private boolean isDelivered;
    private boolean isPickedUp;

    public Package() {

    }

    public Package(String packageID, String destDistrict, PackageSize size, String clientID,
            Instant plannedDeliveryDatetime, Instant pickupDatetime, boolean isDelivered, boolean isPickedUp) {
        this.packageID = packageID;
        this.destDistrict = destDistrict;
        this.size = size;
        this.clientID = clientID;
        this.plannedDeliveryDatetime = plannedDeliveryDatetime;
        this.pickupDatetime = pickupDatetime;
        this.isDelivered = isDelivered;
        this.isPickedUp = isPickedUp;
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

    public String getDestDistrict() {
        return destDistrict;
    }

    public void setDestDistrict(String destDistrict) {
        this.destDistrict = destDistrict;
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

    public Instant getPlannedDeliveryDatetime() {
        return plannedDeliveryDatetime;
    }

    public void setPlannedDeliveryDatetime(Instant plannedDeliveryDatetime) {
        this.plannedDeliveryDatetime = plannedDeliveryDatetime;
    }

    public Instant getPickupDatetime() {
        return pickupDatetime;
    }

    public void setPickupDatetime(Instant pickupDatetime) {
        this.pickupDatetime = pickupDatetime;
    }

    public boolean getIsInWarehouse() {
        return isInWarehouse;
    }

    public void setIsInWarehouse(boolean isInWarehouse) {
        this.isInWarehouse = isInWarehouse;
    }

    public boolean getIsDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean isDelivered) {
        this.isDelivered = isDelivered;
    }

    public boolean getIsPickedUp() {
        return isPickedUp;
    }

    public void setPickedUp(boolean isPickedUp) {
        this.isPickedUp = isPickedUp;
    }

}
