package nearchat.xiaoxiong.com.nearchat.util;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nearchat.xiaoxiong.com.nearchat.db.DBManager;
import nearchat.xiaoxiong.com.nearchat.db.InviteMessageDao;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.Constant;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessage;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessageStatus;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.SELECT_PART_USER;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.SELECT_USER;

/**
 * Created by Administrator on 2017/5/18.
 */

public class Manager {
    private Context mContext;
    private static Manager manager;

    private EMMessageListener messageListener;
    private EMContactListener contactListener;
    private EMConnectionListener connectionListener;
    private LocalBroadcastManager broadcastManager;

    private Manager() {}

    public static synchronized Manager getInstance() {
        if(manager == null) {
            manager = new Manager();
        }
        return manager;
    }

    public void init(Context context) {
        mContext = context;

        /**sdk初始化**/
        initOptions();

        /**缓存初始化**/
        PreferenceManager.init(mContext);

        /**注册消息监听**/
        registerMessageListener();

        /**注册好友监听**/
        registerContactListener();

        /**注册状态监听**/
        registerConnectionListener();

        /**本地广播**/
        broadcastManager = LocalBroadcastManager.getInstance(mContext);
    }

    private void initOptions() {
        /**
         * SDK初始化的一些配置
         */
        EMOptions options = new EMOptions();

        // 设置自动登录
        options.setAutoLogin(true);

        // 设置是否需要发送已读回执
        options.setRequireAck(true);

        // 设置是否需要发送回执，TODO 这个暂时有bug，上层收不到发送回执
        options.setRequireDeliveryAck(true);

        // 收到好友申请是否自动同意，如果是自动同意就不会收到好友请求的回调，因为sdk会自动处理，默认为true
        options.setAcceptInvitationAlways(false);

        // 设置是否自动接收加群邀请，如果设置了当收到群邀请会自动同意加入
        options.setAutoAcceptGroupInvitation(false);

        // 设置（主动或被动）退出群组时，是否删除群聊聊天记录
        options.setDeleteMessagesAsExitGroup(false);

        // 设置是否允许聊天室的Owner 离开并删除聊天室的会话
        options.allowChatroomOwnerLeave(true);

        // 调用初始化方法初始化sdk
        EMClient.getInstance().init(mContext, options);

        // 设置开启debug模式
        EMClient.getInstance().setDebugMode(true);
    }


    private void registerMessageListener() {
        messageListener = new EMMessageListener() {
            /**接受到消息**/
            @Override
            public void onMessageReceived(List<EMMessage> list) {
                playSound();
                broadcastManager.sendBroadcast(new Intent(Constant.MESSAGE_GET));
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> list) {

            }

            @Override
            public void onMessageRead(List<EMMessage> list) {

            }

            @Override
            public void onMessageDelivered(List<EMMessage> list) {

            }

            @Override
            public void onMessageChanged(EMMessage emMessage, Object o) {

            }
        };
        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    private void registerContactListener() {
        contactListener = new EMContactListener() {
            /**增加联系人**/
            @Override
            public void onContactAdded(String s) {
                playSound();
                addContact(s);
                broadcastManager.sendBroadcast(new Intent(Constant.INVITE_ADD));
            }

            /**删除好友**/
            @Override
            public void onContactDeleted(String s) {

            }

            /**接受到好友邀请**/
            @Override
            public void onContactInvited(String s, String s1) {
                playSound();
                InviteMessage inviteMessage = new InviteMessage();
                inviteMessage.setFrom(s);
                inviteMessage.setReason(s1);
                inviteMessage.setTo(EMClient.getInstance().getCurrentUser());
                inviteMessage.setCurrentTime(System.currentTimeMillis());
                inviteMessage.setStatus(InviteMessageStatus.BEINVITEED);
                new InviteMessageDao().saveInviteMessage(inviteMessage);
                broadcastManager.sendBroadcast(new Intent(Constant.INVITE_RECEIVED));
            }

            /**好友请求被接受**/
            @Override
            public void onFriendRequestAccepted(String s) {
                playSound();
                addContact(s);
                broadcastManager.sendBroadcast(new Intent(Constant.INVITE_ACCEPT));
            }

            /**好友请求被拒绝**/
            @Override
            public void onFriendRequestDeclined(String s) {

            }
        };
        EMClient.getInstance().contactManager().setContactListener(contactListener);
    }


    private void registerConnectionListener() {
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {

                if (error == EMError.USER_REMOVED) {

                } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {

                } else if (error == EMError.SERVER_SERVICE_RESTRICTED) {

                }
            }

            @Override
            public void onConnected() {

            }
        };
        EMClient.getInstance().addConnectionListener(connectionListener);
    }

    public boolean isLoggedIn() {
        return EMClient.getInstance().isLoggedInBefore();
    }


    /**加载好友列表**/
    public void loadContactList() {
        /**网络请求必须在子线程中进行**/
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> phoneNumberList = EMClient.getInstance().contactManager().getAllContactsFromServer();
                    String jsonPhoneNumber = new Gson().toJson(phoneNumberList);
                    HttpManager.getInstance().sendPost(jsonPhoneNumber, SELECT_PART_USER, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d("MainActivity", "加载好友列表失败");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String jsonUserList = response.body().string();
                            JsonParser parser = new JsonParser();
                            JsonArray jsonArray = parser.parse(jsonUserList).getAsJsonArray();
                            Gson gson = new Gson();
                            for (JsonElement element : jsonArray) {
                                /**litepal bug**/
                                User user = new User();
                                user = gson.fromJson(element, User.class);
                                user.save();
                            }
                        }
                    });
                }catch(HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**清除好友列表**/
    public void cleanupContactList() {
        new UserDao().cleanupContactList();
    }

    /**更新好友列表**/
    public void updateContactList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> phoneNumberList = EMClient.getInstance().contactManager().getAllContactsFromServer();
                    String jsonPhoneNumber = new Gson().toJson(phoneNumberList);
                    HttpManager.getInstance().sendPost(jsonPhoneNumber,SELECT_PART_USER, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d("MainActivity", "更新好友列表失败");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String jsonUserList = response.body().string();
                            JsonParser parser = new JsonParser();
                            JsonArray jsonArray = parser.parse(jsonUserList).getAsJsonArray();
                            Gson gson = new Gson();
                            for (JsonElement element : jsonArray) {
                                User user = gson.fromJson(element, User.class);
                                new UserDao().updateContact(user);
                            }
                        }
                    });
                } catch(HyphenateException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**退出登录**/
    public void signOut() {
        EMClient.getInstance().logout(true, new EMCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    /**添加新好友**/
    public void addContact(String phoneNumber) {
        HttpManager.getInstance().sendRequest(SELECT_USER + phoneNumber, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonUser = response.body().string();
                User user = new Gson().fromJson(jsonUser, User.class);

                List<User> userList = new UserDao().getContactList();
                for(User userTemp : userList) {
                    if(userTemp.getPhoneNumber().equals(user.getPhoneNumber())) return;
                }
                user.save();
            }
        });
    }


    public List<EMConversation> loadConversationList(){
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();

        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
                }
            }
        }
        try {
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<EMConversation> list = new ArrayList<EMConversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
    }

    /**
     * sort conversations according time stamp of last message
     */
    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {

                if (con1.first.equals(con2.first)) {
                    return 0;
                } else if (con2.first.longValue() > con1.first.longValue()) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });
    }


    public void playSound() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(mContext, notification);
        r.play();
    }
}
