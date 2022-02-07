package pl.pjtom.model;

import java.util.Date;

public class PackageLogEntryModel implements Comparable<PackageLogEntryModel> {
    private String packageID;
    private PackageLogEvent actionType;
    private Date actionTime;
    private String actorID;
    private String postBoxID;

    public PackageLogEntryModel() {

    }

    public PackageLogEntryModel(String packageID, PackageLogEvent actionType, Date actionTime, String actorID, String postBoxID) {
        this.packageID = packageID;
        this.actionType = actionType;
        this.actionTime = actionTime;
        this.actorID = actorID;
        this.postBoxID = postBoxID;
    }

    public String getPackageID() {
        return packageID;
    }

    public void setPackageID(String packageID) {
        this.packageID = packageID;
    }

    public PackageLogEvent getActionType() {
        return actionType;
    }

    public void setActionType(PackageLogEvent actionType) {
        this.actionType = actionType;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    public String getActorID() {
        return actorID;
    }

    public void setActorID(String actorID) {
        this.actorID = actorID;
    }

    public String getPostBoxID() {
        return postBoxID;
    }

    public void setPostBoxID(String postBoxID) {
        this.postBoxID = postBoxID;
    }

    @Override
    public int compareTo(PackageLogEntryModel val) {
        PackageLogEntryModel other = (PackageLogEntryModel)val;
        return this.actionTime.compareTo(other.actionTime);
    }

}
