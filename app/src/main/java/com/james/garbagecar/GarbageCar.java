package com.james.garbagecar;

/**
 * Created by James on 2017/8/31.
 */

public class GarbageCar {
    String TAG = GarbageCar.class.getSimpleName();
    String lineid;
    String car;
    String time;
    String location;
    String longitude;
    String latitude;
    String cityid;
    String cityname;
    String distance;

    public GarbageCar(String lineid, String car, String time, String location, String longitude, String latitude, String cityid, String cityname, String distance) {
        this.lineid = lineid;
        this.car = car;
        this.time = time;
        this.location = location;
        this.longitude = longitude;
        this.latitude = latitude;
        this.cityid = cityid;
        this.cityname = cityname;
        this.distance = distance;
    }

    public String getLineid() {
        return lineid;
    }

    public String getCar() {
        return car;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getLongitude() {
        return longitude;
    }
    public String getLatitude() {
        return latitude;
    }
    public String getCityid() {
        return cityid;
    }
    public String getCityname() {
        return cityname;
    }
    public String getDistance() {
        return distance;
    }
}
