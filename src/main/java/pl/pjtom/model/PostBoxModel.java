package pl.pjtom.model;

import java.util.EnumMap;
import java.util.UUID;

public class PostBoxModel {
    private String postboxID;
    private String district;
    private int capacity;

    public PostBoxModel() {

    }

    public PostBoxModel(String postboxID, String district) {
        this.postboxID = postboxID;
        this.district = district;
    }

    public void generatePostboxID() {
        this.postboxID = UUID.randomUUID().toString();
    }

    public String getPostboxID() {
        return postboxID;
    }

    public void setPostboxID(String postboxID) {
        this.postboxID = postboxID;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
