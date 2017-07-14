package nearchat.xiaoxiong.com.nearchat.db;

import android.util.Log;

import com.hyphenate.chat.EMClient;

import org.litepal.crud.DataSupport;

import java.util.List;

import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessage;
import nearchat.xiaoxiong.com.nearchat.javabean.Trend;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.javabean.Word;

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
        List<User> userList = DataSupport.findAll(User.class);
        return userList;
    }

    synchronized public void deleteContact(String phoneNumber) {
        DataSupport.deleteAll(User.class, "phoneNumber = ?", phoneNumber);
    }

    synchronized public void addContact(User user) {
        user.save();
    }

    synchronized public void cleanupContactList() {
        DataSupport.deleteAll(User.class);
        Log.d("MainActivity", "userList" + String.valueOf(getContactList().size()));
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

    /**删除交友消息**/
    synchronized public void deleteInviteMessage(InviteMessage inviteMessage) {
         DataSupport.deleteAll(InviteMessage.class, "to = ? and from = ? and currenttime = ?", inviteMessage.getTo(), inviteMessage.getFrom(), String.valueOf(inviteMessage.getCurrentTime()));
    }

    /**添加动态**/
    synchronized public void addTrend(Trend trend) {
        trend.save();
    }

    /*查看所有动态*/
    synchronized  public List<Trend> getAllTrend() {
        List<Trend> trendList = DataSupport.findAll(Trend.class);
        if(trendList.size() == 0) return null;
        else return trendList;
    }

    /*查看所有留言*/
    synchronized  public List<Word> getAllWord() {
        List<Word> wordList = DataSupport.findAll(Word.class);
        if(wordList.size() == 0) return null;
        else return wordList;
    }

    /*查看某条动态的留言*/
    synchronized  public List<Word> getWordOfTrend(String trendId) {
        List<Word> wordList = DataSupport.where("trendId = ?", trendId).find(Word.class);
        if(wordList.size() == 0) return null;
        else return wordList;
    }

    /**添加留言**/
    synchronized public void addWord(Word word) {
        word.save();
    }
}
