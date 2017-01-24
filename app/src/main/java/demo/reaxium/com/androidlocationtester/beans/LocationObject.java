package demo.reaxium.com.androidlocationtester.beans;

import java.util.Date;

/**
 * Created by Eduardo Luttinger on 22/05/2016.
 */
public class LocationObject extends AppBean {

    private double longitude;
    private double latitude;
    private float speed;
    private double altitude;
    private float bearing;
    private float accuracy;
    private String proveedor;
    private Date time;


    public LocationObject() {
    }

    public LocationObject(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }


    public LocationObject(double longitude, double latitude, float speed, double altitude, float bearing, float accuracy, String proveedor, Date time) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
        this.altitude = altitude;
        this.bearing = bearing;
        this.accuracy = accuracy;
        this.proveedor = proveedor;
        this.time = time;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
