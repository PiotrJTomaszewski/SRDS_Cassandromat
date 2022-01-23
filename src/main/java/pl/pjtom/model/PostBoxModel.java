package pl.pjtom.model;

import java.util.UUID;

public class PostBoxModel {
    private String postBoxID;
    private String district;
    private int capacity;

    public PostBoxModel() {

    }

    public PostBoxModel(String postboxID, String district) {
        this.postBoxID = postboxID;
        this.district = district;
    }

    public void generatePostboxID() {
        this.postBoxID = UUID.randomUUID().toString();
    }

    public String getPostBoxID() {
        return postBoxID;
    }

    public void setPostboxID(String postboxID) {
        this.postBoxID = postboxID;
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
