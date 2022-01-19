package pl.pjtom.model;

import java.time.LocalTime;
import java.util.UUID;

public class Courier {
    private String courierID;
    private LocalTime workdayStart;
    private LocalTime workdayEnd;

    public Courier() {
        
    }

    public Courier(String courierID, LocalTime workdayStart, LocalTime workdayEnd) {
        this.courierID = courierID;
        this.workdayStart = workdayStart;
        this.workdayEnd = workdayEnd;
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

    public LocalTime getWorkdayStart() {
        return workdayStart;
    }

    public void setWorkdayStart(LocalTime workdayStart) {
        this.workdayStart = workdayStart;
    }

    public LocalTime getWorkdayEnd() {
        return workdayEnd;
    }

    public void setWorkdayEnd(LocalTime workdayEnd) {
        this.workdayEnd = workdayEnd;
    }
}
