package nearchat.xiaoxiong.com.nearchat.javabean;

/*
 * Created by Administrator on 2017/5/23.
 */

public class NearUser {
    private User user;

    private UserLocation userLocation;

    public NearUser(User user, UserLocation userLocation) {
        this.user = user;
        this.userLocation = userLocation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }
}
