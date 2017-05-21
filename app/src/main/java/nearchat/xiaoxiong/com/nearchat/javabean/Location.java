package nearchat.xiaoxiong.com.nearchat.javabean;

/**
 * Created by Administrator on 2017/5/18.
 */

public class Location {
    private String phoneNumber;
    private double longitude;
    private double latitude;

    public  Location(String phoneNumber, double longitude, double latitude) {
        this.phoneNumber = phoneNumber;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Location() {}


    public String getPhoneNumber() {return phoneNumber;}
    public double getLongitude() {return longitude;}
    public double getLatitude() {return latitude;}

    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}
    public void setLongitude(double longitude) {this.longitude = longitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}
}
