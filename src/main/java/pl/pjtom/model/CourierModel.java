package pl.pjtom.model;

import java.util.UUID;

public class CourierModel {
    private String courierID;
    private int capacity;

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
}
