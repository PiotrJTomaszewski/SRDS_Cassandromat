package pl.pjtom.model;

import java.util.UUID;

public class Client {
    private String clientID;
    private String district;

    public Client() {

    }

    public Client(String clientID, String district) {
        this.clientID = clientID;
        this.district = district;
    }

    public void generateClientID() {
        clientID = UUID.randomUUID().toString();
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
