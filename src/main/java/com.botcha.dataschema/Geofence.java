package com.botcha.dataschema;

import java.util.ArrayList;
import java.util.List;

public class Geofence {

    public List<GeoCoordinates> coordinates;
    private transient double[] constant, multiple;

    public Geofence(GeoCoordinates leftBottom, GeoCoordinates rightBottom, GeoCoordinates leftTop, GeoCoordinates rightTop) {
        coordinates = new ArrayList<>();

        addCoordinate(rightTop);
        addCoordinate(leftTop);
        addCoordinate(leftBottom);
        addCoordinate(rightBottom);
        precalc_values();
    }

    public Geofence(List<GeoCoordinates> coordinates) {
        this.coordinates = coordinates;
        precalc_values();
    }

    private void precalc_values() {

        constant = new double[coordinates.size()];
        multiple = new double[coordinates.size()];

        int i, j = coordinates.size() - 1;

        for (i = 0; i < coordinates.size(); i++) {
            double ix = coordinates.get(i).latitude, iy = coordinates.get(i).longitude,
                    jx = coordinates.get(j).latitude, jy = coordinates.get(j).longitude;

            if (jy == iy) {
                constant[i] = ix;
                multiple[i] = 0;
            } else {
                constant[i] = ix - (iy * jx) / (jy - iy) + (iy * ix) / (jy - iy);
                multiple[i] = (jx - ix) / (jy - iy);
            }
            j = i;
        }
    }

    private boolean pointInPolygon(double x, double y) {

        int i, j = coordinates.size() - 1;
        boolean oddNodes = false;

        for (i = 0; i < coordinates.size(); i++) {

            double iy = coordinates.get(i).longitude, jy = coordinates.get(j).longitude;

            if ((iy < y && jy >= y
                    || jy < y && iy >= y)) {
                oddNodes ^= (y * multiple[i] + constant[i] < x);
            }
            j = i;
        }

        return oddNodes;
    }

    private void addCoordinate(GeoCoordinates coordinate) {
        coordinates.add(coordinate);
    }


    //  Globals which should be set before calling these functions:
//
//  int    polyCorners  =  how many corners the polygon has (no repeats)
//  float  polyX[]      =  horizontal coordinates of corners
//  float  polyY[]      =  vertical coordinates of corners
//  float  x, y         =  point to be tested
//
//  The following global arrays should be allocated before calling these functions:
//
//  float  constant[] = storage for precalculated constants (same size as polyX)
//  float  multiple[] = storage for precalculated multipliers (same size as polyX)
//
//  (Globals are used in this example for purposes of speed.  Change as
//  desired.)
//
//  USAGE:
//  Call precalc_values() to initialize the constant[] and multiple[] arrays,
//  then call pointInPolygon(x, y) to determine if the point is in the polygon.
//
//  The function will return YES if the point x,y is inside the polygon, or
//  NO if it is not.  If the point is exactly on the edge of the polygon,
//  then the function may return YES or NO.
//
//  Note that division by zero is avoided because the division is protected
//  by the "if" clause which surrounds it.

    public boolean Contains(GeoCoordinates coordinates) {
        return Contains(coordinates.latitude, coordinates.longitude);
    }

    public boolean Contains(double latitude, double longitude) {
        return pointInPolygon(latitude, longitude);
    }

}
