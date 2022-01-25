package pl.pjtom.model;

import java.util.Formatter;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format("{courier_id: %s, capacity: %d}", courierID, capacity);
        fmt.close();
        return sb.toString();
    }
}
