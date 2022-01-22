package pl.pjtom.model;

import java.util.EnumMap;
import java.util.UUID;

public class Courier {
    private String courierID;
    private EnumMap<PackageSize, Integer> capacity = new EnumMap<>(PackageSize.class);

    public Courier() {

    }

    public Courier(String courierID, EnumMap<PackageSize, Integer> capacity) {
        this.courierID = courierID;
        this.capacity = capacity;
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

}
