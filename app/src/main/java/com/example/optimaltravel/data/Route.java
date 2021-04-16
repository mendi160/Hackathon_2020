package com.example.optimaltravel.data;

import java.util.List;

public class Route {
    private String routeID;
    List<String> point;
    public Route(){};
    public String getRouteID() {
        return routeID;
    }

    public void setRouteID(String routeID) {
        this.routeID = routeID;
    }

    public List<String> getPoint() {
        return point;
    }

    public void setPoint(List<String> point) {
        this.point = point;
    }
}
