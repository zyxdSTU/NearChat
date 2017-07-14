package nearchat.xiaoxiong.com.nearchat.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import nearchat.xiaoxiong.com.nearchat.db.InviteMessageDao;
import nearchat.xiaoxiong.com.nearchat.db.TrendDao;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.db.WordDao;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.Constant;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessage;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessageStatus;
import nearchat.xiaoxiong.com.nearchat.javabean.Trend;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.javabean.Word;
import nearchat.xiaoxiong.com.nearchat.service.AutoUpdateService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.DOWNLOAD_IMAGE;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.SELECT_PART_USER;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.SELECT_USER;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.UPDATE_USER;

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
    private BroadcastReceiver broadcastReceiver;


    private Manager() {}

    public static synchronized Manager getInstance() {
        if(manager == null) {
            manager = new Manager();
        }
        return manager;
    }

    public Context getContent() {
        return mContext;
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

        /**注册广播监听**/
        registerBroadcastReceiver();

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


    /**消息提示音**/
    public void playSound() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(mContext, notification);
        r.play();
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
                Log.d("MainActivity", "增加联系人");
                playSound();
                addContact(s);
            }

            /**删除好友**/
            @Override
            public void onContactDeleted(String s) {
                Log.d("MainActivity", "删除好友");
                Log.d("MainActivity", s);
                playSound();

                /**数据库删除**/
                if((new UserDao().getContact(s)) != null) {
                    Log.d("MainActivity", "数据库已经删除");
                    new UserDao().deleteContact(s);
                }

                /**删除相应对话**/
                if(EMClient.getInstance().chatManager().getConversation(s) != null) {
                    Log.d("MainActivity", "会话已经删除");
                    EMClient.getInstance().chatManager().deleteConversation(s, false);
                }

                /**通知刷新界面**/
                broadcastManager.sendBroadcast(new Intent(Constant.INVITE_DELETE));
            }

            /**接受到好友邀请**/
            @Override
            public void onContactInvited(String s, String s1) {
                Log.d("MainActivity", "接受到好友邀请");
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
                Log.d("MainActivity", "好友请求被接受");
            }

            /**好友请求被拒绝**/
            @Override
            public void onFriendRequestDeclined(String s) {

            }
        };
        EMClient.getInstance().contactManager().setContactListener(contactListener);
    }

    /**添加新好友**/
    public void addContact(String phoneNumber) {
        HttpManager.getInstance().sendRequest(SELECT_USER + phoneNumber, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("MainActivity", "好友添加");
                String jsonUser = response.body().string();
                User user = new Gson().fromJson(jsonUser, User.class);

                List<User> userList = new UserDao().getContactList();
                for(User userTemp : userList) {
                    if(userTemp.getPhoneNumber().equals(user.getPhoneNumber())) return;
                }
                new UserDao().getContactList();

                /**存进数据库**/
                user.save();

                new UserDao().getContactList();
                /**通知刷新界面**/
                broadcastManager.sendBroadcast(new Intent(Constant.INVITE_ACCEPT));
            }
        });
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
                            broadcastManager.sendBroadcast(new Intent(Constant.CONTACT_FAILED));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String jsonUserList = response.body().string();
                            JsonParser parser = new JsonParser();
                            JsonArray jsonArray = parser.parse(jsonUserList).getAsJsonArray();
                            Gson gson = new Gson();
                            for (JsonElement element : jsonArray) {
                                User user = new User();
                                user = gson.fromJson(element, User.class);

                                /**过滤**/
                                if(new UserDao().getContact(user.getPhoneNumber()) != null) {
                                    continue;
                                }
                                user.save();
                            }
                            broadcastManager.sendBroadcast(new Intent(Constant.CONTACT_COMPLETE));
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
                broadcastManager.sendBroadcast(new Intent(Constant.LOGOUT_SUCCESS));
            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onProgress(int i, String s) {

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

    public void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.LOGIN_SUCCESS);


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent intentService = new Intent(mContext, AutoUpdateService.class);
                String action = intent.getAction();
                if(action.equals(Constant.LOGIN_SUCCESS)) {
                    /**更新缓存中个人信息**/
                    updateCurrentUserInfoClient();
                    /*启动服务*/
                    mContext.startService(intentService);
                    return;
                }
            }
        };
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }


    /**更新当前用户信息**/
    public void updateCurrentUserInfoClient() {
        HttpManager.getInstance().sendRequest(SELECT_USER + EMClient.getInstance().getCurrentUser(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonCurrentUserInfo = response.body().string();
                PreferenceManager.getInstance().preferenceManagerRemove("currentUserInfo");
                PreferenceManager.getInstance().preferenceManagerSave("currentUserInfo", jsonCurrentUserInfo);
            }
        });
    }


    /**更新服务器用户信息**/
    public void updateCurrentUserInfoServer(User user) {
        String jsonUser = new Gson().toJson(user);
        HttpManager.getInstance().sendPost(jsonUser, UPDATE_USER, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }


    //通过经纬度计算两者之间路基distance
    public double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        /**地球半径**/
        double EARTH_RADIUS = 6378137.0;

        double radLat1 = (lat_a * Math.PI / 180.0);

        double radLat2 = (lat_b * Math.PI / 180.0);

        double a = radLat1 - radLat2;

        double b = (lng_a - lng_b) * Math.PI / 180.0;

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000.0;
        return s;
    }


    /**重载**/
    synchronized public void updateImagePreference(String phoneNumber, String imagePath) {
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(imagePath));
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);

            byte[] temp = new byte[1024];
            int size = 0;
            while ((size = in.read(temp)) != -1) {
                out.write(temp, 0, size);
            }
            byte[] imageByte = out.toByteArray();
            in.close();
            out.close();
            updateImagePreference(phoneNumber, imageByte);

        }catch(FileNotFoundException e) {
            Log.d("MainActivity", "文件找不到");
            e.printStackTrace();
        }catch(IOException e) {
            Log.d("MainActivity", "文件读写错误");
            e.printStackTrace();
        }
    }

    synchronized public void updateImagePreference(String phoneNumber, byte[] imageByte){
        String imageString = new String(Base64.encodeToString(imageByte, Base64.DEFAULT));
        PreferenceManager.getInstance().preferenceManagerSave(phoneNumber, imageString);
    }

    /**更新缓存中图片数据**/
    synchronized public void updateContactImage(){
        List<User> userList = new UserDao().getContactList();
        for(User user : userList) {
            final String phoneNumber = user.getPhoneNumber();
            /**从网络加载进缓存**/
            HttpManager.getInstance().sendRequest(DOWNLOAD_IMAGE + phoneNumber, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("MainActivity", "从网络加载图片失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                   byte[] imageByte = response.body().bytes();
                    if(imageByte.length > 0) {
                        Manager.getInstance().updateImagePreference(phoneNumber, imageByte);
                    }
                }
            });
        }
    }

    /*最近动态时间， 最近留言时间*/
    public long getRecentTrendTime() {
        List<Trend> trendList = new TrendDao().getAllTrend();
        long max = 0;
        if(trendList == null) return max;
        for(Trend trend : trendList) {
            if(trend.getCurrentTime() > max) max = trend.getCurrentTime();
        }
        return max;
    }

    public long getRecentWordTime() {
        List<Word> wordList = new WordDao().getAllWord();
        long max = 0;
        if(wordList == null) return max;
        for(Word word : wordList) {
            if(word.getCurrentTime() > max) max = word.getCurrentTime();
        }
        return max;
    }


}
