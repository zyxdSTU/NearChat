package nearchat.xiaoxiong.com.nearchat.db;

import com.hyphenate.chat.EMClient;

import org.litepal.crud.DataSupport;

import java.util.List;

import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessage;
import nearchat.xiaoxiong.com.nearchat.javabean.User;

/**
 * Created by Administrator on 2017/5/18.
 */

public class DBManager {
    private static DBManager dbManager;

    private DBManager() {

    }
    public static synchronized DBManager getInstance() {
        if(dbManager == null) {
            dbManager = new DBManager();
        }
        return dbManager;
    }

    synchronized public User getContact(String phoneNumber) {
        List<User> listUser = DataSupport.where("phoneNumber = ?", phoneNumber).find(User.class);
        if(listUser.size() != 0) {
            return listUser.get(0);
        } else return null;
    }

    synchronized public List<User> getContactList() {
        return DataSupport.findAll(User.class);
    }

    synchronized public void deleteContact(String phoneNumber) {
        DataSupport.deleteAll(User.class, "phoneNumber = ?", phoneNumber);
    }

    synchronized public void addContact(User user) {
        user.save();
    }

    synchronized public void cleanupContactList() {
        DataSupport.deleteAll(User.class);
    }

    synchronized public void updateContact(User user) {
        user.updateAll("phoneNumber = ?", user.getPhoneNumber());
    }

    synchronized public List<InviteMessage> getInviteMessageList() {
       return DataSupport.where("to = ?", EMClient.getInstance().getCurrentUser()).find(InviteMessage.class);
    }

    synchronized public void saveInviteMessage(InviteMessage inviteMessage) {
       inviteMessage.save();
    }

    /**更新消息**/
    synchronized public void updateInviteMessage(InviteMessage inviteMessage) {
        inviteMessage.updateAll("to = ? and from = ? and currenttime = ?", EMClient.getInstance().getCurrentUser(), inviteMessage.getFrom(), String.valueOf(inviteMessage.getCurrentTime()));
    }
}
