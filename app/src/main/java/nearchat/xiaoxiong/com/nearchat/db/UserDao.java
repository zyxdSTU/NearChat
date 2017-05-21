package nearchat.xiaoxiong.com.nearchat.db;

import java.util.List;

import nearchat.xiaoxiong.com.nearchat.javabean.User;

/**
 * Created by Administrator on 2017/5/18.
 */

public class UserDao {
    public UserDao() {

    }

    public List<User> getContactList() {
        return DBManager.getInstance().getContactList();
    }

    public void deleteContact(String phoneNumber) {
        DBManager.getInstance().deleteContact(phoneNumber);
    }

    public void addContact(User user) {
        DBManager.getInstance().addContact(user);
    }

    public void cleanupContactList() {
        DBManager.getInstance().cleanupContactList();
    }

    public void updateContact(User user) {
        DBManager.getInstance().updateContact(user);
    }

    public User getContact(String phoneNumber) {return DBManager.getInstance().getContact(phoneNumber);}
}
