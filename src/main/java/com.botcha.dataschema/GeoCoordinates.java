package com.botcha.dataschema;

public class GeoCoordinates {
    public double latitude;
    public double longitude;

    public GeoCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GeoCoordinates() {

    }

    public void update(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
