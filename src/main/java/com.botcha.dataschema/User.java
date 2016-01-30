package com.botcha.dataschema;

/**
 * Created by shverm on 1/29/2016.
 */
public class User {
    public int userId;
    public String userTag;
    private transient GeoCoordinates coordinates;

    public User(int userId, String userTag) {
        this.userId = userId;
        this.userTag = userTag;
        coordinates = new GeoCoordinates();
    }

    public GeoCoordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(GeoCoordinates coordinates) {
        this.coordinates = coordinates;
    }
}
