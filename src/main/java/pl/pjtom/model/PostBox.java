package pl.pjtom.model;

import java.util.EnumMap;
import java.util.UUID;

public class PostBox {
    private String postboxID;
    private String district;
    private EnumMap<PackageSize, Integer> capacity = new EnumMap<>(PackageSize.class);
    private EnumMap<PackageSize, Integer> capacityLeft = new EnumMap<>(PackageSize.class);

    public PostBox() {

    }

    public PostBox(String postboxID, String district, EnumMap<PackageSize, Integer> capacity,
            EnumMap<PackageSize, Integer> capacityLeft) {
        this.postboxID = postboxID;
        this.district = district;
        this.capacity = capacity;
        this.capacityLeft = capacityLeft;

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

    public Integer getCapacityLeft(PackageSize size) {
        return capacityLeft.get(size);
    }

    public void setCapacityLeft(PackageSize size, Integer count) {
        this.capacityLeft.put(size, count);
    }

}
