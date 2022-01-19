package pl.pjtom.model;

import java.util.EnumMap;
import java.util.UUID;

public class Courier {
    private String courierID;
    private EnumMap<PackageSize, Integer> capacity = new EnumMap<>(PackageSize.class);
    private EnumMap<PackageSize, Integer> capacityLeft = new EnumMap<>(PackageSize.class);

    public Courier() {

    }

    public Courier(String courierID, EnumMap<PackageSize, Integer> capacity,
            EnumMap<PackageSize, Integer> capacityLeft) {
        this.courierID = courierID;
        this.capacity = capacity;
        this.capacityLeft = capacityLeft;
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

    public Integer getCapacityLeft(PackageSize size) {
        return capacityLeft.get(size);
    }

    public void setCapacityLeft(PackageSize size, Integer count) {
        this.capacityLeft.put(size, count);
    }

}
