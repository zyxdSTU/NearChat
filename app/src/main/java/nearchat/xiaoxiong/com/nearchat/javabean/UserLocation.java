package nearchat.xiaoxiong.com.nearchat.javabean;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/5/18.
 */

public class UserLocation {
    private String phoneNumber;
    private double longitude;
    private double latitude;
    private long currentTime;

    public  UserLocation(String phoneNumber, double longitude, double latitude, long currentTime) {
        this.phoneNumber = phoneNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.currentTime = currentTime;
    }

    public UserLocation() {}


    public String getPhoneNumber() {return phoneNumber;}
    public double getLongitude() {return longitude;}
    public double getLatitude() {return latitude;}
    public long getCurrentTime() {return currentTime;}

    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}
    public void setLongitude(double longitude) {this.longitude = longitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}
    public void setCurrentTime(long currentTime) {this.currentTime = currentTime;}
}
