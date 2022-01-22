package pl.pjtom.model;

import java.util.EnumMap;
import java.util.UUID;

public class PostBoxModel {
    private String postboxID;
    private String district;
    private EnumMap<PackageSize, Integer> capacity = new EnumMap<>(PackageSize.class);

    public PostBoxModel() {
        capacity.put(PackageSize.SMALL, -1);
        capacity.put(PackageSize.MEDIUM, -1);
        capacity.put(PackageSize.LARGE, -1);
    }

    public PostBoxModel(String postboxID, String district, EnumMap<PackageSize, Integer> capacity) {
        this.postboxID = postboxID;
        this.district = district;
        this.capacity = capacity;

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

    public Integer getCapacity(PackageSize size) {
        return capacity.get(size);
    }

    public void setCapacity(PackageSize size, Integer count) {
        this.capacity.put(size, count);
    }

}
